plugins {
    id("java-library")
    id("io.freefair.lombok") version "8.14.2"
    id("com.gradleup.shadow") version "9.0.2"
    id("maven-publish")
}

group = "org.abstractvault.bytelyplay"
version = "1.2.3-BETA"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    api("tools.jackson.core:jackson-databind:3.1.3")
    implementation("org.slf4j:slf4j-api:2.0.17")
    api("tools.jackson.dataformat:jackson-dataformat-cbor:3.1.3")
    api("tools.jackson.dataformat:jackson-dataformat-smile:3.1.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_24;
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