package com.videoraccoon.adapter.zoneminder

import com.fasterxml.jackson.annotation.JsonProperty

data class ZmMonitorsResponse(
    @JsonProperty("monitors") val monitors: List<ZmMonitorEntry>,
)

data class ZmMonitorEntry(
    @JsonProperty("Monitor") val monitor: ZmMonitor,
)

data class ZmMonitor(
    @JsonProperty("Id") val id: String,
    @JsonProperty("Name") val name: String,
    @JsonProperty("Enabled") val enabled: String,
)
