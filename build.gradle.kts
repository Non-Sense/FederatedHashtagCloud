plugins {
    kotlin("jvm") version "1.7.10"
}

group = "con.n0n5ense"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":database"))
    implementation(project(":apiserver"))

    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("com.github.sys1yagi.mastodon4j:mastodon4j:1.7.0")
    implementation("com.github.sys1yagi.mastodon4j:mastodon4j-rx:1.7.0")

    api("ch.qos.logback:logback-classic:1.2.8")
}