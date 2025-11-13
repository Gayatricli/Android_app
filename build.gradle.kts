// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Top-level Gradle build file
plugins {
    alias(libs.plugins.android.application) apply false

    alias(libs.plugins.kotlin.android) apply false

}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.25")
        classpath("com.google.gms:google-services:4.4.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://jitpack.io")
    }
}

