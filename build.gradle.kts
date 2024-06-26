
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
  dependencies {
      classpath ("com.android.tools.build:gradle:8.3.2")
      classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
      classpath ("com.google.gms:google-services:4.4.2")
      //safe args
      val nav_version = "2.5.0"
      classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
      classpath ("com.google.dagger:hilt-android-gradle-plugin:2.44")
 }
}

