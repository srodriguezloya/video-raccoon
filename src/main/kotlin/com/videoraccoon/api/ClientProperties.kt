package com.videoraccoon.api

import org.springframework.boot.context.properties.ConfigurationProperties

// Only relevant to browser-based clients (CORS/WebSocket origin checks are
// browser-enforced, not something a native desktop client hits) - covers
// the web viewer app now, any future browser client later.
@ConfigurationProperties(prefix = "client")
data class ClientProperties(
    val allowedOrigins: List<String>,
)
