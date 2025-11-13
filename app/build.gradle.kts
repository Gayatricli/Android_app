plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.gms.google-services")
}


android {
    namespace = "com.example.stressease"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stressease"
        minSdk = 30
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

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-P")
            freeCompilerArgs.add("plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true")
        }
    }

    kotlinOptions {
        // 🔧 Important for Compose — use JVM 17, not 11
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    configurations.all {
        exclude(group = "com.android.support")
        exclude(group = "android.support")
    }
}

dependencies {
    // Core Android dependencies

    implementation(libs.core.ktx.v1120)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.activity.ktx.v182)
    implementation(libs.constraintlayout.v220)
    implementation(libs.mhiew.android.pdf.viewer)
    implementation(libs.androidx.recyclerview)
    implementation(libs.material.v1120)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Charts
    implementation(libs.mpandroidchart)

    // Lottie Animations
    implementation(libs.lottie)

    // Kotlin Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Firebase (with BOM for auto-versioning)
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Compose UI core setup
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.compose.ui:ui:1.7.0")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.0")
    implementation(libs.androidx.cardview)
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.0")

    // Compose Icons
    implementation("androidx.compose.material:material-icons-core:1.6.8")
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

    // Debugging tools for Compose
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.compose.material3:material3:1.1.0-alpha05")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")



}
