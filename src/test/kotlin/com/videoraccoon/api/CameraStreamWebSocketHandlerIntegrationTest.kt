package com.videoraccoon.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import jakarta.websocket.ContainerProvider
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// Requires the ZoneMinder/go2rtc Docker stack (zoneminder/docker-compose.yml)
// running locally with the "test" go2rtc stream configured, per
// zoneminder/README.md.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CameraStreamWebSocketHandlerIntegrationTest {

    @LocalServerPort
    var port: Int = 0

    @Test
    fun `relays fMP4 segments from go2rtc - init segment first, then moof fragments`() {
        val messages = CopyOnWriteArrayList<ByteArray>()
        val latch = CountDownLatch(5)

        val handler = object : AbstractWebSocketHandler() {
            override fun handleBinaryMessage(session: WebSocketSession, message: BinaryMessage) {
                messages.add(message.payload.array())
                latch.countDown()
            }
        }

        // Real H.264 keyframes exceed the 8KB default WebSocket message
        // buffer - any real client needs to raise this, same as the server
        // side does in WebSocketConfig.
        val container = ContainerProvider.getWebSocketContainer()
        container.defaultMaxBinaryMessageBufferSize = 1_048_576

        val session = StandardWebSocketClient(container)
            .execute(handler, "ws://localhost:$port/stream/test")
            .get(5, TimeUnit.SECONDS)

        try {
            assertTrue(latch.await(10, TimeUnit.SECONDS), "expected at least 5 segments, got ${messages.size}")

            val init = messages[0]
            assertTrue(
                boxTypeAt(init, 4) == "ftyp",
                "expected first segment to start with an ftyp box",
            )

            val fragment = messages[1]
            assertTrue(
                boxTypeAt(fragment, 4) == "moof",
                "expected subsequent segments to start with a moof box",
            )
        } finally {
            session.close()
        }
    }

    private fun boxTypeAt(bytes: ByteArray, offset: Int): String {
        return String(bytes, offset, 4, Charsets.US_ASCII)
    }
}
