plugins {
    id("java")

    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "org.docker"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.projectreactor:reactor-core:3.5.10")

    implementation("io.r2dbc:r2dbc-h2:1.0.0.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    implementation("ch.qos.logback:logback-classic:1.4.11")

    testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:1.7.30")

    implementation("com.auth0:java-jwt:4.2.1");

    implementation("org.mindrot:jbcrypt:0.4")

}

tasks.shadowJar{
    manifest{
        attributes["Main-Class"] = "org.docker.server.Server"
    }
}


tasks.test {
    useJUnitPlatform()
}