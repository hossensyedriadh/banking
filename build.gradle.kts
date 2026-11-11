plugins {
	java
	id("java-library")
	id("maven-publish")
}

group = "io.github.hossensyedriadh"
version = "1.0.0-SNAPSHOT"
description = "Common cryptographic utilities for banking applications"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			groupId = "io.github.hossensyedriadh"
			artifactId = "crypto-commons"
			version = "1.0.0-SNAPSHOT"
		}
	}

	repositories {
		maven {
			url = uri("http://nexus.cerberus.internal:7081/repository/crypto-commons/")

			isAllowInsecureProtocol = true

			credentials {
				username = providers.gradleProperty("nexusPackageUser").orNull
				password = providers.gradleProperty("nexusPackagePassword").orNull
			}
		}
	}
}

dependencies {
	testImplementation(platform("org.junit:junit-bom:5.10.0"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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