package com.example.data.repository

import com.example.data.local.CallHistory
import com.example.data.local.ChatDao
import com.example.data.local.ChatMessage
import com.example.data.local.ChatRoom
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(private val chatDao: ChatDao) {

    val rooms: Flow<List<ChatRoom>> = chatDao.getAllRooms()
    val calls: Flow<List<CallHistory>> = chatDao.getAllCalls()

    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForRoom(roomId)
    }

    suspend fun getRoom(roomId: String): ChatRoom? {
        return chatDao.getRoomById(roomId)
    }

    suspend fun saveMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
        val preview = when (message.type) {
            "IMAGE" -> "📷 صورة"
            "VOICE" -> "🎤 رسالة صوتية (${message.voiceDurationSec}s)"
            "CALL_LOG" -> "📞 ${message.content}"
            else -> message.content
        }
        chatDao.updateLastMessage(message.roomId, preview, message.timestamp)
    }

    suspend fun updateMessageStatus(messageId: String, status: String) {
        chatDao.updateMessageStatus(messageId, status)
    }

    suspend fun createOrUpdateRoom(room: ChatRoom) {
        chatDao.insertOrUpdateRoom(room)
    }

    suspend fun markRoomRead(roomId: String) {
        chatDao.markRoomAsRead(roomId)
    }

    suspend fun deleteRoom(roomId: String) {
        chatDao.deleteRoom(roomId)
        chatDao.clearMessagesInRoom(roomId)
    }

    suspend fun recordCall(call: CallHistory) {
        chatDao.insertCall(call)
    }

    suspend fun prepopulateDefaultDataIfEmpty(myUserId: String, myUsername: String) {
        val existing = chatDao.getRoomById("room_tech_egypt")
        if (existing != null) return

        val now = System.currentTimeMillis()

        // Room 1: Developers across governorates
        val room1 = ChatRoom(
            roomId = "room_tech_egypt",
            name = "مطورين ومصممين مصر 🇪🇬",
            type = "ROOM",
            lastMessage = "مرحباً بكم في ChatPulse! الاتصال يعمل عبر الإنترنت بين المحافظات.",
            lastTimestamp = now - 1000 * 60 * 5,
            unreadCount = 2,
            avatarSeed = "tech",
            isPinned = true
        )
        chatDao.insertOrUpdateRoom(room1)

        val m1 = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = "room_tech_egypt",
            senderId = "user_cairo_1",
            senderName = "أحمد رضوان (القاهرة)",
            content = "السلام عليكم، تطبيق ChatPulse شغال تماماً عبر الـ WAN بسيرفر WebRTC و Socket.io!",
            type = "TEXT",
            timestamp = now - 1000 * 60 * 15,
            status = "READ"
        )
        val m2 = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = "room_tech_egypt",
            senderId = "user_alex_2",
            senderName = "سارة كمال (الإسكندرية)",
            content = "وعليكم السلام يا أحمد، الصوت والفيديو واضحين جداً بتقنية STUN/TURN بدون تقطيع!",
            type = "TEXT",
            timestamp = now - 1000 * 60 * 10,
            status = "READ"
        )
        val m3 = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = "room_tech_egypt",
            senderId = "user_mansoura_3",
            senderName = "محمود علي (المنصورة)",
            content = "مرحباً بكم في ChatPulse! الاتصال يعمل عبر الإنترنت بين المحافظات.",
            type = "TEXT",
            timestamp = now - 1000 * 60 * 5,
            status = "DELIVERED"
        )
        chatDao.insertMessage(m1)
        chatDao.insertMessage(m2)
        chatDao.insertMessage(m3)

        // Room 2: Direct chat with Sara
        val directRoom = ChatRoom(
            roomId = "direct_sara_alex",
            name = "سارة كمال (الإسكندرية)",
            type = "DIRECT",
            lastMessage = "🎤 رسالة صوتية (5s)",
            lastTimestamp = now - 1000 * 60 * 30,
            unreadCount = 1,
            avatarSeed = "sara",
            participantId = "user_alex_2"
        )
        chatDao.insertOrUpdateRoom(directRoom)

        val d1 = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = "direct_sara_alex",
            senderId = "user_alex_2",
            senderName = "سارة كمال",
            content = "أهلاً بك في المحادثة المباشرة! جرب إجراء مكالمة فيديو بالضغط على الأيقونة بالأعلى.",
            type = "TEXT",
            timestamp = now - 1000 * 60 * 45,
            status = "READ"
        )
        val d2 = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = "direct_sara_alex",
            senderId = "user_alex_2",
            senderName = "سارة كمال",
            content = "تسجيل صوتي تجريبي للتأكد من جودة الصوت",
            type = "VOICE",
            voiceDurationSec = 5,
            timestamp = now - 1000 * 60 * 30,
            status = "DELIVERED"
        )
        chatDao.insertMessage(d1)
        chatDao.insertMessage(d2)

        // Sample Call History
        val call1 = CallHistory(
            peerId = "user_alex_2",
            peerName = "سارة كمال (الإسكندرية)",
            callType = "VIDEO",
            direction = "INCOMING",
            durationSec = 245,
            timestamp = now - 1000 * 60 * 60 * 2,
            wasAnswered = true
        )
        val call2 = CallHistory(
            peerId = "user_cairo_1",
            peerName = "أحمد رضوان (القاهرة)",
            callType = "AUDIO",
            direction = "OUTGOING",
            durationSec = 118,
            timestamp = now - 1000 * 60 * 60 * 6,
            wasAnswered = true
        )
        chatDao.insertCall(call1)
        chatDao.insertCall(call2)
    }
}
