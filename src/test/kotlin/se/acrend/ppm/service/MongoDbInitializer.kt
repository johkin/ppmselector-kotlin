package se.acrend.ppm.service

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestInstancePostProcessor
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.MongoDBContainer


class MongoDbExtension : BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
    BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    val mongoDb = MongoDBContainer("mongo:4.2.8")

    override fun beforeAll(context: ExtensionContext) {
        mongoDb.start()

        val applicationContext = SpringExtension.getApplicationContext(context) as ConfigurableApplicationContext

        TestPropertyValues.of(
            "spring.data.mongodb.uri=${mongoDb.replicaSetUrl}",
        ).applyTo(applicationContext);
    }

    override fun afterAll(context: ExtensionContext) {
        if (!mongoDb.isShouldBeReused) {
            mongoDb.stop()
        }
    }

    override fun postProcessTestInstance(testInstance: Any?, context: ExtensionContext?) {
        TODO("Not yet implemented")
    }

    override fun beforeEach(context: ExtensionContext?) {
        TODO("Not yet implemented")
    }

    override fun afterEach(context: ExtensionContext?) {
        TODO("Not yet implemented")
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        TODO("Not yet implemented")
    }

    override fun afterTestExecution(context: ExtensionContext?) {
        TODO("Not yet implemented")
    }

}
