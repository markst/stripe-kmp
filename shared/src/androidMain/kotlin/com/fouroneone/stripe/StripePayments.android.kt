package com.fouroneone.stripe

import android.content.Context
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.model.CardParams
import com.stripe.android.model.PaymentMethodCreateParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun createStripePayments(): StripePayments = StripePaymentsAndroid()

class StripePaymentsAndroid : StripePayments {
    override suspend fun initialize(publishableKey: String) {

    }
}
