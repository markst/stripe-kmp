package com.fouroneone.stripe

/**
 * Main interface for Stripe payment operations
 */
interface StripePayments {
    /**
     * Initialize Stripe with a publishable key
     */
    suspend fun initialize(publishableKey: String)
}

/**
 * Factory function to create a platform-specific Stripe instance
 */
expect fun createStripePayments(): StripePayments
