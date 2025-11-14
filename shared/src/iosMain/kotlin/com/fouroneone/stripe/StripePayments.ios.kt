package com.fouroneone.stripe

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import StripePaymentSheet.LinkPaymentController

actual fun createStripePayments(): StripePayments = StripePaymentsIOS()

@OptIn(ExperimentalForeignApi::class)
class StripePaymentsIOS : StripePayments {
    override suspend fun initialize(publishableKey: String) {

    }
}
