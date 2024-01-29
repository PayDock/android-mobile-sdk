/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

/*
 * Created by Paydock on 7/6/23, 5:02 PM
 * Copyright (c) 2023 Lasting. All rights reserved.
 *
 * Last modified 7/5/23, 11:59 AM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.paydock.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.paydock.sample"
        minSdk = 24
        targetSdk = 34
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
            "PUBLIC_KEY",
            getPropertyValue("PUBLIC_KEY")
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
            "MERCHANT_IDENTIFIER",
            getPropertyValue("MERCHANT_IDENTIFIER")
        )

        buildConfigField(
            "String",
            "STANDALONE_3DS_SERVICE_ID",
            getPropertyValue("STANDALONE_3DS_SERVICE_ID")
        )
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
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // Allow references to generated code
    kapt {
        correctErrorTypes = true
    }
}

fun getPropertyValue(propertyName: String): String {
    val envValue = System.getenv(propertyName)
    if (envValue != null) {
        return envValue
    }
    val localProperties = gradleLocalProperties(rootDir)
    return localProperties.getProperty(propertyName) ?: ""
}

dependencies {
    // Modules
    implementation(project(":mobile-sdk"))
    // Libraries
    implementation(libs.bundles.androidx)
    implementation(platform(libs.kotlin.bom))
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose.sample.app)
    debugImplementation(libs.bundles.compose.debug)
    // Hilt
    implementation(libs.bundles.hilt)
    kapt(libs.hilt.android.compiler)
    // Retrofit
    implementation(libs.bundles.retrofit)
    // Test Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit)
}
