plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

android {
    namespace = "com.jakting.duolingo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jakting.duolingo"
        minSdk = 29
        targetSdk = 35
        versionCode = 20261013
        versionName = "2.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // 基础依赖
    implementation(libs.yukihookapi.api)
    ksp(libs.yukihookapi.ksp)
    compileOnly(libs.xposed.api)
}