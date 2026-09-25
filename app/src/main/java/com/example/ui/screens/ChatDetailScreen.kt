package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.ChatMessage
import com.example.data.local.ChatRoom
import com.example.ui.ChatViewModel
import com.example.ui.theme.BlueCheckmark
import com.example.ui.theme.CallRed
import com.example.ui.theme.DarkChatBg
import com.example.ui.theme.DarkIncomingBubble
import com.example.ui.theme.DarkOutgoingBubble
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PulseTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    room: ChatRoom,
    viewModel: ChatViewModel,
    onBack: () -> Unit,
    onStartCall: (peerId: String, peerName: String, isVideo: Boolean) -> Unit
) {
    BackHandler { onBack() }

    val messages by viewModel.activeRoomMessages.collectAsState()
    val typingStatus by viewModel.typingStatus.collectAsState()
    val myUserId by viewModel.userId.collectAsState()
    val isRecording by viewModel.voiceHelper.isRecording.collectAsState()
    val recordDuration by viewModel.voiceHelper.recordingDuration.collectAsState()
    val currentlyPlayingId by viewModel.voiceHelper.currentlyPlayingId.collectAsState()
    val playbackProgress by viewModel.voiceHelper.playbackProgress.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var selectedFullImage by remember { mutableStateOf<String?>(null) }
    var showChatMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Photo Picker contract (Zero-permission Play Store compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.sendImageMessage(uri.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (room.type == "ROOM") PulseTeal else EmeraldDark),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (room.type == "ROOM") Icons.Default.Groups else Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (typingStatus != null) {
                                Text(
                                    text = typingStatus ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = EmeraldPrimary,
                                    maxLines = 1
                                )
                            } else {
                                Text(
                                    text = if (room.type == "ROOM") "غرفة محادثة عامة" else "متصل الآن (WAN)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF8696A0),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Audio Call button
                    IconButton(
                        onClick = {
                            val peerId = if (room.type == "DIRECT") room.participantId.ifEmpty { room.roomId } else room.roomId
                            onStartCall(peerId, room.name, false)
                        },
                        modifier = Modifier.testTag("voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = Color.White
                        )
                    }

                    // Video Call button
                    IconButton(
                        onClick = {
                            val peerId = if (room.type == "DIRECT") room.participantId.ifEmpty { room.roomId } else room.roomId
                            onStartCall(peerId, room.name, true)
                        },
                        modifier = Modifier.testTag("video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color.White
                        )
                    }

                    IconButton(onClick = { showChatMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showChatMenu,
                        onDismissRequest = { showChatMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("معلومات المحادثة") },
                            onClick = { showChatMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("إرسال صورة تجريبية سريعة") },
                            onClick = {
                                showChatMenu = false
                                // High quality decorative sample photo
                                viewModel.sendImageMessage("sample://landscape_cairo")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("مسح المحادثة", color = CallRed) },
                            onClick = {
                                showChatMenu = false
                                viewModel.deleteRoom(room.roomId)
                                onBack()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkChatBg)
                .imePadding()
        ) {
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // WAN Notice Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFF1E293B).copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🔒 المحادثة مشفرة وتعمل عبر شبكة الإنترنت المباشرة (WAN) بين المحافظات",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    val isMe = message.senderId == myUserId
                    MessageBubble(
                        message = message,
                        isMe = isMe,
                        isPlaying = currentlyPlayingId == message.id,
                        playProgress = if (currentlyPlayingId == message.id) playbackProgress else 0f,
                        onPlayVoice = {
                            viewModel.voiceHelper.playAudio(message.id, message.voiceDurationSec, message.mediaUri)
                        },
                        onStopVoice = {
                            viewModel.voiceHelper.stopPlayback()
                        },
                        onImageClick = { uri ->
                            selectedFullImage = uri
                        }
                    )
                }
            }

            // Bottom Input Bar & Voice Recorder
            ChatInputBar(
                inputText = inputText,
                onInputChanged = {
                    inputText = it
                    viewModel.onTypingChanged(it.isNotEmpty())
                },
                onSend = {
                    if (inputText.trim().isNotEmpty()) {
                        viewModel.sendTextMessage(inputText)
                        inputText = ""
                        viewModel.onTypingChanged(false)
                    }
                },
                onPickImage = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isRecording = isRecording,
                recordDuration = recordDuration,
                onStartRecord = {
                    viewModel.voiceHelper.startRecording()
                },
                onStopAndSendRecord = {
                    val (file, duration) = viewModel.voiceHelper.stopRecording()
                    viewModel.sendVoiceMessage(file?.absolutePath ?: "", duration)
                },
                onCancelRecord = {
                    viewModel.voiceHelper.cancelRecording()
                }
            )
        }
    }

    // Full screen image preview dialog
    if (selectedFullImage != null) {
        Dialog(onDismissRequest = { selectedFullImage = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { selectedFullImage = null },
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { selectedFullImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                if (selectedFullImage?.startsWith("sample://") == true) {
                    Image(
                        painter = painterResource(id = R.drawable.chatpulse_icon_1790323074328),
                        contentDescription = "Preview",
                        modifier = Modifier.size(320.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    AsyncImage(
                        model = selectedFullImage,
                        contentDescription = "Full Preview",
                        modifier = Modifier.fillMaxWidth().height(400.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    isPlaying: Boolean,
    playProgress: Float,
    onPlayVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onImageClick: (String) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .background(if (isMe) DarkOutgoingBubble else DarkIncomingBubble)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column {
                if (!isMe) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                when (message.type) {
                    "IMAGE" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .clickable { message.mediaUri?.let { onImageClick(it) } },
                            contentAlignment = Alignment.Center
                        ) {
                            if (message.mediaUri?.startsWith("sample://") == true) {
                                Image(
                                    painter = painterResource(id = R.drawable.chatpulse_icon_1790323074328),
                                    contentDescription = "Image message",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else if (message.mediaUri != null) {
                                AsyncImage(
                                    model = message.mediaUri,
                                    contentDescription = "Image message",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Image, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                    "VOICE" -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            IconButton(
                                onClick = { if (isPlaying) onStopVoice() else onPlayVoice() },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldPrimary)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                VoiceWaveformVisualizer(
                                    isPlaying = isPlaying,
                                    progress = playProgress
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${message.voiceDurationSec}s • صوتية HD",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF8696A0)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = EmeraldPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Timestamp & Delivery Checkmarks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF8696A0)
                    )
                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = BlueCheckmark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceWaveformVisualizer(
    isPlaying: Boolean,
    progress: Float
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wavePulse"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
    ) {
        val barCount = 24
        val barWidth = 3.dp.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
        val centerY = size.height / 2

        for (i in 0 until barCount) {
            val progressAtBar = i.toFloat() / barCount
            val isPlayed = progressAtBar <= progress

            // Harmonic height pattern
            val baseScale = 0.25f + 0.75f * kotlin.math.abs(kotlin.math.sin(i * 0.45).toFloat())
            val animatedHeight = if (isPlaying && isPlayed) {
                baseScale * size.height * pulseAnim
            } else {
                baseScale * size.height
            }.coerceAtLeast(4.dp.toPx())

            val startX = i * (barWidth + spacing)
            val color = if (isPlayed) EmeraldPrimary else Color(0xFF64748B)

            drawLine(
                color = color,
                start = Offset(startX, centerY - animatedHeight / 2),
                end = Offset(startX, centerY + animatedHeight / 2),
                strokeWidth = barWidth
            )
        }
    }
}

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    onPickImage: () -> Unit,
    isRecording: Boolean,
    recordDuration: Int,
    onStartRecord: () -> Unit,
    onStopAndSendRecord: () -> Unit,
    onCancelRecord: () -> Unit
) {
    Surface(
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                // Recording Active UI
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1E293B))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Recording",
                        tint = CallRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جارٍ التسجيل: ${recordDuration}s",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onCancelRecord) {
                        Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Send Audio Button
                IconButton(
                    onClick = onStopAndSendRecord,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Voice",
                        tint = Color.White
                    )
                }
            } else {
                // Normal Input UI
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF202C33))
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPickImage,
                        modifier = Modifier.testTag("attach_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach Image",
                            tint = Color(0xFF8696A0)
                        )
                    }

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChanged,
                        placeholder = {
                            Text("اكتب رسالة...", color = Color(0xFF8696A0), fontSize = 15.sp)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_text_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = EmeraldPrimary
                        ),
                        singleLine = false,
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Send or Mic Button
                if (inputText.trim().isNotEmpty()) {
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .testTag("send_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                } else {
                    IconButton(
                        onClick = onStartRecord,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                            .testTag("record_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record Voice",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
