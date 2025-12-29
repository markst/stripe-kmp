// swift-tools-version: 5.9.1
// The swift-tools-version declares the minimum version of Swift required to build this package.

import PackageDescription

let package = Package(
    name: "StripePaymentsBridge",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "StripePaymentsBridge",
            targets: ["StripePaymentsBridge"]
        )
    ],
    dependencies: [
        .package(
            url: "https://github.com/stripe/stripe-ios.git",
            from: "25.0.1"
        )
    ],
    targets: [
        .target(
            name: "StripePaymentsBridge",
            dependencies: [
                .product(name: "StripePayments", package: "stripe-ios"),
                .product(name: "StripePaymentSheet", package: "stripe-ios"),
                .product(name: "StripeApplePay", package: "stripe-ios"),
            ]
        )

    ]
)
