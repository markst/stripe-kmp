package com.fouroneone.stripe

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult as StripePaymentSheetResult

actual typealias PlatformContext = Context

@Composable
actual fun rememberPaymentSheet(
    stripePayments: StripePayments,
    onResult: (PaymentSheetResult) -> Unit
): PaymentSheetWrapper {
    val paymentSheet = remember(onResult) {
        PaymentSheet.Builder(resultCallback = { result: StripePaymentSheetResult ->
            onResult(result.toCommonResult())
        })
    }.build()

    return remember(paymentSheet) {
        AndroidPaymentSheetWrapper(paymentSheet)
    }
}

private class AndroidStripePayments(private val context: Context) : StripePayments {

    override fun initialize(publishableKey: String) {
        PaymentConfiguration.init(
            context = context.applicationContext,
            publishableKey = publishableKey
        )
    }
}

private class AndroidPaymentSheetWrapper(
    private val paymentSheet: PaymentSheet
) : PaymentSheetWrapper {

    override fun presentWithPaymentIntent(
        clientSecret: String,
        configuration: PaymentSheetConfiguration?
    ) {
        val stripeConfig = configuration?.toAndroidConfiguration()
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret = clientSecret,
            configuration = stripeConfig
        )
    }
}

actual fun getStripePayments(context: PlatformContext): StripePayments {
    return AndroidStripePayments(context)
}

private fun PaymentSheetConfiguration.toAndroidConfiguration(): PaymentSheet.Configuration {
    val builder = PaymentSheet.Configuration.Builder(merchantDisplayName = this.merchantDisplayName)
        .allowsDelayedPaymentMethods(this.allowsDelayedPaymentMethods)

    // Set default billing details with country if provided
    this.defaultBillingDetails?.country?.let { country ->
        val billingDetails = PaymentSheet.BillingDetails(
            address = PaymentSheet.Address(country = country)
        )
        builder.defaultBillingDetails(billingDetails)
    }
    
    // Set customer configuration if provided
    if (this.customerId != null && this.customerEphemeralKeySecret != null) {
        builder.customer(
            PaymentSheet.CustomerConfiguration(
                id = this.customerId,
                ephemeralKeySecret = this.customerEphemeralKeySecret
            )
        )
    }
    
    return builder.build()
}

private fun StripePaymentSheetResult.toCommonResult(): PaymentSheetResult {
    return when (this) {
        is StripePaymentSheetResult.Completed -> PaymentSheetResult.Completed
        is StripePaymentSheetResult.Failed -> PaymentSheetResult.Failed(this.error)
        is StripePaymentSheetResult.Canceled -> PaymentSheetResult.Canceled
    }
}