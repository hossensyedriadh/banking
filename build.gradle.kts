import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "io.github.hossensyedriadh"
version = "1.0.0-SNAPSHOT"
description = "ATM Terminal simulation application"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    maven {
        url = uri("http://nexus.cerberus.internal:7081/repository/banking-internal/")
        isAllowInsecureProtocol = true
        credentials {
            username = providers.gradleProperty("nexusBuildUser").orNull
            password = providers.gradleProperty("nexusBuildPassword").orNull
        }
    }
    mavenCentral()
}

configurations.configureEach {
	exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-log4j2")
    compileOnly("org.projectlombok:lombok")
    implementation("org.jpos:jpos:3.0.2")
    implementation("org.jline:jline:3.30.0")
    implementation("io.github.hossensyedriadh:crypto-commons:1.0.0-SNAPSHOT")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
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