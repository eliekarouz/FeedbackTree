import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
    val iosX64 = iosX64()
    val iosArm32 = iosArm32()
    val iosArm64 = iosArm64()

    configure(listOf(iosX64, iosArm32, iosArm64)) {
        binaries.framework("FTKMM") {
            freeCompilerArgs = listOf("-Xg0")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }

        val commonTest by getting {
            dependencies {
            }
        }
    }
}

// Create a task building a fat framework.
val packForXcode by tasks.creating(org.jetbrains.kotlin.gradle.tasks.FatFrameworkTask::class) {
    // The fat framework must have the same base name as the initial frameworks.
    baseName = "FTKMM"

    // The default destination directory is "<build directory>/fat-framework".
    destinationDir = file("$buildDir/xcode-frameworks")

    var mode: String = System.getenv("CONFIGURATION")?.toUpperCase() ?: "DEBUG"
    if (mode == "ENTERPRISE") {
        mode = "RELEASE"
    }

    val arch: String = System.getenv("ARCHS") ?: "x86_64"

    println("Configuration: $mode")
    println("Architecture: $arch")

    if (arch == "x86_64") {
        from(
            kotlin.targets.getByName<KotlinNativeTarget>("iosX64").binaries.getFramework(
                "FTKMM",
                mode
            )
        )
    } else if (arch == "armv7") {
        from(
            kotlin.targets.getByName<KotlinNativeTarget>("iosArm32").binaries.getFramework(
                "FTKMM",
                mode
            )
        )
    } else if (arch == "arm64") {
        from(
            kotlin.targets.getByName<KotlinNativeTarget>("iosArm64").binaries.getFramework(
                "FTKMM",
                mode
            )
        )
    } else if (arch.contains("armv7") && arch.contains("arm64")) {
        from(
            kotlin.targets.getByName<KotlinNativeTarget>("iosArm32").binaries.getFramework(
                "FTKMM",
                mode
            ),
            kotlin.targets.getByName<KotlinNativeTarget>("iosArm64").binaries.getFramework(
                "FTKMM",
                mode
            )
        )
    }
}

tasks.getByName("build").dependsOn(packForXcode)

// workaround for https://youtrack.jetbrains.com/issue/KT-27170
configurations.create("compileClasspath")