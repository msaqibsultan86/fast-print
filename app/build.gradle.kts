import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Google OAuth Web client ID is read from local.properties (never committed).
// See SETUP.md.  Falls back to empty -> app runs, Google button shows a setup hint.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val googleWebClientId: String = localProps.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""

android {
    namespace = "com.notzyvex.fastprint"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.notzyvex.fastprint"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        resValue("string", "google_web_client_id", googleWebClientId)
    }

    // One signing identity for debug and release. In-app updates require it: Android rejects
    // an update signed with a different key than the installed build, and CI's own throwaway
    // debug keystore changes every run. It is also the SHA-1 registered with Google Sign-In.
    val storeFilePath = localProps.getProperty("RELEASE_STORE_FILE")
    val storePass = localProps.getProperty("RELEASE_STORE_PASSWORD")
    val keyAliasProp = localProps.getProperty("RELEASE_KEY_ALIAS")
    // Every field must be present. A half-filled config would fail the build at signing
    // time with a confusing error, so fall back to the debug key and warn instead.
    val hasSigning = !storeFilePath.isNullOrBlank() &&
        !storePass.isNullOrBlank() &&
        !keyAliasProp.isNullOrBlank() &&
        rootProject.file(storeFilePath).exists()

    if (!hasSigning) {
        logger.warn(
            "Fast Print: no signing config in local.properties — building with the debug key. " +
                "Google Sign-In will fail (registered SHA-1 won't match) and in-app updates " +
                "will be rejected as signature-incompatible."
        )
    }

    signingConfigs {
        if (hasSigning) {
            create("fastprint") {
                storeFile = rootProject.file(storeFilePath!!)
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            if (hasSigning) signingConfig = signingConfigs.getByName("fastprint")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasSigning) signingConfig = signingConfigs.getByName("fastprint")
        }
    }

    // fastprint-1.0.0.apk / fastprint-1.0.0-debug.apk
    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val suffix = if (variant.buildType.name == "release") "" else "-${variant.buildType.name}"
            output.outputFileName = "fastprint-${variant.versionName}$suffix.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play)
    implementation(libs.google.identity.googleid)

    implementation(libs.coil.compose)
    implementation(libs.androidx.exifinterface)
}
