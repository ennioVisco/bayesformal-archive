import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "tuwien.cps"
version = "1.0-SNAPSHOT"

plugins {
    // Kotlin support
    kotlin("jvm") version "1.5.21"

    // Package publishing
    `maven-publish`

    // CLI runnable
    application

    // Dependency analysis
    id("org.kordamp.gradle.jdeps") version "0.15.0"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "bayesformal"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set("Bayes-Formal Experiment")
                description.set("An experiment to explore a bayes-formal approach to smart city analysis")
                url.set("https://zk35.org/")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("ennioVisco")
                        name.set("Ennio Visconti")
                        email.set("ennio.visconti@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/ennioVisco/bayesformal.git")
                    developerConnection.set("scm:git:ssh://github.com/ennioVisco/bayesformal.git")
                    url.set("https://github.com/ennioVisco/bayesformal/")
                }
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ennioVisco/bayesformal")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(files("lib/moonlight.jar"))
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.slf4j:slf4j-simple:1.7.29")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    testImplementation(kotlin("test"))
    compileOnly("org.kordamp.gradle:jdeps-gradle-plugin:0.15.0")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("at.ac.tuwien.cps.SingleTraceKt")
}