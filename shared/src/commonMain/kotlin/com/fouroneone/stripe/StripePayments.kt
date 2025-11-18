package com.fouroneone.stripe

import androidx.compose.runtime.Composable

/**
 * Main interface for Stripe payment operations
 */
interface StripePayments {
    /**
     * Initialize Stripe with a publishable key
     */
    @Composable
    fun Initialize(publishableKey: String)

    /**
     * Presents the Stripe payment sheet when the content is clicked.
     * This must be a Composable in order to remember the PaymentSheet.Builder instance.
     */
    @Composable
    fun StripeCheckout(
        clientSecret: String,
        onResult: (PaymentSheetResult) -> Unit,
        content: @Composable () -> Unit
    )
}

sealed class PaymentSheetResult {
    object Completed : PaymentSheetResult()
    data class Failed(val error: Throwable) : PaymentSheetResult()
    object Canceled : PaymentSheetResult()
}

@Composable
expect fun rememberStripePayments(): StripePayments