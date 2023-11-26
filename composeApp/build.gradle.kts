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
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.ktor.client.android)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)

            implementation(libs.kamel.image)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktorfit)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.json)

            api(libs.mvvm.compose)
            api(libs.mvvm.flow.compose)

            implementation(project.dependencies.platform(libs.koin.annotations.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.annotations)
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

    defaultConfigs {
        // non-flavored defaultConfigs must be provided.
    }

    val baseUrlKey = "baseUrl"
    targetConfigs("dev") {
        create("android") {
            buildConfigField(STRING, baseUrlKey, "http://192.168.227.5:8080/", const = true)
        }
        create("iosArm64") {
            buildConfigField(STRING, baseUrlKey, "http://192.168.227.5:8080/", const = true)
        }
        create("iosX64") {
            buildConfigField(STRING, baseUrlKey, "http://0.0.0.0:8080/", const = true)
        }
        create("iosSimulatorArm64") {
            buildConfigField(STRING, baseUrlKey, "http://0.0.0.0:8080/", const = true)
        }
    }

    defaultConfigs("prod") {
        buildConfigField(STRING, baseUrlKey, "https://kommunicator.pich.ovh/", const = true)
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
        getByName("release") {
            isMinifyEnabled = false
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
