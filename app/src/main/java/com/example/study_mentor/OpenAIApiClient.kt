package com.example.study_mentor

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Singleton client that streams chat completions directly from the proxy
 * (https://api.vilao.ai/v1/chat/completions) using HttpURLConnection and lenient
 * JSONObject parsing to avoid schema errors caused by missing non-standard SSE fields.
 */
object OpenAIApiClient {

    private const val ENDPOINT_URL = "https://api.vilao.ai/v1/chat/completions"

    /**
     * Sends a user prompt and streams text chunks line-by-line as a [Flow<String>].
     *
     * @param prompt        The question typed by the user.
     * @param systemPrompt  Concise mentor persona instructions.
     * @return              A [Flow] emitting incremental text tokens directly.
     */
    fun askQuestion(
        prompt: String,
        imageBase64: String? = null,
        systemPrompt: String = "You are a concise study assistant. Answer in 1 to 2 sentences maximum. Do NOT use Markdown formatting (no asterisks or bold text). Emojis are allowed."
    ): Flow<String> = flow {
        val url = URL(ENDPOINT_URL)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 60_000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer ${BuildConfig.AI_API_KEY}")
            setRequestProperty("Accept", "text/event-stream")
        }

        try {
            // Build User message object: text only or multimodal (text + base64 image_url)
            val userMessageJson = JSONObject().apply {
                put("role", "user")
                if (imageBase64 != null) {
                    val contentArray = JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "text")
                            put("text", prompt.ifBlank { "What is in this image?" })
                        })
                        put(JSONObject().apply {
                            put("type", "image_url")
                            put("image_url", JSONObject().apply {
                                put("url", "data:image/jpeg;base64,$imageBase64")
                            })
                        })
                    }
                    put("content", contentArray)
                } else {
                    put("content", prompt)
                }
            }

            // Construct request payload with streaming enabled
            val rootJson = JSONObject().apply {
                put("model", "vgpt/gpt-6.1-sol")
                put("stream", true)
                put("max_tokens", 100)
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(userMessageJson)
                })
            }

            // Write JSON request body
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(rootJson.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorStream = connection.errorStream ?: connection.inputStream
                val errorBody = errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                Log.e("OpenAI_Debug", "HTTP $responseCode Error: $errorBody")
                throw Exception("HTTP $responseCode: $errorBody")
            }

            // Read SSE stream line by line
            BufferedReader(InputStreamReader(connection.inputStream, Charsets.UTF_8)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line?.trim() ?: continue
                    if (currentLine.isEmpty() || currentLine.startsWith(":")) {
                        continue // Keep-alive ping or empty line
                    }

                    if (currentLine.startsWith("data:")) {
                        val dataContent = currentLine.removePrefix("data:").trim()
                        if (dataContent == "[DONE]") {
                            break
                        }

                        try {
                            val chunkJson = JSONObject(dataContent)
                            val choices = chunkJson.optJSONArray("choices")
                            if (choices != null && choices.length() > 0) {
                                val firstChoice = choices.getJSONObject(0)
                                val delta = firstChoice.optJSONObject("delta")
                                if (delta != null && delta.has("content") && !delta.isNull("content")) {
                                    val text = delta.optString("content", "")
                                    if (text.isNotEmpty()) {
                                        emit(text)
                                    }
                                }
                            }
                        } catch (jsonErr: Exception) {
                            Log.w("OpenAI_Debug", "Skipping non-JSON SSE chunk: $dataContent", jsonErr)
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }.flowOn(Dispatchers.IO)
}
