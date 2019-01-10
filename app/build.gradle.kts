val kotlinVersion: String by project

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "pl.elpassion.instaroom"
        minSdkVersion(23)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("src/debug/debug.keystore")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    val ankoVersion = "0.10.7"
    val lifecycleVersion = "2.0.0"
    val androidCommonsVersion = "0.0.23"
    val coroutinesVersion = "1.0.1"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$coroutinesVersion")
    implementation("androidx.core:core-ktx:1.0.0-alpha1")
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0-alpha08")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("org.koin:koin-android-viewmodel:1.0.2")
    implementation("com.google.android.gms:play-services-auth:16.0.1")
    implementation("com.github.elpassion.android-commons:shared-preferences:$androidCommonsVersion")
    implementation("com.github.elpassion.android-commons:shared-preferences-moshi-converter-adapter:$androidCommonsVersion")
    implementation("com.github.elpassion.android-commons:recycler:$androidCommonsVersion")
    implementation("com.squareup.moshi:moshi-kotlin:1.7.0")
    implementation("com.squareup.retrofit2:retrofit:2.4.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.4.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("org.jetbrains.anko:anko-appcompat-v7:$ankoVersion")
    implementation("com.google.android.material:material:1.0.0-beta01")
    implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.1.0")

    testImplementation("junit:junit:4.12")

    androidTestImplementation("androidx.test:runner:1.1.0-alpha4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.0-alpha4")
}
