plugins {
    id("java-library")
//    alias(libs.plugins.jetbrainsKotlinJvm) apply false
    id("org.jetbrains.kotlin.jvm")
    alias(libs.plugins.serialisation)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(kotlin("reflect"))
    implementation("io.github.rburgst:okhttp-digest:3.1.0")
}