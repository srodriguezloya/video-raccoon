package com.videoraccoon.relay

import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketSession
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

// streamName is currently the go2rtc stream name directly (e.g. "test").
// Resolving a canonical camera id to a go2rtc stream name is a known
// follow-up, not solved here - see zoneminder/README.md's go2rtc section.
@Component
class CameraStreamRegistry(
    private val properties: GoToRtcProperties,
) {
    private val relays = ConcurrentHashMap<String, CameraStreamRelay>()

    fun subscribe(streamName: String, session: WebSocketSession) {
        val relay = relays.computeIfAbsent(streamName) {
            CameraStreamRelay(URI.create("${properties.baseUrl}/api/stream.mp4?src=$streamName"))
        }
        relay.subscribe(session)
    }

    fun unsubscribe(streamName: String, session: WebSocketSession) {
        val relay = relays[streamName] ?: return
        relay.unsubscribe(session)
        if (relay.isEmpty) {
            relays.remove(streamName)
        }
    }
}
