# Stripe Payments for Kotlin Multiplatform

A Kotlin Multiplatform library for integrating Stripe payment processing on Android and iOS
platforms.

## Features

- ✅ Unified API for Android and iOS
- ✅ Native Stripe SDK integration on both platforms
- ✅ No Composable dependency in the library (works with any Compose UI)
- ✅ Type-safe payment result handling
- ✅ Support for customer configuration and saved payment methods
- ✅ Works seamlessly with dependency injection frameworks (Koin, Hilt, etc.)

## Supported Platforms

- **Android**: API 24+ (Android 7.0 Nougat and above)
- **iOS**: iOS 15.0+ (via Stripe iOS SDK 25.x)

## Installation

This library can be used as a Git submodule. Add it to your project and configure it in
`settings.gradle.kts`:

```kotlin
include(":stripe-kmp")
project(":stripe-kmp").projectDir = file("./submodule/stripe/shared")
```

Then add the dependency to your shared module:

```kotlin
commonMain.dependencies {
    implementation(project(":stripe-kmp"))
}
```

## Setup with Dependency Injection

### Using Koin

```kotlin
// In your shared DI module
fun stripeModule() = module {
    single<StripePayments> { 
        getStripePayments(get()) // Koin will inject the platform context
    }
}

// Platform-specific setup
// Android - in your Application class or MainActivity:
startKoin {
    modules(
        module {
            single<PlatformContext> { applicationContext }
        },
        stripeModule()
    )
}

// iOS - in your app setup:
// PlatformContext is not used on iOS, pass any instance
```

## Usage in Compose

Here's a complete example using Koin for dependency injection:

```kotlin
@Composable
fun PaymentScreen(
    stripePayments: StripePayments = koinInject()
) {
    // Remember payment sheet - handles lifecycle registration properly
    val paymentSheet = rememberPaymentSheet(stripePayments) { result ->
        when (result) {
            is PaymentSheetResult.Completed -> { /* Success */ }
            is PaymentSheetResult.Failed -> { /* Error */ }
            is PaymentSheetResult.Canceled -> { /* Canceled */ }
        }
    }
    
    Button(onClick = {
        paymentSheet.presentWithPaymentIntent("pi_..._secret_...")
    }) {
        Text("Pay")
    }
}
```

**Note**: Initialize Stripe once at app startup, not in Composables:

```kotlin
// In your Application.onCreate() or main app initialization
stripePayments.initialize("pk_test_...")
```

## Platform-Specific Details

### Android

- Requires a `Context` that is or can be cast to a `ComponentActivity`
- Uses the official [Stripe Android SDK](https://github.com/stripe/stripe-android)
- The payment sheet is presented as a bottom sheet
- **DI Setup**: Provide `applicationContext` as `PlatformContext`

### iOS

- Automatically captures the root view controller from the current window
- Uses the official [Stripe iOS SDK](https://github.com/stripe/stripe-ios)
- The payment sheet is presented as a modal view controller
- **DI Setup**: `PlatformContext` is not used; pass any dummy instance

## API Reference

### `StripePayments`

Main interface for Stripe payment operations.

#### Methods

- `initialize(publishableKey: String)` - Initialize Stripe with your publishable key

### `rememberPaymentSheet`

Composable function to create and remember a payment sheet.

```kotlin
@Composable
fun rememberPaymentSheet(
    stripePayments: StripePayments,
    onResult: (PaymentSheetResult) -> Unit
): PaymentSheetWrapper
```

This function properly handles the Android lifecycle requirements for registering the payment sheet
before the Activity reaches STARTED state. Must be called from a Composable context.

### `PaymentSheetWrapper`

Wrapper for presenting the payment UI.

#### Methods

- `presentWithPaymentIntent(clientSecret: String, configuration: PaymentSheetConfiguration?)` -
  Present the payment sheet

### `PaymentSheetResult`

Sealed class representing payment results:

- `Completed` - Payment successful
- `Failed(error: Throwable)` - Payment failed with error
- `Canceled` - User canceled the payment

### `PaymentSheetConfiguration`

Configuration options:

- `merchantDisplayName: String` - Your business name (required)
- `customerId: String?` - Stripe customer ID for saved payment methods
- `customerEphemeralKeySecret: String?` - Ephemeral key for customer
- `allowsDelayedPaymentMethods: Boolean` - Allow delayed payment methods (default: false)

## Error Handling

Always handle payment failures gracefully:

```kotlin
val paymentSheet = rememberPaymentSheet(stripePayments) { result ->
    when (result) {
        is PaymentSheetResult.Failed -> {
            // Log error for debugging
            Logger.e("Payment failed", result.error)
            
            // Show user-friendly message
            showError("Payment could not be processed. Please try again.")
        }
        // ... handle other cases
    }
}
```

## Requirements

### Android

- Minimum SDK: 24 (Android 7.0)
- Compile SDK: 36
- Requires `ComponentActivity` context

### iOS

- Minimum iOS version: 15.0
- Uses Stripe iOS SDK 25.0.1

## License

[Add your license here]

## Support

[Add support information here]
