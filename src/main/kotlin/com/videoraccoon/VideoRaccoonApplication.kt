package com.videoraccoon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class VideoRaccoonApplication

fun main(args: Array<String>) {
	runApplication<VideoRaccoonApplication>(*args)
}
