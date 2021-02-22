import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.4.RELEASE"
	kotlin("jvm") version "1.4.0"
	kotlin("plugin.spring") version "1.4.0"
}

group = "se.acrend.ppm"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
	implementation(platform("org.springframework.boot:spring-boot-dependencies:2.3.4.RELEASE"))
	implementation(platform("org.springframework.cloud:spring-cloud-gcp-dependencies:1.2.5.RELEASE"))

	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-freemarker")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	implementation("org.springframework.cloud:spring-cloud-gcp-starter-logging")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter-secretmanager")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter-metrics")
	implementation("org.springframework.cloud:spring-cloud-gcp-starter-trace")


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
