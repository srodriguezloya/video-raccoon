package com.videoraccoon.adapter.zoneminder

import tools.jackson.databind.ObjectMapper
import com.videoraccoon.canonical.Camera
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class ZoneMinderRoutes(
    private val properties: ZoneMinderProperties,
    private val objectMapper: ObjectMapper,
) : RouteBuilder() {

    override fun configure() {
        from("direct:zoneminder.getCameras")
            // camel-http infers GET vs POST from whether the exchange has a body;
            // set it explicitly so a non-empty triggering body can't turn this into a POST.
            .setHeader(Exchange.HTTP_METHOD, constant("GET"))
            .to("${properties.baseUrl}/api/monitors.json")
            .process { exchange ->
                val body = exchange.message.getBody(String::class.java)
                val response = objectMapper.readValue(body, ZmMonitorsResponse::class.java)
                exchange.message.body = response.monitors.map { entry ->
                    Camera(
                        id = entry.monitor.id,
                        name = entry.monitor.name,
                        enabled = entry.monitor.enabled == "1",
                    )
                }
            }
    }
}
