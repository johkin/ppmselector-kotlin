import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	id("org.springframework.boot") version "2.5.1"
	kotlin("jvm") version "1.5.0"
	kotlin("plugin.spring") version "1.5.0"
	// id("org.springframework.experimental.aot") version "0.9.2"
}

group = "se.acrend.ppm"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/release") }
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:2.5.1"))
	implementation(platform("com.google.cloud:spring-cloud-gcp-dependencies:2.0.3"))
	implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2020.0.3"))

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:1.5.0")
	implementation("io.projectreactor.tools:blockhound:1.0.6.RELEASE")


	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-freemarker")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("com.google.cloud:spring-cloud-gcp-starter-logging")
	implementation("com.google.cloud:spring-cloud-gcp-starter-secretmanager")
	implementation("com.google.cloud:spring-cloud-gcp-starter-metrics")
	implementation("com.google.cloud:spring-cloud-gcp-starter-trace")


	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	implementation("com.sendgrid:sendgrid-java:4.6.7")
	implementation("org.freemarker:freemarker:2.3.30")
	implementation("commons-io:commons-io:2.8.0")

	implementation("org.jsoup:jsoup:1.10.3")

	implementation("org.apache.poi:poi:3.17")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	testImplementation("org.testcontainers:testcontainers:1.12.3")
	testImplementation("org.testcontainers:junit-jupiter:1.12.3")
	testImplementation("org.testcontainers:mongodb:1.14.3")
	testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

/*
tasks.withType<BootBuildImage> {
	builder = "paketobuildpacks/builder:tiny"
	environment = mapOf("BP_NATIVE_IMAGE" to "true")
}
*/
tasks.bootRun {
	args = listOf("--spring.profiles.active=gcp")
}