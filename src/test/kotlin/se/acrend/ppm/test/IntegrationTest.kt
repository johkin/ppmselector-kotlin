package se.acrend.ppm.test

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import se.acrend.ppm.Application
import se.acrend.ppm.config.IntegrationTestConfig

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataMongoTest
@ContextConfiguration(classes = [Application::class])
@Import(IntegrationTestConfig::class)
@ActiveProfiles("test")
@ComponentScan(basePackageClasses = [Application::class])
annotation class IntegrationTest
