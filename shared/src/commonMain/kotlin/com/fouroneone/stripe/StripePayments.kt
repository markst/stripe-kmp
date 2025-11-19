package com.fouroneone.stripe

import androidx.compose.runtime.Composable

/**
 * Platform-specific context type required to initialize Stripe payments.
 * - On Android: [android.content.Context]
 * - On iOS: Not used (abstract class with no implementations needed)
 */
expect abstract class PlatformContext

/**
 * Main interface for Stripe payment operations across all platforms.
 *
 * This provides a unified API for initializing Stripe and presenting payment sheets
 * on both Android and iOS platforms.
 */
interface StripePayments {
    /**
     * Initialize Stripe with your publishable key.
     *
     * This must be called before building or presenting any payment sheets.
     *
     * @param publishableKey Your Stripe publishable key (starts with `pk_test_` or `pk_live_`)
     */
    fun initialize(publishableKey: String)
}

/**
 * Remembers a payment sheet that can be presented to collect payment information.
 *
 * This Composable function properly handles the Android lifecycle requirements for
 * registering the payment sheet before the Activity reaches STARTED state.
 *
 * ## Platform Behavior:
 * - **Android**: Uses `rememberLauncherForActivityResult` to register before lifecycle STARTED
 * - **iOS**: Captures the `rootViewController` from the current window
 *
 * @param stripePayments The [StripePayments] instance (typically injected via DI)
 * @param onResult Callback invoked when the payment sheet completes, is canceled, or fails
 * @return A [PaymentSheetWrapper] that can be used to present the payment UI
 *
 * ## Usage Example:
 * ```kotlin
 * @Composable
 * fun PaymentScreen(stripePayments: StripePayments = koinInject()) {
 *     val paymentSheet = rememberPaymentSheet(stripePayments) { result ->
 *         when (result) {
 *             is PaymentSheetResult.Completed -> { /* Success */ }
 *             is PaymentSheetResult.Failed -> { /* Error */ }
 *             is PaymentSheetResult.Canceled -> { /* Canceled */ }
 *         }
 *     }
 *
 *     Button(onClick = {
 *         paymentSheet.presentWithPaymentIntent("pi_..._secret_...")
 *     }) {
 *         Text("Pay")
 *     }
 * }
 * ```
 */
@Composable
expect fun rememberPaymentSheet(
    stripePayments: StripePayments,
    onResult: (PaymentSheetResult) -> Unit
): PaymentSheetWrapper

/**
 * Wrapper for a platform-specific payment sheet that handles the presentation of payment UI.
 *
 * This wrapper holds a reference to the platform-specific payment sheet and the UI context
 * needed to present it. Create instances using [rememberPaymentSheet].
 */
interface PaymentSheetWrapper {
    /**
     * Presents the payment sheet to the user with a PaymentIntent.
     *
     * The payment sheet UI will be displayed, allowing the user to enter payment details
     * and complete the payment. The result will be delivered to the callback provided
     * when building this payment sheet.
     *
     * @param clientSecret The client secret from a Stripe PaymentIntent (format: `pi_..._secret_...`)
     * @param configuration Optional configuration for customizing the payment sheet appearance and behavior
     */
    fun presentWithPaymentIntent(
        clientSecret: String,
        configuration: PaymentSheetConfiguration? = null
    )
}

/**
 * Configuration options for customizing the payment sheet.
 *
 * @property merchantDisplayName The name of your business to display in the payment sheet
 * @property customerId The Stripe customer ID if using a saved customer (starts with `cus_`)
 * @property customerEphemeralKeySecret The ephemeral key secret for the customer, required if [customerId] is provided
 * @property allowsDelayedPaymentMethods Whether to allow payment methods that complete after a delay (e.g., bank transfers)
 */
data class PaymentSheetConfiguration(
    val merchantDisplayName: String,
    val customerId: String? = null,
    val customerEphemeralKeySecret: String? = null,
    val allowsDelayedPaymentMethods: Boolean = false
)

/**
 * Represents the result of a payment sheet interaction.
 */
sealed class PaymentSheetResult {
    /**
     * The payment was completed successfully.
     * The payment has been confirmed and will be processed.
     */
    object Completed : PaymentSheetResult()

    /**
     * The payment failed due to an error.
     *
     * @property error The error that caused the payment to fail
     */
    data class Failed(val error: Throwable) : PaymentSheetResult()

    /**
     * The user canceled the payment sheet without completing the payment.
     * No payment was attempted.
     */
    object Canceled : PaymentSheetResult()
}

/**
 * Factory function to create a platform-specific [StripePayments] instance.
 *
 * @param context Platform-specific context:
 *   - **Android**: An [android.content.Context], which will be cast to [ComponentActivity] when building payment sheets
 *   - **iOS**: Not used, pass any [PlatformContext] instance
 * @return A platform-specific implementation of [StripePayments]
 */
expect fun getStripePayments(context: PlatformContext): StripePayments