import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.4.21"
}

dependencies {
    implementation(compose.desktop.currentOs);
    implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-beta03")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.5.3")
    implementation("org.xerial:sqlite-jdbc:3.43.2.1")
    implementation("org.jetbrains.exposed", "exposed-core", "0.44.0")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.44.0")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.44.0")
    implementation("org.xerial:sqlite-jdbc:3.43.2.1")
    implementation("com.google.cloud:google-cloud-storage:2.29.1")
    implementation ("org.jetbrains.exposed:exposed-jodatime:0.34.1")
    implementation ("joda-time:joda-time:2.10.10")
    implementation ("io.ktor:ktor-client-core:2.3.6")
    implementation ("io.ktor:ktor-client-cio:2.3.6")
    implementation("com.google.cloud:google-cloud-storage:2.29.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
    implementation("io.ktor:ktor-client-serialization:2.3.6")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.6")
    implementation("io.ktor:ktor-client-json:2.3.6")
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    implementation(project(mapOf("path" to ":models")))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation(kotlin("test"))
    implementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.ktor:ktor-client-mock:2.3.6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}


tasks.test {
    useJUnitPlatform()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    from("/Users/seangong/IdeaProjects/CS346-project/src/main/resources/chinook.db") {
        into("resources")
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            includeAllModules = true
            macOS{
                iconFile.set(File("top.svg"));
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi);
                files("chinook.db");
            }
        }
    }
}