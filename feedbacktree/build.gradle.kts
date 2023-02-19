plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    signing
    id("org.jetbrains.dokka")
}


android {

    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

}

dependencies {
    api(project(":core"))

    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.1.0")
    implementation("com.squareup.coordinators:coordinators:0.4")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
}

// Because the components are created only during the afterEvaluate phase, you must
// configure your publications using the afterEvaluate() lifecycle method.
afterEvaluate {
    publishing {
        publications {
            register("release", MavenPublication::class) {
                from(components["release"])
                group = project.group
                version = "${project.version}"
            }

            register("debug", MavenPublication::class) {
                from(components["debug"])
                group = project.group
                version = "${project.version}"
            }
        }
    }
}