package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.api.GeminiApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class ChatRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.chatDao()
    private val apiClient = GeminiApiClient()
    private val prefs: SharedPreferences = context.getSharedPreferences("gemini_studio_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_CUSTOM_API_KEY = "custom_gemini_api_key"
        private const val PREF_SELECTED_MODEL = "selected_model"
        private const val PREF_SYSTEM_INSTRUCTION = "system_instruction"
        const val DEFAULT_MODEL = "gemini-3.5-flash"
        const val DEFAULT_SYSTEM_INSTRUCTION =
            "Eres Gemini Studio, un asistente de IA avanzado de Google. Eres experto en desarrollo de software, Kotlin, Jetpack Compose, análisis técnico y productividad en tiempo real. Respondes con claridad, precisión, tono profesional y formato Markdown enriquecido con bloques de código limpios."
    }

    val sessions: Flow<List<ChatSessionEntity>> = dao.getAllSessions()

    fun getSession(sessionId: Long): Flow<ChatSessionEntity?> = dao.getSessionById(sessionId)

    fun getMessages(sessionId: Long): Flow<List<ChatMessageEntity>> = dao.getMessagesForSession(sessionId)

    fun getApiKey(): String {
        val customKey = prefs.getString(PREF_CUSTOM_API_KEY, null)?.trim()
        if (!customKey.isNullOrBlank()) {
            return customKey
        }
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (e: Exception) {
            ""
        }
        return if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }
    }

    fun hasValidApiKey(): Boolean = getApiKey().isNotBlank()

    fun isUsingBuildConfigKey(): Boolean {
        val customKey = prefs.getString(PREF_CUSTOM_API_KEY, null)?.trim()
        return customKey.isNullOrBlank() && getApiKey().isNotBlank()
    }

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString(PREF_CUSTOM_API_KEY, key.trim()).apply()
    }

    fun clearCustomApiKey() {
        prefs.edit().remove(PREF_CUSTOM_API_KEY).apply()
    }

    fun getSelectedModel(): String {
        return prefs.getString(PREF_SELECTED_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun saveSelectedModel(model: String) {
        prefs.edit().putString(PREF_SELECTED_MODEL, model).apply()
    }

    fun getSystemInstruction(): String {
        return prefs.getString(PREF_SYSTEM_INSTRUCTION, DEFAULT_SYSTEM_INSTRUCTION) ?: DEFAULT_SYSTEM_INSTRUCTION
    }

    fun saveSystemInstruction(instruction: String) {
        prefs.edit().putString(PREF_SYSTEM_INSTRUCTION, instruction).apply()
    }

    suspend fun createNewSession(title: String = "Nueva conversación"): Long {
        val session = ChatSessionEntity(
            title = title,
            modelName = getSelectedModel(),
            systemPrompt = getSystemInstruction()
        )
        return dao.insertSession(session)
    }

    suspend fun updateSessionTitle(sessionId: Long, title: String) {
        val session = ChatSessionEntity(
            id = sessionId,
            title = title,
            updatedAt = System.currentTimeMillis(),
            modelName = getSelectedModel(),
            systemPrompt = getSystemInstruction()
        )
        dao.updateSession(session)
    }

    suspend fun deleteSession(sessionId: Long) {
        dao.deleteSession(sessionId)
    }

    suspend fun clearSessionMessages(sessionId: Long) {
        dao.clearMessagesForSession(sessionId)
    }

    suspend fun insertUserMessage(sessionId: Long, text: String): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            role = "user",
            content = text,
            timestamp = System.currentTimeMillis()
        )
        val id = dao.insertMessage(message)
        // Also update session title if it was default
        val history = dao.getMessagesListForSession(sessionId)
        if (history.size <= 1) {
            val title = if (text.length > 32) text.take(30) + "..." else text
            updateSessionTitle(sessionId, title)
        }
        return id
    }

    suspend fun saveModelMessage(sessionId: Long, text: String, isError: Boolean = false): Long {
        val message = ChatMessageEntity(
            sessionId = sessionId,
            role = "model",
            content = text,
            timestamp = System.currentTimeMillis(),
            isError = isError
        )
        return dao.insertMessage(message)
    }

    /**
     * Executes streaming generation and yields chunks.
     */
    fun streamChatResponse(
        sessionId: Long,
        prompt: String,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            throw IllegalStateException("No se ha configurado la clave API de Gemini. Agrega tu clave en Configuración o en los Secretos de AI Studio.")
        }

        val allMessages = dao.getMessagesListForSession(sessionId)
        // Prepare history excluding the current prompt
        val history = allMessages
            .dropLast(1)
            .filter { !it.isError && it.content.isNotBlank() }
            .takeLast(10) // Keep context compact and fast
            .map { it.role to it.content }

        val model = getSelectedModel()
        val sysPrompt = getSystemInstruction()

        apiClient.streamGenerateContent(
            apiKey = apiKey,
            model = model,
            history = history,
            prompt = prompt,
            systemInstruction = sysPrompt,
            temperature = temperature
        ).collect { chunk ->
            emit(chunk)
        }
    }
}
