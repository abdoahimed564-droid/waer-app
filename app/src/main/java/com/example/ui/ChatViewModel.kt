package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CallHistory
import com.example.data.local.ChatMessage
import com.example.data.local.ChatPulseDatabase
import com.example.data.local.ChatRoom
import com.example.data.repository.ChatRepository
import com.example.media.VoiceRecorderHelper
import com.example.network.CallSession
import com.example.network.CallState
import com.example.network.ConnectionStatus
import com.example.network.RealtimeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ChatPulseDatabase.getDatabase(application)
    private val repository = ChatRepository(db.chatDao())
    val voiceHelper = VoiceRecorderHelper(application)
    val realtimeManager = RealtimeManager(viewModelScope)

    private val prefs = application.getSharedPreferences("chatpulse_prefs", Context.MODE_PRIVATE)

    // User Session
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    // Navigation & Active Chat State
    private val _activeRoom = MutableStateFlow<ChatRoom?>(null)
    val activeRoom: StateFlow<ChatRoom?> = _activeRoom.asStateFlow()

    private val _activeRoomMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val activeRoomMessages: StateFlow<List<ChatMessage>> = _activeRoomMessages.asStateFlow()

    private var activeRoomJob: Job? = null

    // Typing State
    private val _typingStatus = MutableStateFlow<String?>(null)
    val typingStatus: StateFlow<String?> = _typingStatus.asStateFlow()

    // Active Call State
    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()
    private var callDurationJob: Job? = null

    // Room and Call Lists from Local Database
    val rooms: StateFlow<List<ChatRoom>> = repository.rooms.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val calls: StateFlow<List<CallHistory>> = repository.calls.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val connectionStatus: StateFlow<ConnectionStatus> = realtimeManager.connectionStatus
    val serverUrl: StateFlow<String> = realtimeManager.serverUrl

    init {
        // Load saved session if exists
        val savedUserId = prefs.getString("user_id", null)
        val savedUsername = prefs.getString("username", null)
        if (!savedUserId.isNullOrEmpty() && !savedUsername.isNullOrEmpty()) {
            _userId.value = savedUserId
            _username.value = savedUsername
            _isLoggedIn.value = true
            realtimeManager.currentUserId = savedUserId
            realtimeManager.currentUsername = savedUsername
            realtimeManager.connect()
            viewModelScope.launch {
                repository.prepopulateDefaultDataIfEmpty(savedUserId, savedUsername)
            }
        }

        // Collect incoming real-time messages
        viewModelScope.launch {
            realtimeManager.incomingMessages.collect { msg ->
                repository.saveMessage(msg)
            }
        }

        // Collect typing indicators
        viewModelScope.launch {
            realtimeManager.typingEvents.collect { event ->
                val current = _activeRoom.value
                if (current != null && current.roomId == event.roomId && event.userId != _userId.value) {
                    _typingStatus.value = if (event.isTyping) "${event.username} يكتب الآن..." else null
                }
            }
        }

        // Collect call signals
        viewModelScope.launch {
            realtimeManager.callSignals.collect { signal ->
                if (signal.targetId == _userId.value && signal.type == "CALL_INIT") {
                    _activeCall.value = CallSession(
                        callId = signal.callId,
                        peerId = signal.callerId,
                        peerName = signal.callerName,
                        callType = signal.callType,
                        isIncoming = true,
                        state = CallState.RINGING
                    )
                }
            }
        }
    }

    fun login(chosenUsername: String) {
        val cleanName = chosenUsername.trim()
        if (cleanName.isEmpty()) return

        // Generate unique token / ID based on username + random UUID slice
        val generatedId = "cp_${cleanName.lowercase().replace(" ", "_")}_${UUID.randomUUID().toString().substring(0, 6)}"

        _userId.value = generatedId
        _username.value = cleanName
        _isLoggedIn.value = true

        prefs.edit()
            .putString("user_id", generatedId)
            .putString("username", cleanName)
            .apply()

        realtimeManager.currentUserId = generatedId
        realtimeManager.currentUsername = cleanName
        realtimeManager.connect()

        viewModelScope.launch {
            repository.prepopulateDefaultDataIfEmpty(generatedId, cleanName)
        }
    }

    fun logout() {
        prefs.edit().clear().apply()
        _isLoggedIn.value = false
        _userId.value = ""
        _username.value = ""
        _activeRoom.value = null
        realtimeManager.disconnect()
    }

    fun selectRoom(room: ChatRoom) {
        _activeRoom.value = room
        _typingStatus.value = null
        viewModelScope.launch {
            repository.markRoomRead(room.roomId)
        }

        activeRoomJob?.cancel()
        activeRoomJob = viewModelScope.launch {
            repository.getMessagesForRoom(room.roomId).collect { msgList ->
                _activeRoomMessages.value = msgList
            }
        }
    }

    fun closeActiveRoom() {
        _activeRoom.value = null
        _typingStatus.value = null
        activeRoomJob?.cancel()
    }

    fun sendTextMessage(content: String) {
        val current = _activeRoom.value ?: return
        val text = content.trim()
        if (text.isEmpty()) return

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = current.roomId,
            senderId = _userId.value,
            senderName = _username.value,
            content = text,
            type = "TEXT",
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED"
        )

        viewModelScope.launch {
            repository.saveMessage(msg)
            realtimeManager.sendMessage(msg)
        }
    }

    fun sendImageMessage(mediaUri: String) {
        val current = _activeRoom.value ?: return

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = current.roomId,
            senderId = _userId.value,
            senderName = _username.value,
            content = "صورة مرفقة",
            type = "IMAGE",
            mediaUri = mediaUri,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED"
        )

        viewModelScope.launch {
            repository.saveMessage(msg)
            realtimeManager.sendMessage(msg)
        }
    }

    fun sendVoiceMessage(filePath: String, durationSec: Int) {
        val current = _activeRoom.value ?: return

        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            roomId = current.roomId,
            senderId = _userId.value,
            senderName = _username.value,
            content = "رسالة صوتية",
            type = "VOICE",
            mediaUri = filePath,
            voiceDurationSec = durationSec,
            timestamp = System.currentTimeMillis(),
            status = "DELIVERED"
        )

        viewModelScope.launch {
            repository.saveMessage(msg)
            realtimeManager.sendMessage(msg)
        }
    }

    fun onTypingChanged(isTyping: Boolean) {
        val current = _activeRoom.value ?: return
        realtimeManager.sendTyping(current.roomId, isTyping)
    }

    fun createRoom(name: String, type: String, participantId: String = "") {
        viewModelScope.launch {
            val roomId = if (type == "DIRECT") {
                "direct_${participantId.lowercase()}"
            } else {
                "room_${UUID.randomUUID().toString().substring(0, 8)}"
            }

            val newRoom = ChatRoom(
                roomId = roomId,
                name = name,
                type = type,
                lastMessage = if (type == "DIRECT") "بدأت المحادثة المباشرة" else "تم إنشاء الغرفة الجديدة",
                lastTimestamp = System.currentTimeMillis(),
                unreadCount = 0,
                avatarSeed = name,
                participantId = participantId
            )
            repository.createOrUpdateRoom(newRoom)
            selectRoom(newRoom)
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            repository.deleteRoom(roomId)
            if (_activeRoom.value?.roomId == roomId) {
                closeActiveRoom()
            }
        }
    }

    // --- Calling Logic ---
    fun startCall(peerId: String, peerName: String, isVideo: Boolean) {
        val callId = UUID.randomUUID().toString()
        val session = CallSession(
            callId = callId,
            peerId = peerId,
            peerName = peerName,
            callType = if (isVideo) "VIDEO" else "AUDIO",
            isIncoming = false,
            state = CallState.DIALING
        )
        _activeCall.value = session

        // Simulate dialing -> connecting ICE -> connected
        viewModelScope.launch {
            delay(1500)
            if (_activeCall.value?.callId == callId) {
                _activeCall.value = _activeCall.value?.copy(state = CallState.CONNECTING_ICE)
                delay(1200)
                if (_activeCall.value?.callId == callId) {
                    _activeCall.value = _activeCall.value?.copy(state = CallState.CONNECTED)
                    startCallDurationTimer()
                }
            }
        }
    }

    fun acceptIncomingCall() {
        val current = _activeCall.value ?: return
        _activeCall.value = current.copy(state = CallState.CONNECTED)
        startCallDurationTimer()
    }

    private fun startCallDurationTimer() {
        callDurationJob?.cancel()
        callDurationJob = viewModelScope.launch {
            while (_activeCall.value?.state == CallState.CONNECTED) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSec = it.durationSec + 1) }
            }
        }
    }

    fun endCall() {
        val current = _activeCall.value
        callDurationJob?.cancel()
        if (current != null) {
            // Save to call history in database
            viewModelScope.launch {
                repository.recordCall(
                    CallHistory(
                        peerId = current.peerId,
                        peerName = current.peerName,
                        callType = current.callType,
                        direction = if (current.isIncoming) "INCOMING" else "OUTGOING",
                        durationSec = current.durationSec,
                        timestamp = System.currentTimeMillis(),
                        wasAnswered = current.state == CallState.CONNECTED
                    )
                )
            }
        }
        _activeCall.value = null
    }

    fun toggleMute() {
        _activeCall.value = _activeCall.value?.let { it.copy(isMicMuted = !it.isMicMuted) }
    }

    fun toggleCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isCameraOff = !it.isCameraOff) }
    }

    fun toggleSpeaker() {
        _activeCall.value = _activeCall.value?.let { it.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun flipCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun updateServerUrl(url: String) {
        realtimeManager.updateServerUrl(url)
    }
}
