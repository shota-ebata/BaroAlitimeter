// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        // oss-licenses-pluginはpluginsを使えないっぽい
        // https://stackoverflow.com/questions/32352816/what-the-difference-in-applying-gradle-plugin
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    // TargetSDKと最低Version https://developer.android.com/build/releases/gradle-plugin?hl=ja
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
    id("com.google.firebase.crashlytics") version "2.9.8" apply false
}