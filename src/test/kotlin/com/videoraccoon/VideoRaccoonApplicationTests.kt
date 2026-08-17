package com.videoraccoon

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

// RANDOM_PORT, not the default mock web environment: the WebSocket support
// needs a real embedded servlet container to attach to (see WebSocketConfig's
// webSocketContainer bean), which the mock environment never starts.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VideoRaccoonApplicationTests {

	@Test
	fun contextLoads() {
	}

}
