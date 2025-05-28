plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.groink.reservationapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.groink.reservationapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3) // Material 3
    implementation("androidx.navigation:navigation-compose:2.7.5") // Use a known stable version or your desired one
    implementation("io.coil-kt:coil-compose:2.4.0")
    // Temporarily comment out other dependencies to isolate the issue
    // implementation "androidx.compose.ui:ui:$compose_version"
    // implementation "androidx.compose.ui:ui-tooling-preview:$compose_version"
    // implementation "androidx.compose.material3:material3:1.1.2" // This is an older M3, libs.androidx.material3 is likely newer
    // implementation "androidx.activity:activity-compose:1.8.0" // libs.androidx.activity.compose is likely newer
    // implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2" // libs.androidx.lifecycle.runtime.ktx is likely newer

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}