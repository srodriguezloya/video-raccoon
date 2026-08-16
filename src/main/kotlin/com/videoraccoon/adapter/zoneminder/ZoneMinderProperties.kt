package com.videoraccoon.adapter.zoneminder

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zoneminder")
data class ZoneMinderProperties(
    val baseUrl: String,
)
