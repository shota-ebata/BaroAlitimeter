import java.util.Properties
import java.io.FileInputStream
import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.ebata_shota.baroalitimeter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ebata_shota.baroalitimeter"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("./keystore/debug.keystore")
            storePassword = "qwer1234"
            keyAlias = "debug"
            keyPassword = "qwer1234"
        }
        create("release") {
            val signFileBase = file("./../key_store_info.properties")
            if (signFileBase.exists()) {
                val signingProps = Properties()
                signingProps.load(FileInputStream(signFileBase))
                val storeFilePathValue = signingProps["storeFilePathValue"]
                storeFile = file(storeFilePathValue!!)
            } else {
                val releaseKeystoreFileName = "release-keystore.keystore"
                System.getenv("SHOTA_EBATA_KEY_STORE_BASE64")?.let { base64 ->
                    val decoder = Base64.getMimeDecoder()
                    File(releaseKeystoreFileName).also { file ->
                        file.createNewFile()
                        file.writeBytes(decoder.decode(base64))
                    }
                }
                storeFile = rootProject.file(releaseKeystoreFileName)
            }
            storePassword = System.getenv("SHOTA_EBATA_KEY_STORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS_VALUE")
            keyPassword = System.getenv("KEY_PASSWORD_VALUE")
        }
    }

    buildTypes {
        getByName("debug") {
            isShrinkResources = false
            isMinifyEnabled = false
            isDebuggable =  true
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["appName"] = "debug気圧高度計"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders["appName"] = "気圧高度計"
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
        compose = true
    }
    composeOptions {
        // Kotlinとの互換性
        // https://developer.android.com/jetpack/androidx/releases/compose-kotlin?hl=ja#pre-release_kotlin_compatibility
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        // unitTests.returnDefaultValues = true
        // 参考: https://developer.android.com/training/testing/unit-testing/local-unit-tests?hl=ja
        //  android.jarの中を呼んだ場合に、例外ではなくデフォルト値を返すようにする。
        // 注意: returnDefaultValues プロパティを true に設定する場合は注意が必要です。
        //  戻り値を null やゼロにすることで、テストの有効性が落ちる場合があります。
        //  この場合、デバッグが難しくなり、失敗するはずのテストが成功する可能性もあります。
        //  最終手段としてのみ使用してください。
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.10"))

    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    implementation("androidx.activity:activity-compose:1.8.2")

    // compose-bom
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.robolectric:robolectric:4.11.1")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // PreferencesDataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // FirebaseBom
    implementation(platform("com.google.firebase:firebase-bom:32.2.2"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // OSS Licenses
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.1")

}

kapt {
    correctErrorTypes = true
}