@file:Suppress("UnstableApiUsage")

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktorfit)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.gms)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.crashlyticslink)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            export(libs.kmpnotifier)
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts += "-ld64"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:32.5.0"))
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.ios)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(libs.kamel.image)

            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.tabNavigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.koin)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.uuid.core)
            implementation(libs.human.readable)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.uuid.sqldelight)
            // https://github.com/cashapp/sqldelight/issues/4357#issuecomment-1839905700
            //noinspection UseTomlInstead
            implementation("co.touchlab:stately-common:2.0.5")

            implementation(libs.ktorfit)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)

            api(libs.kmpnotifier)

            implementation(project.dependencies.platform(libs.koin.annotations.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.annotations)

            implementation(libs.kermit)
            implementation(libs.kermit.crashlytics)
            implementation(libs.firebase.crashlytics)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
    targets.configureEach {
        compilations.configureEach {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

dependencies {
    listOf(
        libs.ktorfit.ksp,
        libs.koin.ksp,
    ).forEach {
        add("kspAndroid", it)

        add("kspIosX64", it)
        add("kspIosArm64", it)
        add("kspIosSimulatorArm64", it)
    }
}

buildkonfig {
    packageName = "io.github.mklkj.kommunicator"

    val baseUrlKey = "BASE_URL"
    val isDebugKey = "IS_DEBUG"
    defaultConfigs {
        buildConfigField(STRING, baseUrlKey, "http://0.0.0.0:8080/", const = true)
        buildConfigField(BOOLEAN, isDebugKey, "true", const = true)
    }

    targetConfigs("dev") {
        create("android") {
            buildConfigField(STRING, baseUrlKey, "http://192.168.227.5:8080/", const = true)
            // host loopback in android emulator
//            buildConfigField(STRING, baseUrlKey, "http://10.0.2.2:8080/", const = true)
        }
        create("iosArm64") {
            buildConfigField(STRING, baseUrlKey, "http://192.168.227.5:8080/", const = true)
        }
        create("iosX64")
        create("iosSimulatorArm64")
    }

    defaultConfigs("prod") {
        buildConfigField(STRING, baseUrlKey, "https://kommunicator.pich.ovh/", const = true)
        buildConfigField(BOOLEAN, isDebugKey, "false", const = true)
    }
}

android {
    namespace = "io.github.mklkj.kommunicator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "io.github.mklkj.kommunicator"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "0.1.0"
    }

    signingConfigs {
        create("signing") {
            storeFile = file(extra["PRODUCTION_STORE_FILE"].toString())
            storePassword = extra["PRODUCTION_PASSWORD"].toString()
            keyAlias = extra["PRODUCTION_KEY_ALIAS"].toString()
            keyPassword = extra["PRODUCTION_PASSWORD"].toString()
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("signing")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("signing")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("io.github.mklkj.kommunicator.data.db")
        }
    }
}
