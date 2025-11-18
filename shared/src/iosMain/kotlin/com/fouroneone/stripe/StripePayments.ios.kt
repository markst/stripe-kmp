package com.fouroneone.stripe

import StripePaymentsBridge.PaymentSheetResultBridge
import StripePaymentsBridge.PaymentSheetResultBridgeCanceled
import StripePaymentsBridge.PaymentSheetResultBridgeCompleted
import StripePaymentsBridge.PaymentSheetResultBridgeFailed
import StripePaymentsBridge.StripeBridgeWrapper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosStripePayments : StripePayments {

    private var stripeBridge: StripeBridgeWrapper? = StripeBridgeWrapper()

    @Composable
    override fun Initialize(publishableKey: String) {
        StripeBridgeWrapper.initializeStripeWithPublishableKey(publishableKey = publishableKey)
    }

    @Composable
    override fun StripeCheckout(
        clientSecret: String,
        onResult: (PaymentSheetResult) -> Unit,
        content: @Composable () -> Unit
    ) {
        val coroutineScope = rememberCoroutineScope()
        val bridge = stripeBridge

        Box(
            modifier = Modifier.clickable {
                coroutineScope.launch {
                    val result = presentPaymentSheet(clientSecret, bridge)
                    result.fold(
                        onSuccess = { paymentResult -> onResult(paymentResult) },
                        onFailure = { error -> onResult(PaymentSheetResult.Failed(error)) }
                    )
                }
            }
        ) {
            content()
        }
    }

    private suspend fun presentPaymentSheet(
        paymentIntentClientSecret: String,
        bridge: StripeBridgeWrapper?
    ): Result<PaymentSheetResult> = suspendCancellableCoroutine { continuation ->
        if (bridge == null) {
            continuation.resume(
                Result.failure(Exception("Stripe not initialized. Call Initialize() first."))
            )
            return@suspendCancellableCoroutine
        }

        val viewController = UIApplication.sharedApplication.keyWindow?.rootViewController

        if (viewController == null) {
            continuation.resume(
                Result.failure(Exception("No root view controller found"))
            )
            return@suspendCancellableCoroutine
        }

        bridge.presentPaymentSheetWithPaymentIntentClientSecret(
            paymentIntentClientSecret = paymentIntentClientSecret,
            viewController = viewController as objcnames.classes.UIViewController
        ) { result: PaymentSheetResultBridge ->
            when (result) {
                PaymentSheetResultBridgeCompleted -> {
                    continuation.resume(Result.success(PaymentSheetResult.Completed))
                }

                PaymentSheetResultBridgeCanceled -> {
                    continuation.resume(Result.success(PaymentSheetResult.Canceled))
                }

                PaymentSheetResultBridgeFailed -> {
                    continuation.resume(Result.success(PaymentSheetResult.Failed(Exception("Payment failed"))))
                }

                else -> {
                    continuation.resume(
                        Result.failure(Exception("Unknown payment result type: $result"))
                    )
                }
            }
        }
    }
}

@Composable
actual fun rememberStripePayments(): StripePayments {
    return remember {
        IosStripePayments()
    }
}