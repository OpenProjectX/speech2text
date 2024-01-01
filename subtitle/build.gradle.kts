plugins {
    java
}

dependencies {
    implementation("fr.noop:charset:1.0.1")
    implementation("org.apache.commons:commons-lang3:3.4")
    implementation("commons-cli:commons-cli:1.3.1")

//    testImplementation("junit:junit:4.13.2")
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
//    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")


}

tasks.withType<Test> {
    useJUnitPlatform()
}

