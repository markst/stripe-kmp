package com.fouroneone.stripe

import StripePaymentsBridge.PaymentSheetResultBridge
import StripePaymentsBridge.PaymentSheetResultBridgeCanceled
import StripePaymentsBridge.PaymentSheetResultBridgeCompleted
import StripePaymentsBridge.PaymentSheetResultBridgeFailed
import StripePaymentsBridge.StripeBridgeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication

actual abstract class PlatformContext

@OptIn(ExperimentalForeignApi::class)
private class IosStripePayments : StripePayments {

    private val stripeBridge: StripeBridgeWrapper = StripeBridgeWrapper()

    override fun initialize(publishableKey: String) {
        StripeBridgeWrapper.initializeStripeWithPublishableKey(publishableKey = publishableKey)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IosPaymentSheetWrapper(
    private val stripeBridge: StripeBridgeWrapper,
    private val viewController: objcnames.classes.UIViewController,
    private val onResult: (PaymentSheetResult) -> Unit
) : PaymentSheetWrapper {

    override fun presentWithPaymentIntent(
        clientSecret: String,
        configuration: PaymentSheetConfiguration?
    ) {
        stripeBridge.presentPaymentSheetWithPaymentIntentClientSecret(
            paymentIntentClientSecret = clientSecret,
            viewController = viewController
        ) { result: PaymentSheetResultBridge ->
            val mappedResult = when (result) {
                PaymentSheetResultBridgeCompleted -> PaymentSheetResult.Completed
                PaymentSheetResultBridgeCanceled -> PaymentSheetResult.Canceled
                PaymentSheetResultBridgeFailed -> PaymentSheetResult.Failed(Exception("Payment failed"))
                else -> PaymentSheetResult.Failed(Exception("Unknown payment result type: $result"))
            }
            onResult(mappedResult)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberPaymentSheet(
    stripePayments: StripePayments,
    onResult: (PaymentSheetResult) -> Unit
): PaymentSheetWrapper {
    val stripeBridge = remember { StripeBridgeWrapper() }

    val viewController = remember {
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: throw IllegalStateException("No root view controller found")
    }

    return remember(stripeBridge, viewController, onResult) {
        IosPaymentSheetWrapper(
            stripeBridge = stripeBridge,
            viewController = viewController as objcnames.classes.UIViewController,
            onResult = onResult
        )
    }
}

actual fun getStripePayments(context: PlatformContext): StripePayments {
    return IosStripePayments()
}