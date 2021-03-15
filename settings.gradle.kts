pluginManagement {
	repositories {
		maven { url = uri("https://repo.spring.io/milestone") }
		maven { url = uri("https://repo.spring.io/release") }
		gradlePluginPortal()
	}
	/*
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "org.springframework.boot") {
				useModule("org.springframework.boot:spring-boot-gradle-plugin:${requested.version}")
			}
		}
	}
	 */
}
rootProject.name = "ppmselector"
