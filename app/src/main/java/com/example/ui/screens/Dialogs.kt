package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "إنشاء غرفة محادثة جديدة",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "أدخل اسم الغرفة أو الموضوع، وسيتم إنشاء معرف فريد للغرفة يمكن مشاركته مع أي شخص.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("اسم الغرفة (مثال: أصدقاء الإسكندرية)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("create_room_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomName.trim().isNotEmpty()) {
                        onCreate(roomName.trim())
                    }
                },
                enabled = roomName.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_create_room_button")
            ) {
                Text("إنشاء الغرفة", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun DirectChatDialog(
    onDismiss: () -> Unit,
    onStartDirectChat: (username: String, targetId: String) -> Unit
) {
    var targetUsername by remember { mutableStateOf("") }
    var targetId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "بدء محادثة مباشرة (1-on-1)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "أدخل اسم المستخدم أو المعرف الفريد للشخص الذي تريد التحدث معه مباشرة.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = targetUsername,
                    onValueChange = {
                        targetUsername = it
                        if (targetId.isEmpty()) {
                            targetId = "cp_${it.lowercase().replace(" ", "_")}"
                        }
                    },
                    label = { Text("اسم الشخص (مثال: سارة كمال)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("direct_user_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetId,
                    onValueChange = { targetId = it },
                    label = { Text("معرف المستخدم (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetUsername.trim().isNotEmpty()) {
                        val finalId = if (targetId.trim().isNotEmpty()) targetId.trim() else "user_${System.currentTimeMillis()}"
                        onStartDirectChat(targetUsername.trim(), finalId)
                    }
                },
                enabled = targetUsername.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("confirm_direct_chat_button")
            ) {
                Text("بدء المحادثة", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color(0xFF94A3B8))
            }
        }
    )
}

@Composable
fun ServerConfigDialog(
    currentUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf(currentUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "إعدادات سيرفر الـ WAN و WebRTC",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column {
                Text(
                    text = "يمكنك ربط التطبيق بسيرفر Socket.io / WebSockets مخصص على الإنترنت (Cloud VPS مثل AWS, DigitalOcean, Heroku أو Cloud Run).",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط WebSocket السحابي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("server_url_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldPrimary,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = EmeraldPrimary,
                        unfocusedLabelColor = Color(0xFF94A3B8)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "خوادم STUN المضمنة: stun.l.google.com:19302\nخوادم TURN: Coturn مع تفويض المستخدمين",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldDark
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(url.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                modifier = Modifier.testTag("save_server_button")
            ) {
                Text("حفظ وتوصيل", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color(0xFF94A3B8))
            }
        }
    )
}
