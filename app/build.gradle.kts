plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.hilt)
    // alias(libs.plugins.ksp) // Uncomment if using KSP for Hilt
    kotlin("kapt") // Use kapt OR ksp for Hilt
}

android {
    namespace = "com.example.androcode" // Change to your desired package name
    compileSdk = 34 // Target latest stable SDK

    defaultConfig {
        applicationId = "com.example.androcode" // Match namespace or customize
        minSdk = 28 // Consider target audience - API 28+ reasonable for foldables
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Enable R8/ProGuard
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
             // Add specific debug configurations if needed
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 // Use Java 17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17" // Match Java version
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    lint {
        disable.add("CoroutineCreationDuringComposition")
        disable.add("FlowOperatorInvokedInComposition")
        disable.add("StateFlowValueCalledInComposition")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.google.android.material) // Add view-based Material Components
    implementation(libs.timber)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler) // OR ksp(libs.hilt.compiler.ksp)

    // Hilt Navigation Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // DocumentFile for SAF
    implementation(libs.androidx.documentfile)

    // Compose RichText for syntax highlighting
    implementation("com.halilibo.compose-richtext:richtext-ui:0.16.0")
    implementation("com.halilibo.compose-richtext:richtext-ui-material3:0.16.0")
    implementation("com.halilibo.compose-richtext:richtext-commonmark:0.16.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Allow references to generated code (Hilt)
kapt {
    correctErrorTypes = true
}
