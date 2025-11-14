plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.spmForKmp)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.compilations {
            val main by getting {
                // Create the cinterop for bridging Swift/Objective-C to Kotlin
                cinterops.create("StripePaymentsBridge")
            }
        }
        it.binaries.framework {
            baseName = "StripeKMP"
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
                implementation(libs.androidx.lifecycle.runtime)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        val iosArm64Main by getting {
            dependsOn(iosMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(iosMain)
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
    create("StripePaymentsBridge") { // Must match cinterops.create name
        // Minimum platform versions
        minIos = "13.0"  // Stripe iOS SDK 23.x requires iOS 13+

        // Add Stripe iOS SDK as a dependency
        // With exportToKotlin = true, we can directly use Stripe classes in Kotlin
        // Using v23 for better Objective-C interop
        dependency {
            remotePackageVersion(
                url = uri("https://github.com/stripe/stripe-ios.git"),
                version = "23.29.1",
                products = {
                    add("StripePayments", exportToKotlin = true)
                },
            )
        }

        // Optional: Add debug logging
        debug = true
    }
}
