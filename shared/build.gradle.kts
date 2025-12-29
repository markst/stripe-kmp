plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    androidTarget()

    swiftPMDependencies {
        localPackage(
            path = projectDir.resolve("StripePaymentsBridge").canonicalFile,
            products = listOf("StripePaymentsBridge")
        )
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
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
