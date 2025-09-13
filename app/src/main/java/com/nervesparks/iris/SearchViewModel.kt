package com.nervesparks.iris

import android.llama.cpp.LLamaAndroid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nervesparks.iris.data.database.ChatMessage
import com.nervesparks.iris.data.database.ChatMessageDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class SearchViewModel(
    private val chatMessageDao: ChatMessageDao,
    private val llamaAndroid: LLamaAndroid
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ChatMessage>>(emptyList())
    val searchResults: StateFlow<List<ChatMessage>> = _searchResults

    fun search(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                val queryEmbedding = llamaAndroid.embedding(query)
                val allMessages = chatMessageDao.getAllMessages().first()

                val rankedMessages = allMessages.map { message ->
                    val messageEmbedding = byteArrayToFloatArray(message.embedding)
                    val similarity = cosineSimilarity(queryEmbedding, messageEmbedding)
                    message to similarity
                }
                .sortedByDescending { it.second }
                .map { it.first }

                _searchResults.value = rankedMessages
            } else {
                _searchResults.value = emptyList()
            }
        }
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
        return dotProduct / (kotlin.math.sqrt(norm1) * kotlin.math.sqrt(norm2))
    }

    private fun byteArrayToFloatArray(byteArray: ByteArray): FloatArray {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        val floatBuffer = byteBuffer.asFloatBuffer()
        val floatArray = FloatArray(floatBuffer.remaining())
        floatBuffer.get(floatArray)
        return floatArray
    }
}

class SearchViewModelFactory(
    private val chatMessageDao: ChatMessageDao,
    private val llamaAndroid: LLamaAndroid
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(chatMessageDao, llamaAndroid) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
