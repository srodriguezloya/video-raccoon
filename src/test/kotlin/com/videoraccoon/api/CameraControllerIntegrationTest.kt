package com.videoraccoon.api

import com.videoraccoon.canonical.Camera
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate
import org.springframework.boot.test.context.SpringBootTest

// Requires the ZoneMinder Docker stack (zoneminder/docker-compose.yml) running
// locally with the TestCamera monitor configured, per zoneminder/README.md.
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CameraControllerIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `lists cameras from the real ZoneMinder test stack`() {
        val cameras = restTemplate.getForObject("/cameras", Array<Camera>::class.java)!!

        val testCamera = cameras.find { it.name == "TestCamera" }
        assertTrue(testCamera != null, "expected a camera named 'TestCamera', got: ${cameras.toList()}")
        assertTrue(testCamera!!.enabled, "expected TestCamera to be enabled")
    }
}
