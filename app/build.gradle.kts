plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.eliekarouz.feedbacktree"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android.txt"),
                    "proguard-rules.pro"
                )
            )
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    lintOptions {
        isAbortOnError = true
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":feedbacktree"))
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.core:core-ktx:1.3.2")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    implementation("com.google.android.material:material:1.2.0-alpha01")

    // RxJava/Kotlin
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")

    // RxBinding
    implementation("com.jakewharton.rxbinding3:rxbinding:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-core:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-appcompat:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-drawerlayout:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-leanback:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-recyclerview:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-slidingpanelayout:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-swiperefreshlayout:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-viewpager:3.0.0")
    implementation("com.jakewharton.rxbinding3:rxbinding-material:3.0.0")
}
