package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoom(
    @PrimaryKey
    val roomId: String,
    val name: String,
    val type: String, // "DIRECT" or "ROOM"
    val lastMessage: String,
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val avatarSeed: String = "",
    val isPinned: Boolean = false,
    val participantId: String = "" // For direct chats
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey
    val id: String,
    val roomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val type: String = "TEXT", // "TEXT", "IMAGE", "VOICE", "CALL_LOG"
    val mediaUri: String? = null,
    val voiceDurationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "DELIVERED" // "SENDING", "SENT", "DELIVERED", "READ"
)

@Entity(tableName = "call_history")
data class CallHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val peerId: String,
    val peerName: String,
    val callType: String, // "AUDIO" or "VIDEO"
    val direction: String, // "INCOMING" or "OUTGOING"
    val durationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val wasAnswered: Boolean = true
)
