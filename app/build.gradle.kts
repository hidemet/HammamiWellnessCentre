plugins {
    id("kotlin-android")
    id("kotlin-kapt")
    id("com.android.application")
    id ("com.google.gms.google-services")
    id ("kotlin-parcelize")
    id ("androidx.navigation.safeargs.kotlin")
    id ("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.hammami"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.example.hammami"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.androidx.material3.android)




    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    // Material Design 3
    implementation(libs.material)


    //implementation("com.google.android.material:material-icons-core:1.6.7")
    //implementation("com.google.android.material:material-icons-extended:1.6.7")

    //loading button
    //implementation("br.com.simplepass:loading-button-android:2.2.0")

    //Glide
    implementation(libs.glide)

    //circular image
    implementation(libs.circleimageview)

    //viewpager2 indicatior
    //implementation("io.github.vejei.viewpagerindicator:viewpagerindicator:1.0.0-alpha.1")

    //stepView
    //implementation("com.shuhart.stepview:stepview:1.5.1")

    //Android Ktx
    implementation(libs.androidx.navigation.fragment.ktx)

    //Dagger hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    // Firebase
    implementation(libs.firebase.auth)
    //implementation(platform("com.google.firebase:firebase-bom:32.0.0")) // Update to the latest BOM version
    implementation("com.google.firebase:firebase-firestore-ktx") // Add Firestore dependency

    //Coroutines with firebase
    implementation(libs.kotlinx.coroutines.play.services)

    //Navigation component
    val nav_version = "2.5.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Splash screen
    implementation("androidx.core:core-splashscreen:1.0.0")
}
