plugins {
	java
	id("org.springframework.boot") version "4.0.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "DBMS.UniSlot"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// ── Core Spring Boot ──────────────────────────────────────
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")

	// ── JWT (JSON Web Tokens) for stateless authentication ────
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

	// ── Database (MySQL) ─────────────────────────────────────
	runtimeOnly("com.mysql:mysql-connector-j")

	// ── PDF Generation (iText 7 community) ───────────────────
	implementation("com.itextpdf:itext7-core:8.0.4")

	// ── Lombok — reduces boilerplate (getters/setters/etc.) ───
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// ── OpenAPI / Swagger UI ──────────────────────────────────
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

	// ── MapStruct — DTO ↔ Entity mapping ─────────────────────
	implementation("org.mapstruct:mapstruct:1.6.2")
	annotationProcessor("org.mapstruct:mapstruct-processor:1.6.2")

	// ── Testing ───────────────────────────────────────────────
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("com.h2database:h2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
