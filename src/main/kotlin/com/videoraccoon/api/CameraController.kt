package com.videoraccoon.api

import com.videoraccoon.canonical.Camera
import org.apache.camel.ProducerTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CameraController(
    private val producerTemplate: ProducerTemplate,
) {

    @GetMapping("/cameras")
    fun listCameras(): List<Camera> {
        @Suppress("UNCHECKED_CAST")
        return producerTemplate.requestBody("direct:zoneminder.getCameras", "") as List<Camera>
    }
}
