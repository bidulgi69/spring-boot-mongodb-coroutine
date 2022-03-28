package kr.dove.coroutines

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

@SpringBootApplication
class KotlinCoroutinesApplication {

	@Bean
	fun builder(): WebClient.Builder {
		return WebClient.builder()
	}
}

fun main(args: Array<String>) {
	runApplication<KotlinCoroutinesApplication>(*args)
}
