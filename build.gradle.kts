val javaVersion = JavaVersion.VERSION_17.majorVersion

allprojects {
    repositories {
        maven(url = "https://repo.huaweicloud.com/repository/maven/")
        maven(url = "https://maven.aliyun.com/repository/central/")
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
//        enable this repo if above are not up-to-date
//        mavenCentral()
    }

}

plugins {
    kotlin("jvm")
}


ext {
    val kotlinVersion: String by System.getProperties()
    set("kotlinVersion", kotlinVersion)
}

subprojects {
    if (this.name != "app" && this.name != "subtitle") {

        plugins.apply("org.jetbrains.kotlin.jvm")

        dependencies {
            implementation(kotlin("stdlib"))
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = javaVersion
            }
        }

        tasks.withType<JavaCompile> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }
    }
}

