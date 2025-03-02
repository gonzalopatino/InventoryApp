plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zybooks.inventorymapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.zybooks.inventorymapp"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    // AndroidX libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.7.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")


    implementation("com.google.android.material:material:1.11.0")

    // **JUnit 4 for Local Unit Tests**
    testImplementation("junit:junit:4.13.2")

    // **Android Instrumented Tests**
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // ✅ Provides AndroidJUnit4
    androidTestImplementation("androidx.test:runner:1.5.2")    // ✅ Required for instrumentation
    androidTestImplementation("androidx.test:rules:1.5.0")     // ✅ Ensures AndroidX test rules
    androidTestImplementation("androidx.test:core:1.5.0")      // ✅ For ApplicationProvider
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}
