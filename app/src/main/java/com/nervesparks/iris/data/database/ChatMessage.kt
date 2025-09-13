package com.nervesparks.iris.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val timestamp: Long,
    val isUser: Boolean,
    val embedding: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatMessage

        if (id != other.id) return false
        if (text != other.text) return false
        if (timestamp != other.timestamp) return false
        if (isUser != other.isUser) return false
        if (!embedding.contentEquals(other.embedding)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + text.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + isUser.hashCode()
        result = 31 * result + embedding.contentHashCode()
        return result
    }
}
