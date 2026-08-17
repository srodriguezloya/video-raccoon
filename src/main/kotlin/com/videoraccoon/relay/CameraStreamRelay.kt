package com.videoraccoon.relay

import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.slf4j.LoggerFactory
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.WebSocketSession
import java.net.URI
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executors

// Pulls one go2rtc fMP4 stream and fans it out to every subscribed client
// session - one upstream connection per camera, regardless of viewer count.
// Started lazily on first subscriber, torn down when the last one leaves.
//
// Uses Apache HttpClient 5 (already a transitive dependency via
// camel-http-starter) rather than java.net.http.HttpClient: the latter isn't
// well-suited to long-lived, indefinitely-streaming chunked responses and
// threw spurious "chunked transfer encoding" read errors under real testing.
class CameraStreamRelay(private val streamUrl: URI) {
    private val log = LoggerFactory.getLogger(CameraStreamRelay::class.java)
    private val sessions = CopyOnWriteArraySet<WebSocketSession>()
    private val executor = Executors.newSingleThreadExecutor()
    private val httpClient: CloseableHttpClient = HttpClients.createDefault()

    @Volatile private var initSegment: ByteArray? = null
    @Volatile private var upstream: CloseableHttpResponse? = null

    @Synchronized
    fun subscribe(session: WebSocketSession) {
        sessions.add(session)
        initSegment?.let { sendTo(session, it) }
        if (upstream == null) {
            executor.submit { pump() }
        }
    }

    @Synchronized
    fun unsubscribe(session: WebSocketSession) {
        sessions.remove(session)
        if (sessions.isEmpty()) {
            upstream?.close()
            upstream = null
            initSegment = null
        }
    }

    val isEmpty: Boolean
        @Synchronized get() = sessions.isEmpty()

    // The single-arg execute() is deprecated in favor of the callback-based
    // overload, which auto-closes the response when the callback returns -
    // the wrong shape here, since we hold the connection open across many
    // reads over an indefinite period rather than one bounded call.
    @Suppress("DEPRECATION")
    private fun pump() {
        try {
            httpClient.execute(HttpGet(streamUrl)).use { response ->
                synchronized(this) { upstream = response }

                val reader = Mp4SegmentReader(response.entity.content)
                var isFirstSegment = true
                while (true) {
                    val segment = reader.readNextSegment() ?: break
                    if (isFirstSegment) {
                        synchronized(this) { initSegment = segment }
                        isFirstSegment = false
                    }
                    for (session in sessions) {
                        sendTo(session, segment)
                    }
                }
            }
        } catch (e: Exception) {
            log.warn("Stream relay for {} stopped: {}", streamUrl, e.message)
        }
    }

    private fun sendTo(session: WebSocketSession, bytes: ByteArray) {
        if (!session.isOpen) return
        try {
            session.sendMessage(BinaryMessage(bytes))
        } catch (e: Exception) {
            log.warn("Failed to send segment to session {}: {}", session.id, e.message)
        }
    }
}
