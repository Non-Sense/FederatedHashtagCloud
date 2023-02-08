plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version("1.6.21")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    val ktorVersion = "2.0.1"
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}