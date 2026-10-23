plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

dependencies {
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
}

android {
    namespace = "tv.allinonetv.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "tv.allinonetv.player"
        minSdk = 23
        targetSdk = 28
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
