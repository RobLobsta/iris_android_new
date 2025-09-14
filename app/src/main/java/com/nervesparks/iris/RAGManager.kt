package com.nervesparks.iris

import android.content.Context
import android.llama.cpp.LLamaAndroid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.sqrt

class RAGManager(
    private val context: Context,
    private val llama: LLamaAndroid,
    private val onEmbeddingsReady: () -> Unit,
) {

    private val knowledgeBase: List<String> = loadKnowledgeBase()
    private val embeddings: MutableMap<String, FloatArray> = mutableMapOf()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            generateEmbeddings()
            onEmbeddingsReady()
        }
    }

    private fun loadKnowledgeBase(): List<String> {
        val knowledgeBase = mutableListOf<String>()
        try {
            val inputStream = context.assets.open("knowledge_base.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                knowledgeBase.add(line!!)
            }
            reader.close()
            inputStream.close()
        } catch (e: Exception) {
            Log.e("RAGManager", "Error loading knowledge base: ${e.message}")
        }
        return knowledgeBase
    }

    private suspend fun generateEmbeddings() {
        for (chunk in knowledgeBase) {
            try {
                val embedding = llama.embedding(chunk)
                embeddings[chunk] = embedding
            } catch (e: Exception) {
                Log.e("RAGManager", "Error generating embedding for chunk: $chunk, error: ${e.message}")
            }
        }
    }

    suspend fun search(query: String, topN: Int = 3): List<String> {
        if (embeddings.isEmpty()) {
            return emptyList()
        }

        val queryEmbedding = llama.embedding(query)

        val similarities = embeddings.map { (chunk, chunkEmbedding) ->
            chunk to cosineSimilarity(queryEmbedding, chunkEmbedding)
        }

        return similarities.sortedByDescending { it.second }
            .take(topN)
            .map { it.first }
    }

    private fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        var dotProduct = 0.0f
        var norm1 = 0.0f
        var norm2 = 0.0f
        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            norm1 += vec1[i] * vec1[i]
            norm2 += vec2[i] * vec2[i]
        }
        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }
}
