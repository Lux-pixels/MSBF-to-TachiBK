plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")

    implementation("com.squareup.okio:okio:3.10.2")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("MainKt")
}

kotlin {
    jvmToolchain(17)
}
