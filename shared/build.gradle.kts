@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "StripePayments"

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    swiftPMDependencies {
        iosMinimumDeploymentTarget = "15.0"

        localSwiftPackage(
            directory = layout.projectDirectory.dir("StripePaymentsBridge"),
            products = listOf("StripePaymentsBridge"),
        )
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    compilerOptions {
        optIn.add("kotlinx.cinterop.ExperimentalForeignApi")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(compose.runtime)
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Official Stripe Android SDK
                implementation(libs.stripe.android)
                implementation(libs.androidx.appcompat)
                implementation(compose.ui)
                implementation(compose.material3)
            }
        }

    }
}

android {
    namespace = "com.fouroneone.stripe"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }
}
