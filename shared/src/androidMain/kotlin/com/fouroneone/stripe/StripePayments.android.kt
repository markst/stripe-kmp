package com.fouroneone.stripe

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult as StripePaymentSheetResult

private class AndroidStripePayments(private val context: Context) : StripePayments {

    @Composable
    override fun Initialize(publishableKey: String) {
            PaymentConfiguration.init(
                context = context,
                publishableKey = publishableKey
            )
    }

    @Composable
    override fun StripeCheckout(
        clientSecret: String,
        onResult: (PaymentSheetResult) -> Unit,
        content: @Composable () -> Unit
    ) {
        val paymentSheet = remember {
            PaymentSheet.Builder { result ->
                onResult(result.toCommonResult())
            }
        }.build()

        Box(
            modifier = Modifier.clickable {
                paymentSheet.presentWithPaymentIntent(
                    paymentIntentClientSecret = clientSecret,
                    configuration = null
                )
            }
        ) {
            content()
        }
    }
}

@Composable
actual fun rememberStripePayments(): StripePayments {
    val context = LocalContext.current
    return remember(context) {
        AndroidStripePayments(context)
    }
}

private fun StripePaymentSheetResult.toCommonResult(): PaymentSheetResult {
    return when (this) {
        is StripePaymentSheetResult.Completed -> PaymentSheetResult.Completed
        is StripePaymentSheetResult.Failed -> PaymentSheetResult.Failed(this.error)
        is StripePaymentSheetResult.Canceled -> PaymentSheetResult.Canceled
    }
}