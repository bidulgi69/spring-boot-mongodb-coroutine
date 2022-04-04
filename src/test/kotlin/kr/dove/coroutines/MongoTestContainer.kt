package kr.dove.coroutines

import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import javax.annotation.PreDestroy

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class MongoTestContainer {
    companion object {
        //  test 종료 이후,
        //  docker container 가 유지되는 경우를 방지.
        @PreDestroy
        fun stop() {
            CONTAINER.stop()
        }

        //  @Container manages docker images to be maintained per class.
        //  If you want the container to remain between test classes,
        //  you must set testcontainers.reuse.enable=true in the .testcontainers.properties file.
        @Container
        @JvmStatic
        val CONTAINER = MongoDBContainer("mongo:latest")
            .apply { addExposedPorts(27017) }
            .apply {
                waitingFor(
                    Wait.forHttp("/")
                        .forStatusCode(200)
                )
            }
            .apply { start() }
    }
}