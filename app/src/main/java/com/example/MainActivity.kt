package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.ChatViewModel
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    ChatPulseApp()
                }
            }
        }
    }
}

@Composable
fun ChatPulseApp(
    viewModel: ChatViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()

    when {
        activeCall != null -> {
            CallScreen(
                session = activeCall!!,
                viewModel = viewModel,
                onEndCall = { viewModel.endCall() }
            )
        }
        activeRoom != null -> {
            ChatDetailScreen(
                room = activeRoom!!,
                viewModel = viewModel,
                onBack = { viewModel.closeActiveRoom() },
                onStartCall = { peerId, peerName, isVideo ->
                    viewModel.startCall(peerId, peerName, isVideo)
                }
            )
        }
        isLoggedIn -> {
            MainScreen(
                viewModel = viewModel,
                onOpenChat = { room ->
                    viewModel.selectRoom(room)
                },
                onStartCall = { peerId, peerName, isVideo ->
                    viewModel.startCall(peerId, peerName, isVideo)
                }
            )
        }
        else -> {
            LoginScreen(
                onLoginSuccess = { username ->
                    viewModel.login(username)
                }
            )
        }
    }
}
