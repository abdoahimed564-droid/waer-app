package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CallHistory
import com.example.data.local.ChatRoom
import com.example.network.ConnectionStatus
import com.example.ui.ChatViewModel
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldWarning
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.PulseTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: ChatViewModel,
    onOpenChat: (ChatRoom) -> Unit,
    onStartCall: (peerId: String, peerName: String, isVideo: Boolean) -> Unit
) {
    val context = LocalContext.current
    val rooms by viewModel.rooms.collectAsState()
    val calls by viewModel.calls.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val username by viewModel.username.collectAsState()
    val userId by viewModel.userId.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("المحادثات", "الغرف", "المكالمات", "شبكة الـ WAN")

    var showMenu by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showDirectChatDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ChatPulse",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Status Dot
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (connectionStatus) {
                                            ConnectionStatus.CONNECTED -> OnlineGreen
                                            ConnectionStatus.CONNECTING -> GoldWarning
                                            ConnectionStatus.STANDALONE_WAN_MODE -> EmeraldPrimary
                                            ConnectionStatus.DISCONNECTED -> CallRed
                                        }
                                    )
                            )
                        }

                        // User token & city banner
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cm.setPrimaryClip(ClipData.newPlainText("ChatPulse ID", userId))
                                Toast.makeText(context, "تم نسخ معرفك الفريد بنجاح!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text(
                                text = "$username ($userId)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8696A0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showServerConfigDialog = true },
                        modifier = Modifier.testTag("server_config_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Server Settings",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("main_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("محادثة مباشرة جديدة (1-on-1)") },
                            onClick = {
                                showMenu = false
                                showDirectChatDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("إنشاء غرفة محادثة (Room)") },
                            onClick = {
                                showMenu = false
                                showCreateRoomDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("إعدادات السيرفر و WebRTC") },
                            onClick = {
                                showMenu = false
                                showServerConfigDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("تسجيل الخروج", color = CallRed) },
                            onClick = {
                                showMenu = false
                                viewModel.logout()
                            },
                            leadingIcon = { Icon(Icons.Default.ExitToApp, contentDescription = null, tint = CallRed) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 1) {
                        showCreateRoomDialog = true
                    } else {
                        showDirectChatDialog = true
                    }
                },
                containerColor = EmeraldPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_new_chat")
            ) {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Default.Groups else Icons.Default.Chat,
                    contentDescription = "New Action"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
        ) {
            // Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = Color.White,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = EmeraldPrimary,
                        width = 48.dp
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) EmeraldPrimary else Color(0xFF8696A0),
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> {
                    // Direct Chats & All active conversations
                    val directRooms = rooms.filter { it.type == "DIRECT" || it.type == "ROOM" }
                    ChatListView(
                        rooms = directRooms,
                        onRoomClick = onOpenChat,
                        onDeleteRoom = { viewModel.deleteRoom(it.roomId) }
                    )
                }
                1 -> {
                    // Rooms only
                    val groupRooms = rooms.filter { it.type == "ROOM" }
                    RoomsListView(
                        rooms = groupRooms,
                        onRoomClick = onOpenChat,
                        onCreateRoomClick = { showCreateRoomDialog = true }
                    )
                }
                2 -> {
                    // Calls History
                    CallsListView(
                        calls = calls,
                        onCallPeer = { peerId, peerName, isVideo ->
                            onStartCall(peerId, peerName, isVideo)
                        }
                    )
                }
                3 -> {
                    // WAN & WebRTC Architecture Info
                    WanArchitectureView(
                        serverUrl = viewModel.serverUrl.collectAsState().value,
                        connectionStatus = connectionStatus,
                        myId = userId,
                        onOpenSettings = { showServerConfigDialog = true }
                    )
                }
            }
        }
    }

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { name ->
                viewModel.createRoom(name, "ROOM")
                showCreateRoomDialog = false
            }
        )
    }

    if (showDirectChatDialog) {
        DirectChatDialog(
            onDismiss = { showDirectChatDialog = false },
            onStartDirectChat = { targetUsername, targetId ->
                viewModel.createRoom(targetUsername, "DIRECT", targetId)
                showDirectChatDialog = false
            }
        )
    }

    if (showServerConfigDialog) {
        ServerConfigDialog(
            currentUrl = viewModel.serverUrl.collectAsState().value,
            onDismiss = { showServerConfigDialog = false },
            onSave = { newUrl ->
                viewModel.updateServerUrl(newUrl)
                showServerConfigDialog = false
            }
        )
    }
}

