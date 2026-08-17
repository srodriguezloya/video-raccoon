package com.videoraccoon.api

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean

// H.264 keyframes routinely exceed Tomcat's default 8KB WebSocket message
// buffer (verified: a real fMP4 keyframe segment from our test source hit
// this exactly and got its connection dropped). 1MB is a placeholder upper
// bound, not a measured real-world value - revisit once real camera keyframe
// sizes are known.
private const val MAX_BINARY_MESSAGE_BUFFER_SIZE = 1_048_576

@Configuration
@EnableWebSocket
class WebSocketConfig(
    private val cameraStreamWebSocketHandler: CameraStreamWebSocketHandler,
) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        // Path variables aren't supported by this registry (that's an MVC/
        // @RequestMapping feature) - match with a wildcard and extract the
        // stream name from the URI manually, as CameraStreamWebSocketHandler does.
        registry.addHandler(cameraStreamWebSocketHandler, "/stream/*")
    }

    // Bean name "webSocketContainer" is what Spring's WebSocket support looks
    // for to configure the embedded Tomcat container - see
    // ServletServerContainerFactoryBean's own Javadoc.
    @Bean
    fun webSocketContainer(): ServletServerContainerFactoryBean {
        val container = ServletServerContainerFactoryBean()
        container.setMaxBinaryMessageBufferSize(MAX_BINARY_MESSAGE_BUFFER_SIZE)
        return container
    }
}
