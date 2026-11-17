import org.gradle.kotlin.dsl.named
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	java
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.hossensyedriadh"
version = "1.0.0-SNAPSHOT"
description = "Internet Banking Channel Service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

configurations.configureEach {
	exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-session-jdbc")
//	implementation("org.springframework.boot:spring-boot-starter-flyway")
//	implementation("org.springframework.boot:spring-boot-starter-kafka")
//	implementation("org.springframework.boot:spring-boot-starter-quartz")
	implementation("org.springframework.boot:spring-boot-starter-restclient")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
//	implementation("com.mchange:c3p0:0.14.0")
//	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.apache.commons:commons-pool2:2.13.1")
	implementation("org.apache.httpcomponents.client5:httpclient5:5.6.5")
	implementation("org.springframework.boot:spring-boot-starter-log4j2")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.21.0")
	implementation("org.mapstruct:mapstruct:1.6.3")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
	annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
//	implementation("org.redisson:redisson:4.2.0")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
//	testImplementation("org.springframework.boot:spring-boot-starter-quartz-test")
	testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
	testImplementation("org.springframework.boot:spring-boot-starter-security-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-session-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
	testCompileOnly("org.projectlombok:lombok")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<Jar> {
	manifest {
		attributes(
			"Developer" to "Syed Riadh Hossen",
			"Developer-Email" to "riadhhossen@gmail.com",
			"Developer-URL" to "https://hossensyedriadh.github.io"
		)
	}
}

tasks.named<BootRun>("bootRun") {
	systemProperty(
		"spring.profiles.active",
		System.getenv("SPRING_PROFILES_ACTIVE") ?: "dev"
	)
}