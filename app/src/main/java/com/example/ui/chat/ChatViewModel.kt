package com.example.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val currentSessionId: Long? = null,
    val currentSessionTitle: String = "Nueva conversación",
    val messages: List<ChatMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingContent: String = "",
    val selectedModel: String = ChatRepository.DEFAULT_MODEL,
    val systemPrompt: String = ChatRepository.DEFAULT_SYSTEM_INSTRUCTION,
    val hasApiKey: Boolean = false,
    val isUsingBuildConfig: Boolean = false,
    val currentApiKey: String = "",
    val isApiKeyDialogOpen: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        ChatUiState(
            selectedModel = repository.getSelectedModel(),
            systemPrompt = repository.getSystemInstruction(),
            hasApiKey = repository.hasValidApiKey(),
            isUsingBuildConfig = repository.isUsingBuildConfigKey(),
            currentApiKey = repository.getApiKey()
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var messagesJob: Job? = null

    init {
        // Collect sessions list
        viewModelScope.launch {
            repository.sessions.collectLatest { list ->
                _uiState.update { it.copy(sessions = list) }
                if (_uiState.value.currentSessionId == null) {
                    if (list.isNotEmpty()) {
                        selectSession(list.first().id)
                    } else {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun refreshKeyStatus() {
        _uiState.update {
            it.copy(
                hasApiKey = repository.hasValidApiKey(),
                isUsingBuildConfig = repository.isUsingBuildConfigKey(),
                currentApiKey = repository.getApiKey()
            )
        }
    }

    fun selectSession(sessionId: Long) {
        if (_uiState.value.currentSessionId == sessionId) return
        stopGeneration()

        val session = _uiState.value.sessions.find { it.id == sessionId }
        _uiState.update {
            it.copy(
                currentSessionId = sessionId,
                currentSessionTitle = session?.title ?: "Conversación",
                streamingContent = "",
                errorMessage = null
            )
        }

        // Cancel previous message listener and observe new session messages
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessages(sessionId).collectLatest { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            stopGeneration()
            val newId = repository.createNewSession()
            selectSession(newId)
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_uiState.value.currentSessionId == sessionId) {
                val remaining = _uiState.value.sessions.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    selectSession(remaining.first().id)
                } else {
                    createNewSession()
                }
            }
        }
    }

    fun clearCurrentMessages() {
        val currentId = _uiState.value.currentSessionId ?: return
        viewModelScope.launch {
            stopGeneration()
            repository.clearSessionMessages(currentId)
        }
    }

    fun sendMessage(promptText: String) {
        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isBlank() || _uiState.value.isGenerating) return

        val sessionId = _uiState.value.currentSessionId ?: return

        if (!repository.hasValidApiKey()) {
            _uiState.update {
                it.copy(
                    isApiKeyDialogOpen = true,
                    errorMessage = "Se requiere una clave API de Gemini para enviar mensajes."
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isGenerating = true,
                streamingContent = "",
                errorMessage = null
            )
        }

        viewModelScope.launch {
            // 1. Insert User Message
            repository.insertUserMessage(sessionId, cleanPrompt)

            // 2. Stream Response from Gemini API
            val accumulated = StringBuilder()
            streamJob = launch {
                repository.streamChatResponse(sessionId, cleanPrompt)
                    .catch { error ->
                        val errText = error.localizedMessage ?: "Error al comunicar con Gemini"
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                errorMessage = errText
                            )
                        }
                        repository.saveModelMessage(
                            sessionId = sessionId,
                            text = "⚠️ $errText",
                            isError = true
                        )
                    }
                    .collect { chunk ->
                        accumulated.append(chunk)
                        _uiState.update {
                            it.copy(streamingContent = accumulated.toString())
                        }
                    }

                // Streaming finished successfully
                if (accumulated.isNotEmpty()) {
                    repository.saveModelMessage(
                        sessionId = sessionId,
                        text = accumulated.toString(),
                        isError = false
                    )
                }
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        streamingContent = ""
                    )
                }
            }
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        val currentStreaming = _uiState.value.streamingContent
        val sessionId = _uiState.value.currentSessionId
        if (currentStreaming.isNotBlank() && sessionId != null) {
            viewModelScope.launch {
                repository.saveModelMessage(
                    sessionId = sessionId,
                    text = currentStreaming + " [Detenido]",
                    isError = false
                )
            }
        }
        _uiState.update {
            it.copy(
                isGenerating = false,
                streamingContent = ""
            )
        }
    }

    fun selectModel(model: String) {
        repository.saveSelectedModel(model)
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun selectPersona(prompt: String) {
        repository.saveSystemInstruction(prompt)
        _uiState.update { it.copy(systemPrompt = prompt) }
    }

    fun saveApiKey(key: String) {
        repository.saveCustomApiKey(key)
        refreshKeyStatus()
    }

    fun clearApiKey() {
        repository.clearCustomApiKey()
        refreshKeyStatus()
    }

    fun setApiKeyDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isApiKeyDialogOpen = open) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
