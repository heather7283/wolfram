plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.serialization)
}

android {
    namespace = "io.github.heather7283.wolfram"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.heather7283.wolfram"
        minSdk = 28
        targetSdk = 36

        // https://developer.android.com/studio/publish/versioning#versioningsettings
        // max is 2100000000 (2^30 < 2100000000 < 2^31 so we can use 30 bits for version)
        // 8 + 8 + 8 + 6 = 30
        val (major, minor, patch, build) = arrayOf(0, 1, 0, 0)
        versionCode = (major shl 22) or (minor shl 14) or (patch shl 6) or (build and 0b00111111)
        versionName = "${major}.${minor}.${patch}" + if (build > 0) "-${build}" else ""

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }
    buildTypes {
        release {
            // https://developer.android.com/reference/kotlin/androidx/compose/material/icons
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
    buildFeatures {
        compose = true
        // https://stackoverflow.com/a/76124393
        buildConfig = true
    }
    packaging {
        jniLibs {
            // otherwise libxray.so doesn't embed properly for whatever reason
            useLegacyPackaging = true
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }
}

room {
    schemaDirectory("${rootDir}/app/src/main/roomSchemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.ktx)
    implementation(libs.cardview.v7)
    implementation(libs.okhttp)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.arrow.core)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.timber)
    implementation(libs.hilt.android.core)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist)
    ksp(libs.kotlinMetadataJvm)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
}

// fixes the stupid duplicate classes nonsense, do not remove
configurations.all {
    exclude(group = "com.intellij", module = "annotations")
}
