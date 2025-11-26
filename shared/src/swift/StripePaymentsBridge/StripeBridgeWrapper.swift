import Foundation
import StripePaymentSheet
import UIKit

/// Bridge wrapper for Stripe iOS SDK
///
/// This wrapper exists because Stripe's PaymentSheet and related types are Swift-only
/// and do not have @objc exposure, making them unavailable to Kotlin/Native cinterop.
/// We bridge these Swift APIs to Objective-C compatible interfaces.
///
/// Note: STPAPIClient and other core Stripe types DO have @objc exposure and can be
/// used directly from Kotlin without this wrapper.
@objc public class StripeBridgeWrapper: NSObject {

    private var paymentSheet: PaymentSheet?

    @objc public override init() {
        super.init()
    }

    @objc public static func initializeStripe(publishableKey: String) {
        StripeAPI.defaultPublishableKey = publishableKey
    }

    @objc public func presentPaymentSheet(
        paymentIntentClientSecret: String,
        viewController: UIViewController,
        defaultCountry: String?,
        completion: @escaping (PaymentSheetResultBridge) -> Void
    ) {
        var configuration = PaymentSheet.Configuration()
        
        // Set default billing details with country if provided
        if let country = defaultCountry {
            configuration.defaultBillingDetails.address = PaymentSheet.Address(country: country)
        }

        let paymentSheet = PaymentSheet(
            paymentIntentClientSecret: paymentIntentClientSecret,
            configuration: configuration
        )

        self.paymentSheet = paymentSheet

        paymentSheet.present(from: viewController) { paymentResult in
            let bridgeResult: PaymentSheetResultBridge

            switch paymentResult {
            case .completed:
                bridgeResult = .completed
            case .canceled:
                bridgeResult = .canceled
            case .failed(_):
                bridgeResult = .failed
            }

            completion(bridgeResult)
        }
    }
}

/// Bridge enum for PaymentSheet.PaymentSheetResult
///
/// PaymentSheet.PaymentSheetResult is a Swift enum without @objc exposure.
/// This @objc compatible enum bridges the result to Kotlin.
@objc public enum PaymentSheetResultBridge: Int {
    case completed
    case canceled
    case failed
}
