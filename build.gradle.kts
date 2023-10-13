plugins {
    kotlin("jvm") version "1.9.20-Beta"
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
    mainClass.set("BossSeaDumpingKt")
}


group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.e-iceblue.cn/repository/maven-public/")
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    // selenium
    implementation("org.seleniumhq.selenium:selenium-java:4.14.0")
    implementation("org.seleniumhq.selenium:selenium-http-jdk-client:4.13.0")


    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
