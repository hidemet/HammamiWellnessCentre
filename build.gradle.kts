
// Top-level build file where you can add configuration options common to all sub-projects/modules.


buildscript {
    val nav_version = "2.5.0"
    dependencies {
        classpath("com.google.gms:google-services:4.3.13")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
    repositories {
        google()
    }
}


plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}