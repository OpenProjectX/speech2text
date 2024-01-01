package io.github.openprojectx.ai.speech2text

import org.springframework.boot.fromApplication
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.boot.with
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration(proxyBeanMethods = false)
class TestSpeech2textApplication {

	@Bean
	@ServiceConnection
	fun mariaDbContainer(): MariaDBContainer<*> {
		return MariaDBContainer(DockerImageName.parse("mariadb:latest"))
	}

}

fun main(args: Array<String>) {
	fromApplication<Speech2textWebApplication>().with(TestSpeech2textApplication::class).run(*args)
}
