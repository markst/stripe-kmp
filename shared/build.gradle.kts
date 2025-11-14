plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.spmForKmp)
}

kotlin {
    androidTarget {

    }
    listOf(
        // iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.compilations {
            val main by getting {
                // Create the cinterop for bridging Swift/Objective-C to Kotlin
                cinterops.create("nativeIosShared")
            }
        }
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        
        val androidMain by getting {
            dependencies {
                // Official Stripe Android SDK
                implementation(libs.stripe.android)
                implementation(libs.androidx.appcompat)
            }
        }
    }
}

android {
    namespace = "com.fouroneone.stripe"
    compileSdk = 34
    
    defaultConfig {
        minSdk = 24
    }
}

// SPM4KMP configuration for Swift Package Manager
swiftPackageConfig {
    create("nativeIosShared") { // Must match cinterops.create name
        // Minimum platform versions
        minIos = "15.0"  // Stripe iOS SDK 25.x requires iOS 15+

        // Add Stripe iOS SDK as a dependency
        // With exportToKotlin = true, we can directly use Stripe classes in Kotlin
        // Using v23 for better Objective-C interop
        dependency {
            remotePackageVersion(
                url = uri("https://github.com/stripe/stripe-ios.git"),
                version = "25.0.1",
                products = {
                    add("StripePaymentSheet", exportToKotlin = true)
                    add("StripeApplePay", exportToKotlin = true)
                },
            )
        }

        // Optional: Add debug logging
        debug = true
    }
}