@Composable
fun ChatListView(
    rooms: List<ChatRoom>,
    onRoomClick: (ChatRoom) -> Unit,
    onDeleteRoom: (ChatRoom) -> Unit
) {
    if (rooms.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "لا توجد محادثات حتى الآن",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اضغط على زر الرسالة بالأسفل لبدء محادثة مباشرة أو إنشاء غرفة",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(rooms, key = { it.roomId }) { room ->
                ChatRoomItem(
                    room = room,
                    onClick = { onRoomClick(room) }
                )
                HorizontalDivider(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 72.dp)
                )
            }
        }
    }
}

@Composable
fun ChatRoomItem(
    room: ChatRoom,
    onClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("room_item_${room.roomId}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Badge
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (room.type == "ROOM") PulseTeal else EmeraldDark
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (room.type == "ROOM") Icons.Default.Groups else Icons.Default.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = timeFormat.format(Date(room.lastTimestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (room.unreadCount > 0) EmeraldPrimary else Color(0xFF8696A0)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = room.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF8696A0),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (room.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${room.unreadCount}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                } else if (room.isPinned) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = "Pinned",
                        tint = Color(0xFF8696A0),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RoomsListView(
    rooms: List<ChatRoom>,
    onRoomClick: (ChatRoom) -> Unit,
    onCreateRoomClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "غرف المحادثة الجماعية العامة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "انضم أو أنشئ غرفة لمستخدمين في مختلف المحافظات والدول",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        ChatListView(rooms = rooms, onRoomClick = onRoomClick, onDeleteRoom = {})
    }
}

@Composable
fun CallsListView(
    calls: List<CallHistory>,
    onCallPeer: (peerId: String, peerName: String, isVideo: Boolean) -> Unit
) {
    if (calls.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = null,
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "سجل المكالمات فارغ",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ابدأ مكالمة صوتية أو فيديو WebRTC من أي محادثة مباشرة",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    } else {
        val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(calls, key = { it.id }) { call ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = call.peerName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (call.direction == "INCOMING") Icons.Default.CallReceived else Icons.Default.CallMade,
                                contentDescription = null,
                                tint = if (call.wasAnswered) CallGreen else CallRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${dateFormat.format(Date(call.timestamp))} • ${call.durationSec}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8696A0)
                            )
                        }
                    }

                    // Call Back Buttons
                    IconButton(onClick = { onCallPeer(call.peerId, call.peerName, false) }) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = EmeraldPrimary)
                    }
                    IconButton(onClick = { onCallPeer(call.peerId, call.peerName, true) }) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = EmeraldPrimary)
                    }
                }
                HorizontalDivider(color = Color(0xFF1E293B).copy(alpha = 0.5f), modifier = Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Composable
fun WanArchitectureView(
    serverUrl: String,
    connectionStatus: ConnectionStatus,
    myId: String,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "حالة الاتصال عبر الإنترنت (WAN)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when (connectionStatus) {
                                        ConnectionStatus.CONNECTED -> OnlineGreen.copy(alpha = 0.2f)
                                        ConnectionStatus.CONNECTING -> GoldWarning.copy(alpha = 0.2f)
                                        ConnectionStatus.STANDALONE_WAN_MODE -> EmeraldPrimary.copy(alpha = 0.2f)
                                        ConnectionStatus.DISCONNECTED -> CallRed.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "متصل بالسيرفر مباشر"
                                    ConnectionStatus.CONNECTING -> "جارٍ الاتصال..."
                                    ConnectionStatus.STANDALONE_WAN_MODE -> "وضع المحاكاة السحابية"
                                    ConnectionStatus.DISCONNECTED -> "غير متصل"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> OnlineGreen
                                    ConnectionStatus.CONNECTING -> GoldWarning
                                    ConnectionStatus.STANDALONE_WAN_MODE -> EmeraldPrimary
                                    ConnectionStatus.DISCONNECTED -> CallRed
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "السيرفر الفعّال: $serverUrl",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "معرفك الفريد: $myId",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F2624)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "كيف يتصل مستخدمان في محافظتين مختلفتين؟",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. سيرفر الإشارات (Signaling Server): يقوم السيرفر عبر Socket.io / WebSockets بربط الطرفين وتبادل عروض الاتصال (SDP Offer/Answer) ومعرفات الغرف.\n\n" +
                                "2. خوادم STUN: تقوم باكتشاف العنوان العام (Public IP:Port) لكل طرف لتجاوز الـ NAT والرواتر المنزلية.\n\n" +
                                "3. خوادم TURN (Coturn Relay): في حالة وجود جدار ناري صارم (Symmetric NAT) في شبكات 4G أو الشركات تمنع الـ P2P، يتم تمرير الصوت والفيديو عبر خادم TURN مشفراً من طرف لطرف.\n\n" +
                                "4. WebRTC Media Engine: يتولى معالجة الصوت بترميز Opus والفيديو بترميز VP8/H.264 عالي الجودة مع إلغاء الصدى والضوضاء.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
