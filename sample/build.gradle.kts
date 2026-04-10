import com.android.build.api.dsl.ApplicationProductFlavor
import com.android.build.api.dsl.ProductFlavor
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp.devtools)
    alias(libs.plugins.dagger.hilt)
}

val deployVersionName: String = project.findProperty("versionName") as String? ?: "1.0.0"

android {
    namespace = "com.paydock.sample"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.paydock.sample"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = deployVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
            isDefault = true
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
    flavorDimensions += "environment"
    productFlavors {
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            configureFlavorBuildConfig(
                "com.paydock.core.domain.model.Environment.STAGING",
                true,
                "staging"
            )
        }
        create("sandbox") {
            isDefault = true
            dimension = "environment"
            applicationIdSuffix = ".sandbox"
            versionNameSuffix = "-sandbox"
            configureFlavorBuildConfig(
                "com.paydock.core.domain.model.Environment.SANDBOX",
                true,
                "sandbox"
            )
        }
        create("prod") {
            dimension = "environment"
            configureFlavorBuildConfig(
                "com.paydock.core.domain.model.Environment.PRODUCTION",
                false,
                "prod",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

enum class BuildVariable(private val baseEnvName: String) {
    // Shared Variables
    MERCHANT_ID_GOOGLE_PAY("MERCHANT_ID_GOOGLE_PAY"),
    WALLET_ID_COLES_PAY("WALLET_ID_COLES_PAY"),

    // Environment Specific Variables
    ACCESS_TOKEN_WIDGET("ACCESS_TOKEN_WIDGET"),
    ACCESS_TOKEN_API("ACCESS_TOKEN_API"),
    SERVICE_ID_MPGS("SERVICE_ID_MPGS"),
    SERVICE_ID_MPGS_TEST("SERVICE_ID_MPGS_TEST"),
    SERVICE_ID_CYBERSOURCE("SERVICE_ID_CYBERSOURCE"),
    SERVICE_ID_PAYPAL("SERVICE_ID_PAYPAL"),
    SERVICE_ID_COLES_PAY("SERVICE_ID_COLES_PAY"),
    SERVICE_ID_AFTERPAY("SERVICE_ID_AFTERPAY"),
    SERVICE_ID_CLICK_TO_PAY("SERVICE_ID_CLICK_TO_PAY"),
    SERVICE_ID_GOOGLE_PAY("SERVICE_ID_GOOGLE_PAY"),
    // To be removed
    SERVICE_ID_GPAYMENTS("SERVICE_ID_GPAYMENTS"),
    SERVICE_ID_ZIP("SERVICE_ID_ZIP");

    fun getEnvName(flavor: String): String {
        return when {
            this == MERCHANT_ID_GOOGLE_PAY -> this.baseEnvName
            this == WALLET_ID_COLES_PAY -> this.baseEnvName
            flavor.isEmpty() -> this.baseEnvName
            else -> "${this.baseEnvName}_${flavor.uppercase()}"
        }
    }
}

fun ApplicationProductFlavor.configureFlavorBuildConfig(
    sdkEnvironment: String,
    enableTestMode: Boolean,
    flavor: String
): ApplicationProductFlavor {
    return this.apply {
        addBuildConfigField(
            "com.paydock.core.domain.model.Environment",
            "SDK_ENVIRONMENT",
            sdkEnvironment
        )
        addBuildConfigField("Boolean", "ENABLE_TEST_MODE", enableTestMode.toString())
        BuildVariable.values().forEach { variable ->
            addBuildConfigField("String", variable.name, readBuildVariable(variable, flavor))
        }
    }
}

fun readBuildVariable(variable: BuildVariable, flavor: String): String {
    val envName = variable.getEnvName(flavor)
    var envValue = System.getenv(envName)?.trim()
    val rawValue = if (!envValue.isNullOrEmpty()) {
        envValue
    } else {
        val props = getLocalConfigProps(flavor)
        props.getProperty(variable.name) ?: ""
    }
    // Strip surrounding quotes (config.properties uses key="value", env vars may too)
    val trimmed = if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
        rawValue.substring(1, rawValue.length - 1)
    } else {
        rawValue
    }
    return "\"$trimmed\""
}

fun getLocalConfigProps(flavor: String): Properties {
    val props = Properties()
    val configPath = "src/$flavor/config.properties"
    val propsFile = file(configPath)
    if (propsFile.exists()) {
        try {
            FileInputStream(propsFile).use {
                props.load(it)
            }
        } catch (e: Exception) {
            println("Error loading properties from $configPath: ${e.message}")
        }
    } else {
        println("Properties file not found: $configPath")
    }
    return props
}

fun ProductFlavor.addBuildConfigField(type: String, name: String, value: String) =
    buildConfigField(type, name, value)

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
