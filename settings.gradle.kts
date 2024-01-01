pluginManagement {
    repositories {
        maven(url = "https://repo.huaweicloud.com/repository/maven")
        maven(url = "https://maven.aliyun.com/repository/central")
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven(url = "https://mirrors.tencent.com/nexus/repository/gradle-plugins")
//        enable these repos if above are not up-to-date
//        gradlePluginPortal()
//        mavenCentral()
    }
    plugins {
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm") version kotlinVersion
    }
}

rootProject.name = "speech2text"
include("app")
include("app:web")
include("subtitle")
