package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // --- Rooms ---
    @Query("SELECT * FROM chat_rooms ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllRooms(): Flow<List<ChatRoom>>

    @Query("SELECT * FROM chat_rooms WHERE roomId = :roomId LIMIT 1")
    suspend fun getRoomById(roomId: String): ChatRoom?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoom(room: ChatRoom)

    @Update
    suspend fun updateRoom(room: ChatRoom)

    @Query("DELETE FROM chat_rooms WHERE roomId = :roomId")
    suspend fun deleteRoom(roomId: String)

    @Query("UPDATE chat_rooms SET lastMessage = :lastMessage, lastTimestamp = :timestamp WHERE roomId = :roomId")
    suspend fun updateLastMessage(roomId: String, lastMessage: String, timestamp: Long)

    @Query("UPDATE chat_rooms SET unreadCount = 0 WHERE roomId = :roomId")
    suspend fun markRoomAsRead(roomId: String)

    // --- Messages ---
    @Query("SELECT * FROM chat_messages WHERE roomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("UPDATE chat_messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE roomId = :roomId")
    suspend fun clearMessagesInRoom(roomId: String)

    // --- Calls ---
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallHistory)

    @Query("DELETE FROM call_history")
    suspend fun clearCallHistory()
}
