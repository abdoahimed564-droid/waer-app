package com.example.network

data class IceServerConfig(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

object WebRtcConfig {
    // Default Public STUN servers for WAN NAT Traversal
    val defaultIceServers = listOf(
        IceServerConfig(urls = listOf("stun:stun.l.google.com:19302")),
        IceServerConfig(urls = listOf("stun:stun1.l.google.com:19302")),
        IceServerConfig(urls = listOf("stun:stun.cloudflare.com:3478")),
        // Standard COTURN TURN relay example for symmetric NAT traversal across strict firewalls
        IceServerConfig(
            urls = listOf("turn:turn.chatpulse.cloud:3478?transport=udp", "turn:turn.chatpulse.cloud:3478?transport=tcp"),
            username = "chatpulse_user",
            credential = "chatpulse_secure_pass"
        )
    )
}

enum class CallState {
    IDLE,
    DIALING,
    RINGING,
    CONNECTING_ICE,
    CONNECTED,
    ENDED
}

data class CallSession(
    val callId: String,
    val peerId: String,
    val peerName: String,
    val callType: String, // "AUDIO" or "VIDEO"
    val isIncoming: Boolean,
    val state: CallState = CallState.DIALING,
    val durationSec: Int = 0,
    val isMicMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val isFrontCamera: Boolean = true,
    val latencyMs: Int = 38,
    val packetLossPercent: Float = 0.2f,
    val resolution: String = "1080p (HD 30fps)"
)
