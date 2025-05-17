package com.example.elearning.service

import android.util.Log
import com.example.elearning.R
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {
    private val TAG = "GeminiService"
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            temperature = 0.7f
            topK = 40
            topP = 0.95f
            maxOutputTokens = 1024
        }
    )

    fun checkModelAvailability() {
        try {
            Log.d(TAG, "Using model: gemini-1.5-flash")
            Log.d(TAG, "API Key: ${apiKey.take(5)}...${apiKey.takeLast(5)}")
        } catch (e: Exception) {
            Log.e(TAG, "Error checking model availability", e)
        }
    }

    suspend fun generateLearningResources(
        quizTitle: String,
        quizDescription: String,
        failedQuestions: List<String>
    ): List<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                A student failed a quiz with the following details:
                
                Quiz Title: $quizTitle
                Quiz Description: $quizDescription
                
                The student had trouble with these questions:
                ${failedQuestions.joinToString("\n") { "- $it" }}
                
                Please provide ONLY a list of 5-10 direct links to high-quality, free online resources (such as YouTube, Wikipedia, W3Schools, GeeksforGeeks, Khan Academy, Coursera, or similar) that will help the student understand these topics. 
                Each link should be on a separate line, and include a short description (max 1 sentence) before the link. 
                Do NOT include any explanations, summaries, or text that is not a link or its description.
                Example format:
                - [Description](https://link)
            """.trimIndent()

            val response = model.generateContent(prompt)
            val responseText = response.text ?: return@withContext emptyList()
            // Return only lines that look like links
            responseText.lines().filter { it.contains("http") }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating resources", e)
            emptyList()
        }
    }
} 