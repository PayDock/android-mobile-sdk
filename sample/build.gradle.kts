import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.devtools)
    alias(libs.plugins.dagger.hilt)
}

android {
    namespace = "com.paydock.sample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.paydock.sample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // These values are added from local.properties
        buildConfigField(
            "String",
            "SECRET_KEY",
            getPropertyValue("SECRET_KEY")
        )

        buildConfigField(
            "String",
            "ACCESS_TOKEN",
            getPropertyValue("ACCESS_TOKEN")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID",
            getPropertyValue("GATEWAY_ID")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID_PAY_PAL",
            getPropertyValue("GATEWAY_ID_PAY_PAL")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID_FLY_PAY",
            getPropertyValue("GATEWAY_ID_FLY_PAY")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID_AFTER_PAY",
            getPropertyValue("GATEWAY_ID_AFTER_PAY")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID_GOOGLE_PAY",
            getPropertyValue("GATEWAY_ID_GOOGLE_PAY")
        )

        buildConfigField(
            "String",
            "GATEWAY_ID_MASTERCARD_SRC",
            getPropertyValue("GATEWAY_ID_MASTERCARD_SRC")
        )

        buildConfigField(
            "String",
            "MERCHANT_IDENTIFIER",
            getPropertyValue("MERCHANT_IDENTIFIER")
        )

        buildConfigField(
            "String",
            "STANDALONE_3DS_SERVICE_ID",
            getPropertyValue("STANDALONE_3DS_SERVICE_ID")
        )

        buildConfigField(
            "String",
            "FLY_PAY_CLIENT_ID",
            getPropertyValue("FLY_PAY_CLIENT_ID")
        )
    }
    signingConfigs {
        create("debugtest") {
            storeFile = file(System.getProperty("user.home") + "/.android/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debugtest")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        // https://developer.android.com/jetpack/androidx/releases/compose-compiler
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtension.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getPropertyValue(propertyName: String, defaultValue: String = ""): String {
    val envValue = System.getenv(propertyName)
    if (envValue != null) {
        return envValue
    }
    val localProperties = gradleLocalProperties(rootDir, providers)
    return localProperties.getProperty(propertyName) ?: defaultValue
}

dependencies {
    // Modules
    implementation(project(":mobile-sdk"))
    // Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.ktx)
    implementation(platform(libs.kotlin.bom))
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.accompanist.system.ui)
    // To allow builds to build for release
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.ui.tooling)
    // Hilt
    implementation(libs.bundles.hilt)
    ksp(libs.hilt.android.compiler)
    // Retrofit
    implementation(libs.bundles.retrofit)
    // Test Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
