pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev/")
        maven("https://jitpack.io")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.google.com")
        maven("https://androidx.dev/storage/compose-compiler/repository/")
        maven("https://jitpack.io")
    }
}

rootProject.name = "StressEase"
include(":app")
