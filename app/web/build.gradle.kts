import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.cloud.tools.jib.gradle.extension.layerfilter.Configuration
import org.jetbrains.kotlin.cli.jvm.compiler.findMainClass

buildscript {
    dependencies {
        classpath("com.google.cloud.tools:jib-layer-filter-extension-gradle:0.3.0")
    }
}


plugins {
    id("com.google.cloud.tools.jib") version "3.4.0"
}

val otelAgent by configurations.creating {
    // You can add any specific configuration settings here if needed
}


//configurations {
//    create("otelAgent"){
//        isCanBeResolved = false
//    }
//}


extra["springShellVersion"] = "3.1.5"

dependencies {
    implementation(project(":subtitle"))

    otelAgent("io.opentelemetry.javaagent:opentelemetry-javaagent:1.31.0")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
//	implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    implementation("com.lordcodes.turtle:turtle:0.9.0")
    implementation("org.apache.commons:commons-lang3")
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.httpcomponents.client5:httpclient5")
    testImplementation("org.apache.httpcomponents.core5:httpcore5-reactive")

    implementation("org.elasticsearch.plugin:x-pack-sql-jdbc:8.11.3")
    implementation("org.opensearch.driver:opensearch-sql-jdbc:1.4.0.1")
    implementation("org.tikv:tikv-client-java:3.3.5")


    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
    runtimeOnly("org.mariadb:r2dbc-mariadb:1.1.3")
    runtimeOnly("io.opentelemetry.javaagent:opentelemetry-javaagent:1.32.0")

    testRuntimeOnly("org.mariadb.jdbc:mariadb-java-client")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mariadb")
    testImplementation("org.testcontainers:r2dbc")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    builder.set("paketobuildpacks/builder-jammy-base:latest")
}

jib {
    val resourcesDir = "/app/resources"
    val otelAgentJar = configurations["otelAgent"].singleFile

    from {
        image = "c173chuj.mirror.aliyuncs.com/library/eclipse-temurin:17-jdk"
    }

    container {
        mainClass = "io.github.openprojectx.ai.speech2text.Speech2textWebApplicationKt"

        jvmFlags = listOf(
            "-javaagent:$resourcesDir/${otelAgentJar.name}" // OTel agent
            // Add other JVM flags if necessary
        )
//        environment = mapOf(
//            "OTEL_SERVICE_NAME" to "your-service-name",
//            "OTEL_EXPORTER" to "otlp",
//            "OTEL_EXPORTER_OTLP_ENDPOINT" to "your-otlp-collector-endpoint"
//        )
        // Set entrypoint and other configuration if needed
    }

    extraDirectories {
        paths {
            path {
                setFrom(otelAgentJar.parent)
                into = resourcesDir
            }
        }
    }

    pluginExtensions {

    }
}

configure<com.google.cloud.tools.jib.gradle.JibExtension> {
    pluginExtensions {
        pluginExtension {
            implementation = "com.google.cloud.tools.jib.gradle.extension.layerfilter.JibLayerFilterExtension"
            configuration(Action<Configuration> {
                filters {
                    configurations["developmentOnly"]
                        .resolvedConfiguration
                        .firstLevelModuleDependencies
                        .flatMap { dependency ->
                            dependency.moduleArtifacts.map { artifact ->
                                artifact.file
                            }
                        }.forEach {
                            filter {
                                glob = "/app/libs/${it.name}"
                            }
                        }
                }
            })
        }
    }
}
