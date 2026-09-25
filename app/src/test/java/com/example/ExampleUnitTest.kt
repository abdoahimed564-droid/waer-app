package com.example

import com.example.data.local.ChatMessage
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun chatPulse_uniqueId_generation() {
    val username = "Ahmed Radwan"
    val clean = username.trim().lowercase().replace(" ", "_")
    val uid = "cp_${clean}_${UUID.randomUUID().toString().substring(0, 6)}"
    assertTrue(uid.startsWith("cp_ahmed_radwan_"))
    assertEquals(23, uid.length)
  }

  @Test
  fun chatMessage_defaults_valid() {
    val msg = ChatMessage(
      id = "test_msg_1",
      roomId = "room_cairo",
      senderId = "user_1",
      senderName = "Ahmed",
      content = "Hello ChatPulse"
    )
    assertEquals("TEXT", msg.type)
    assertEquals("DELIVERED", msg.status)
    assertNull(msg.mediaUri)
  }
}

