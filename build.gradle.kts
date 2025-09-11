plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
    id("maven-publish")
}

group = "org.abstractvault.bytelyplay"
version = "1.1.2-BETA"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.0.2-1")
    api("com.fasterxml.jackson.core:jackson-databind:2.20.0")
    implementation("org.slf4j:slf4j-api:2.0.17")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.20.0")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-smile:2.20.0")
}

tasks.test {
    useJUnitPlatform()
}
tasks.build {
    dependsOn("shadowJar")
}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }
    repositories {
        mavenLocal()
    }
}
java {
    withSourcesJar()
}