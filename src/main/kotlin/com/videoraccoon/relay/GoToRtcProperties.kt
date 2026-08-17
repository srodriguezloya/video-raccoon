package com.videoraccoon.relay

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "go2rtc")
data class GoToRtcProperties(
    val baseUrl: String,
)
