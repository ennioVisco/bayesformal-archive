import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21"
    `maven-publish`
    application
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
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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

group = "tuwien.cps"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("lib/moonlight.jar"))
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("org.slf4j:slf4j-simple:1.7.29")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("at.ac.tuwien.cps.SingleTrace")
}