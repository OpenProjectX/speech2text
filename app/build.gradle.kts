buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-layer-filter-extension-gradle:0.3.0")
        classpath("com.google.cloud.tools:jib-gradle-plugin-extension-api:0.4.0")
    }
}

plugins {
    val kotlinVersion: String by System.getProperties()
    id("org.springframework.boot") version "3.1.5" apply false
    id("io.spring.dependency-management") version "1.1.3" apply false
    kotlin("plugin.spring") version kotlinVersion apply false
}

subprojects {

    logger.info("applying plugins for spring applications $name")
    plugins.apply("io.spring.dependency-management")
    plugins.apply("org.springframework.boot")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
}
