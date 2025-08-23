plugins {
    id("java")
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
}

group = "org.abstractvault.bytelyplay"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0-rc1")
    implementation("org.slf4j:slf4j-api:2.0.17")
}

tasks.test {
    useJUnitPlatform()
}
tasks.build {
    dependsOn("shadowJar")
}