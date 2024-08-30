plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "com.github.icpc.autoanalyst"
version = "1.0-SNAPSHOT"

configurations.all {
    resolutionStrategy {
//        failOnVersionConflict()
    }
}

repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        // We recommend limiting jitpack to our lib. But you can remove this line if you don't care.
        //group = "com.github.icpc.live-v3"
    }
}

dependencies {
    testImplementation(kotlin("test"))

    val liveVersion: String by project
    implementation("com.github.icpc.live-v3:org.icpclive.cds.core:$liveVersion")
    implementation("com.github.icpc.live-v3:org.icpclive.cds.clics:$liveVersion")
    implementation("com.github.icpc.live-v3:org.icpclive.cds.clics-api:$liveVersion")

    val exposedVersion: String by project
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")

    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")

    // https://mvnrepository.com/artifact/org.jfree/jfreechart
    implementation("org.jfree:jfreechart:1.5.5")

    // https://mvnrepository.com/artifact/org.jfree/jcommon
    implementation("org.jfree:jcommon:1.0.24")

    // https://mvnrepository.com/artifact/org.json/json
    implementation("org.json:json:20240303")

    // https://mvnrepository.com/artifact/org.yaml/snakeyaml
    implementation("org.yaml:snakeyaml:2.2")

    // https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
    implementation("com.mysql:mysql-connector-j:9.0.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.23.1")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api-kotlin
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.4.0")

    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-1.2-api
    // TODO: remove due to CVE
    implementation("org.apache.logging.log4j:log4j-1.2-api:2.23.1")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.11.0")

    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.16.1")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.15.0")

    // https://mvnrepository.com/artifact/commons-logging/commons-logging
    implementation("commons-logging:commons-logging:1.3.3")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-configuration2
    // TODO: remove due to CVE
    // implementation("org.apache.commons:commons-configuration2:2.11.0")

    // https://mvnrepository.com/artifact/commons-configuration/commons-configuration
    // TODO: remove due to CVE
    implementation("commons-configuration:commons-configuration:1.10")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.3")

    // https://mvnrepository.com/artifact/com.h2database/h2
    testImplementation("com.h2database:h2:2.3.232")

}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("katalyzeapp.Katalyze")
}

kotlin {
    jvmToolchain(21)
}