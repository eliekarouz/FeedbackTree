// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google()
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    }
}

allprojects {
    group = "com.github.eliekarouz.feedbacktree"

    version = System.getenv("GITHUB_REF_NAME")?.takeIf { it.isNotEmpty() } ?: "0.16.0"

    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
    }
    afterEvaluate {
        extensions.findByType<PublishingExtension>()?.apply {
            repositories {
                maven {
                    name = "sonatype"
                    setUrl("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = properties["SONATYPE_NEXUS_USERNAME"]?.toString()
                        password = properties["SONATYPE_NEXUS_PASSWORD"]?.toString()
                    }
                }
            }
            publications.withType<MavenPublication>().configureEach {
                artifact(javadocJar.get())
                pom {
                    name.set("feedbacktree")
                    description.set("Unidirectional architecture for Android")
                    url.set("https://github.com/eliekarouz/feedbacktree")

                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                            distribution.set("repo")
                        }
                    }
                    developers {
                        developer {
                            id.set("eliekarouz")
                            name.set("Elie Karouz")
                        }
                    }
                    scm {
                        connection.set("scm:git:https://github.com/eliekarouz/feedbacktree")
                        developerConnection.set("scm:git:https://github.com/eliekarouz/feedbacktree")
                        url.set("https://github.com/eliekarouz/feedbacktree")
                    }
                }
            }
        }


        extensions.findByType<SigningExtension>()?.apply {
            val publishing = extensions.findByType<PublishingExtension>() ?: return@apply
            val key = properties["signingKey"]?.toString()?.replace("\\n", "\n")

            println("signing")
            useInMemoryPgpKeys(key, "")
            sign(publishing.publications)
        }

        tasks.withType<Sign>().configureEach {
            onlyIf { isReleaseBuild }
        }

        tasks.withType(org.jetbrains.dokka.gradle.DokkaTask::class.java) {
//            multiplatform {
//                extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.targets?.forEach {
//                    create(
//                        it.name
//                    )
//                }
//            }
        }
    }
}

val isReleaseBuild: Boolean
    get() = properties.containsKey("signingKey")

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}