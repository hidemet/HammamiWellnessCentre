plugins {
    id("kotlin-android")
    id("com.android.application")
    id ("com.google.gms.google-services")
    id ("kotlin-parcelize")
    id ("androidx.navigation.safeargs.kotlin")
    id ("dagger.hilt.android.plugin")
    kotlin("kapt")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)



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
    implementation("com.google.dagger:hilt-android:2.38.1")
    kapt("com.google.dagger:hilt-android-compiler:2.40.1")

    // Firebase
    implementation(libs.firebase.auth)
    //implementation(platform("com.google.firebase:firebase-bom:32.0.0")) // Update to the latest BOM version
    //implementation("com.google.firebase:firebase-firestore-ktx") // Add Firestore dependency

    //Navigation component
    val nav_version = "2.5.2"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
}
