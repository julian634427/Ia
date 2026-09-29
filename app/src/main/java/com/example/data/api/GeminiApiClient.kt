package com.example.data.api

import android.util.Log
import com.example.data.model.GeminiContent
import com.example.data.model.GeminiGenerationConfig
import com.example.data.model.GeminiPart
import com.example.data.model.GeminiRequest
import com.example.data.model.GeminiResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Streams content generation using Server-Sent Events (SSE).
     */
    fun streamGenerateContent(
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>, // role to message
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): Flow<String> = flow {
        val cleanModel = if (model.startsWith("models/")) model.removePrefix("models/") else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:streamGenerateContent?alt=sse&key=$apiKey"

        val contents = mutableListOf<GeminiContent>()
        history.forEach { (role, text) ->
            contents.add(
                GeminiContent(
                    role = if (role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = text))
                )
            )
        }
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val sysContent = if (!systemInstruction.isNullOrBlank()) {
            GeminiContent(
                parts = listOf(GeminiPart(text = systemInstruction))
            )
        } else null

        val geminiReq = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = temperature),
            systemInstruction = sysContent
        )

        val jsonBody = requestAdapter.toJson(geminiReq)
        val body = jsonBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "HTTP ${response.code}"
            val friendlyMsg = try {
                val json = JSONObject(errorBody)
                val errObj = json.optJSONObject("error")
                errObj?.optString("message") ?: errorBody
            } catch (e: Exception) {
                errorBody
            }
            throw RuntimeException(friendlyMsg)
        }

        val responseBody = response.body ?: throw RuntimeException("Respuesta vacía del servidor")
        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            val raw = line?.trim() ?: continue
            if (!raw.startsWith("data:")) continue
            val jsonPayload = raw.removePrefix("data:").trim()
            if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

            try {
                val jsonObj = JSONObject(jsonPayload)
                val candidates = jsonObj.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val text = parts.getJSONObject(0).optString("text")
                        if (!text.isNullOrEmpty()) {
                            emit(text)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("GeminiApiClient", "Error parsing chunk: $jsonPayload", e)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Standard non-streaming generateContent fallback.
     */
    suspend fun generateContent(
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>,
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f
    ): String = withContext(Dispatchers.IO) {
        val cleanModel = if (model.startsWith("models/")) model.removePrefix("models/") else model
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$cleanModel:generateContent?key=$apiKey"

        val contents = mutableListOf<GeminiContent>()
        history.forEach { (role, text) ->
            contents.add(
                GeminiContent(
                    role = if (role == "user") "user" else "model",
                    parts = listOf(GeminiPart(text = text))
                )
            )
        }
        contents.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val sysContent = if (!systemInstruction.isNullOrBlank()) {
            GeminiContent(parts = listOf(GeminiPart(text = systemInstruction)))
        } else null

        val geminiReq = GeminiRequest(
            contents = contents,
            generationConfig = GeminiGenerationConfig(temperature = temperature),
            systemInstruction = sysContent
        )

        val jsonBody = requestAdapter.toJson(geminiReq)
        val body = jsonBody.toRequestBody(jsonMediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val resStr = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val msg = try {
                JSONObject(resStr).optJSONObject("error")?.optString("message") ?: resStr
            } catch (e: Exception) {
                resStr
            }
            throw RuntimeException(msg)
        }

        val parsed = responseAdapter.fromJson(resStr)
        val text = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        text ?: "No se recibió respuesta."
    }
}
