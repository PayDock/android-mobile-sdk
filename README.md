[![GitLab Pipeline Status](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-sdk-android/badges/main/pipeline.svg)](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-sdk-android/-/pipelines?scope=branches&ref=main)
[![Coverage](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-sdk-android/badges/main/coverage.svg?job=test)](https://gitlab.com/paydock/bounded-contexts/mobile/mobile-sdk-android/-/pipelines?scope=branches&ref=main)
![GitHub Release](https://img.shields.io/github/v/release/PayDock/android-mobile-sdk)
[![Maven Central](https://img.shields.io/maven-central/v/com.paydock/mobile-sdk.svg)](https://central.sonatype.com/artifact/com.paydock/mobile-sdk)
![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)
![Android Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Compile SDK](https://img.shields.io/badge/compileSdk-36-3DDC84?logo=android&logoColor=white)

# Project Description

The Paydock Android MobileSDK seamlessly integrates with Paydock services to easily build a payments
flow in your Android app. The SDK provides a customisable experience with pre-built UI widgets that
handle and support various payment methods.

Once you have setup and initialised the MobileSDK in your application, you can use the MobileSDK
widgets to access payment flows. These include interacting with GooglePay and Paypal. You can also
complete 3DS challenges, capture addresses, securely collect gift card details, and tokenise card
details.

# Requirements

- Android 7.0 (API level 24) and above

# How to install and configure the SDK

1. Setup the Paydock API Integration by [contacting Paydock](https://paydock.com/contact-us/) to
   signup for a sandbox account, and then following
   our [integration guide](https://docs.paydock.com/#getting-started) .
2. [Setup](https://github.com/PayDock/mobile-sdk-doc/blob/main/setup/installation.md#setup-the-paydock-android-sdk)
   the Android SDK.
3. [Configure](https://github.com/PayDock/mobile-sdk-doc/blob/main/setup/installation.md#step-1-configure-repository-access-1)
   repository access.
4. [Add](https://github.com/PayDock/mobile-sdk-doc/blob/main/setup/installation.md#step-2-add-sdk-dependency-1)
   the SDK dependency to your app.
5. Use the following guide
   to [initialize](https://github.com/PayDock/mobile-sdk-doc/blob/main/setup/initialise.md#initialize-the-android-sdk)
   your SDK.

# Getting Started (Sample App)

This repository includes a sample application (`sample/`) that demonstrates how to integrate and use the Paydock Android Mobile SDK. 
This example app showcases various features of the SDK and provides a practical guide for developers looking to implement Paydock 
payments in their own Android applications.

## Prerequisites

Before you can run the sample app, you'll need to have the following installed:

*   **Paydock Integration** Ensure you have setup your Paydock account and signed up for your Sandbox account. (As indicated in [step 1](#how-to-install-and-configure-the-sdk))
*   **Android Studio:** The latest stable version of Android Studio is recommended. You can download it from the [official Android Studio website](https://developer.android.com/studio).
*   **Android SDK:** Ensure you have the necessary Android SDK platforms and build tools installed via the Android Studio SDK Manager.
*   **Git:** You'll need Git to clone the repository.

## Configuration

The sample app uses a `config.properties` file to manage environment-specific settings. Each build flavor has a corresponding `config.properties` file containing key-value pairs used during the build process. These values are injected into the app via `build.gradle`.

**Note:**
> You only need to create and configure the `config.properties` file for the flavors you are actively working on. For example, if you are working on `sandbox` and `prod`, you can omit the file for `staging`, as it is not required for your current build.
> Ensure the necessary configuration files are in place before running the app to avoid missing variable errors.

### `config.properties` File

1.  **Flavors:** The sample app supports three flavors: `staging`, `sandbox`, and `prod`. You'll need to provide values for each flavor.
2.  **Location:** Create a file named `config.properties` in the `sample/src/[flavor]` directory of the project.
3.  **Structure:** The `config.properties` file should contain key-value pairs for each configuration setting.
4.  **Required Fields:** The following fields are required in the `config.properties` file:

   *   **`API_ACCESS_TOKEN`:** The Paydock API access token for the specified environment (e.g., sandbox, staging, production). This token is used by the sample app to make direct calls to the Paydock API for tasks like creating customers or managing transactions.
   *   **`WIDGET_ACCESS_TOKEN`:** The Paydock Widget/UI access token for the specified environment. This token is used by the Paydock SDK to authenticate and authorize the use of the pre-built UI widgets for payment processing.
   *   **`GATEWAY_ID_MPGS`:** Your Paydock service ID for the MPGS (Mastercard Payment Gateway Services) gateway. This ID is required to process card payments, handle 3D Secure (3DS) authentication, and manage other card-related transactions.
   *   **`GATEWAY_ID_PAY_PAL`:** Your Paydock service ID for the PayPal gateway. This ID is necessary to enable PayPal as a payment method within the sample app.
   *   **`GATEWAY_ID_COLES_PAY`:** Your Paydock service ID for the Coles Pay gateway. This ID is required to enable Coles Pay as a payment method.
   *   **`GATEWAY_ID_AFTER_PAY`:** Your Paydock service ID for the Afterpay gateway. This ID is required to enable Afterpay as a payment method.
   *   **`GATEWAY_ID_CLICK_TO_PAY`:** Your Paydock service ID for the ClickToPay gateway. This ID is required to enable ClickToPay as a payment method.
   *   **`GATEWAY_ID_GOOGLE_PAY`:** Your Paydock service ID for the Google Pay gateway. This ID is required to enable Google Pay as a payment method.
   *   **`STANDALONE_3DS_SERVICE_ID`:** The Paydock service ID for Standalone 3DS (3D Secure) authentication, specifically using the GPayments service. This ID is used when you need to perform 3DS authentication outside of a regular payment flow.
   *   **`MERCHANT_IDENTIFIER`:** Your Paydock merchant identifier, which is required for Google Pay. This identifier is used to associate your transactions with your Google Pay merchant account.
   *   **`COLES_PAY_CLIENT_ID`:** The client ID provided by Coles Pay. This ID is required for authenticating and using the Coles Pay service.

**Example `config.properties`:**
```
# Authentication keys
API_ACCESS_TOKEN=your_api_access_token 
WIDGET_ACCESS_TOKEN=your_widget_access_token 
# Gateway keys
GATEWAY_ID_MPGS=your_gateway_id_mpgs 
GATEWAY_ID_PAY_PAL= your_gateway_id_pay_pal 
GATEWAY_ID_COLES_PAY=your_gateway_id_coles_pay 
GATEWAY_ID_AFTER_PAY= your_gateway_id_after_pay 
GATEWAY_ID_CLICK_TO_PAY= your_gateway_id_click_to_pay 
GATEWAY_ID_GOOGLE_PAY= your_gateway_id_google_pay 
STANDALONE_3DS_SERVICE_ID= your_standalone_3ds_service_id 
# Misc Gateway keys
MERCHANT_IDENTIFIER= your_merchant_identifier 
COLES_PAY_CLIENT_ID= your_coles_pay_client_id 
```

**Note:**
> Replace the placeholder values (e.g., `your_api_access_token`) with your actual keys and IDs.

## Running the Sample App

1.  **Clone the Repository:** bash git clone https://github.com/PayDock/android-mobile-sdk.git
2.  **Open in Android Studio:** Open the `sample/` directory in Android Studio.
3.  **Select a Build Variant:** In Android Studio, go to `Build` > `Select Build Variant...` and choose one of the following:
   *   `stagingDebug`
   *   `sandboxDebug`
   *   `prodDebug`
4.  **Run the App:** Click the "Run" button (green play icon) in Android Studio to build and run the sample app on an emulator or a connected device.

## Android notes: WebView-based flows and rotation

For JS-driven webflows (Click to Pay, 3DS), the JS runtime inside the WebView will reinitialize if the hosting Activity is recreated (e.g., orientation change). To avoid losing in-page state:

- Prefer hosting these flows in a dedicated Activity and opt out of Activity recreation for orientation/size changes:

```xml
<activity
    android:name=".feature.WebActivity"
    android:exported="true"
    android:launchMode="singleTop"
    android:configChanges="orientation|screenSize"
    android:windowSoftInputMode="adjustResize" />
```

Apply the same to 3DS demo Activities. The SDK also:
- Uses saveable WebView state and stable HTML generation to minimize reloads.
- Avoids persisting sensitive card data (PAN/CVV/expiry) across process death; rely on OS Autofill to re-fill when needed.

If you cannot opt out of recreation, pass a resumable session/token to the widget config so the flow can re-bootstrap after restore.