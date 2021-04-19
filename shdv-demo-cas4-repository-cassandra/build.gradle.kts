

val cassandraDriverVersion: String by project
val cassandraVersion: String by project
val coroutinesVersion: String by project
val testContainersVersion: String by project

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = rootProject.group
version = rootProject.version

repositories {
    mavenCentral{
        metadataSources {
            gradleMetadata()
            mavenPom()
        }
    }
}


dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("com.datastax.oss:java-driver-core:$cassandraDriverVersion")
    implementation("com.datastax.oss:java-driver-query-builder:$cassandraDriverVersion")
    kapt("com.datastax.oss:java-driver-mapper-processor:$cassandraDriverVersion")
    implementation("com.datastax.oss:java-driver-mapper-runtime:$cassandraDriverVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.3.8")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:$coroutinesVersion")

//    implementation("com.datastax.cassandra:cassandra-driver-core:$cassandraVersion")
//    implementation("com.datastax.cassandra:cassandra-driver-mapping:$cassandraVersion")
//    implementation("com.datastax.cassandra:cassandra-driver-extras:$cassandraVersion")

}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
