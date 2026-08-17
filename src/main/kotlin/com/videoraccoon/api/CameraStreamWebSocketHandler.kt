package com.videoraccoon.api

import com.videoraccoon.relay.CameraStreamRegistry
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.AbstractWebSocketHandler

@Component
class CameraStreamWebSocketHandler(
    private val registry: CameraStreamRegistry,
) : AbstractWebSocketHandler() {

    override fun afterConnectionEstablished(session: WebSocketSession) {
        registry.subscribe(streamName(session), session)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        registry.unsubscribe(streamName(session), session)
    }

    private fun streamName(session: WebSocketSession): String {
        return session.uri!!.path.substringAfterLast("/")
    }
}
