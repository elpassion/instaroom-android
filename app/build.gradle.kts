import groovy.lang.Closure

val kotlinVersion: String by project

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
    id("io.fabric")
    id("com.google.gms.google-services")
}
apply("version.gradle")

android {
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "pl.elpassion.instaroom"
        minSdkVersion(23)
        targetSdkVersion(28)

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

    testOptions {
        unitTests.apply {
            all(closureOf<Test> {
                useJUnitPlatform()
            } as Closure<Test>)
        }
    }
}

dependencies {
    val lifecycleVersion = "2.0.0"
    val androidCommonsVersion = "0.0.23"
    val coroutinesVersion = "1.0.1"

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:$coroutinesVersion")
    implementation("androidx.core:core-ktx:1.0.0-alpha1")
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("android.arch.navigation:navigation-fragment-ktx:1.0.0-alpha09")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-extensions:$lifecycleVersion")
    implementation("org.koin:koin-android-viewmodel:1.0.2")
    implementation("com.google.android.gms:play-services-auth:16.0.1")
    implementation("com.github.elpassion.android-commons:shared-preferences:$androidCommonsVersion")
    implementation("com.github.elpassion.android-commons:shared-preferences-moshi-converter-adapter:$androidCommonsVersion")
    implementation("com.github.elpassion.android-commons:recycler:$androidCommonsVersion")
    implementation("com.squareup.moshi:moshi-kotlin:1.8.0")
    implementation("com.squareup.retrofit2:retrofit:2.5.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.5.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    implementation("com.google.android.material:material:1.1.0-alpha02")
    implementation("io.reactivex.rxjava2:rxkotlin:2.3.0")
    implementation("com.jakewharton.rxrelay2:rxrelay:2.1.0")
    implementation("com.jakewharton.threetenabp:threetenabp:1.1.1")
    implementation("com.shopify:livedata-ktx:2.0.1")
    implementation("com.jakewharton.rxbinding3:rxbinding-core:3.0.0-alpha2")
    implementation("com.jakewharton.rxbinding3:rxbinding:3.0.0-alpha2")
    implementation("com.jakewharton.rxbinding3:rxbinding-material:3.0.0-alpha2")
    
    implementation("com.github.elpassion:instaroom:b825146c6d")

    implementation("com.google.firebase:firebase-core:16.0.7")
    implementation("com.crashlytics.sdk.android:crashlytics:2.9.9")
    implementation("com.markodevcic.peko:peko:1.0.0")

    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testImplementation("org.threeten:threetenbp:1.3.7")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.2.1")
    testImplementation("org.slf4j:slf4j-nop:1.7.25")
    testImplementation("com.jraska.livedata:testing-ktx:1.0.0")

    androidTestImplementation("androidx.test:runner:1.1.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.1.1") {
        exclude(group = "com.google.code.findbugs")
    }
}


