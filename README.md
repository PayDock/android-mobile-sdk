[![GitLab Pipeline Status](https://gitlab.com/paydock-rps/bounded-contexts/mobile/mobile-sdk-android/badges/main/pipeline.svg)](https://gitlab.com/paydock-rps/bounded-contexts/mobile/mobile-sdk-android/-/pipelines?scope=branches&ref=main)
[![Coverage](https://gitlab.com/paydock-rps/bounded-contexts/mobile/mobile-sdk-android/badges/main/coverage.svg?job=test)](https://gitlab.com/paydock-rps/bounded-contexts/mobile/mobile-sdk-android/-/pipelines?scope=branches&ref=main)
![GitHub Release](https://img.shields.io/github/v/release/PayDock/android-mobile-sdk)
[![Maven Central](https://img.shields.io/maven-central/v/com.paydock/mobile-sdk.svg)](https://central.sonatype.com/artifact/com.paydock/mobile-sdk)
![Kotlin](https://img.shields.io/badge/kotlin-2.2.20-7F52FF?logo=kotlin&logoColor=white)
![Android Min SDK](https://img.shields.io/badge/minSdk-24-3DDC84?logo=android&logoColor=white)
![Compile SDK](https://img.shields.io/badge/compileSdk-37-3DDC84?logo=android&logoColor=white)

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
- Core library desugaring enabled in your app module (required by the Afterpay SDK, a MobileSDK
  dependency, from `afterpay-android` 4.8.x onward). Without it, your build will fail at the dexing
  step. Add to your app's `build.gradle.kts`:
  ```kotlin
  android {
      compileOptions {
          isCoreLibraryDesugaringEnabled = true
      }
  }
  dependencies {
      coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2") // or newer
  }
  ```
  Your `desugar_jdk_libs` version must be `2.1.2` or newer to match what MobileSDK was built against.

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

## Client SDK version

The web-based widgets (Click to Pay, MPGS 3DS, standalone 3DS) run Paydock's Client SDK inside a WebView. This
release supports the Client SDK line `v1.150` and, by default, loads:

| Environment | Client SDK address |
|---|---|
| `PRODUCTION`, `SANDBOX`, `STAGING` | the line's **stable** alias, `https://widget.paydock.com/sdk/v1.150.stable/widget.umd.min.js` |

Paydock promotes patch releases of the line to the stable alias, so they reach your app without a Mobile SDK update.
Sandbox and staging load the same alias as production, so what you test in sandbox is what runs in production.

If your own controls require a fixed script, pin an exact release of the supported line. The pin applies in every
environment:

```kotlin
MobileSDK.Builder()
    .environment(Environment.PRODUCTION)
    .clientSdkVersion("v1.150.1") // optional; a plain vX.Y.Z release of the supported line
    .build(context)
```

A pin outside the supported line, or with a suffix such as `-beta`, fails at `build()` with
`MobileSdkException.InvalidConfiguration`, e.g. `clientSdkVersion v1.151.0 is outside supported line v1.150`.

After a widget page loads, `MobileSDK.getInstance().loadedClientSdkVersion` returns the Client SDK version that page
reported (e.g. `v1.150.2`), and the SDK logs it at debug level. It is `null` until a page has reported one.

If the Client SDK script fails to load, or loads without providing the Client SDK, the widget ends with its load
error (`WebViewException`).

**Release order.** A Mobile SDK release that supports a new line ships only after that line's stable alias existss: promote a
release of the line to the stable alias in the client SDK pipeline. The release pipeline's `check_client_sdk_aliases`
job (`scripts/check-client-sdk-aliases.sh`) refuses the release until the stable alias serves a script and a version
record of the line.

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

   *   **`ACCESS_TOKEN_API`:** The Paydock API access token for the specified environment (e.g., sandbox, staging, production). This token is used by the sample app to make direct calls to the Paydock API for tasks like creating customers or managing transactions.
   *   **`ACCESS_TOKEN_WIDGET`:** The Paydock Widget/UI access token for the specified environment. This token is used by the Paydock SDK to authenticate and authorize the use of the pre-built UI widgets for payment processing.
   *   **`SERVICE_ID_MPGS`:** Your Paydock service ID for the MPGS (Mastercard Payment Gateway Services) gateway. This ID is required to process card payments, handle 3D Secure (3DS) authentication, and manage other card-related transactions.
   *   **`SERVICE_ID_MPGS_TEST`:** Your Paydock service ID for the MPGS (Mastercard Payment Gateway Services) gateway. This ID should use an MPGS merchant id starting with TEST* for using MPGS supported test cards and 3ds challenge emulator.
   *   **`SERVICE_ID_PAYPAL`:** Your Paydock service ID for the PayPal gateway. This ID is necessary to enable PayPal as a payment method within the sample app.
   *   **`SERVICE_ID_COLES_PAY`:** Your Paydock service ID for the Coles Pay gateway. This ID is required to enable Coles Pay as a payment method.
   *   **`SERVICE_ID_AFTERPAY`:** Your Paydock service ID for the Afterpay gateway. This ID is required to enable Afterpay as a payment method.
   *   **`SERVICE_ID_CLICK_TO_PAY`:** Your Paydock service ID for the ClickToPay gateway. This ID is required to enable ClickToPay as a payment method.
   *   **`SERVICE_ID_GOOGLE_PAY`:** Your Paydock service ID for the Google Pay gateway. This ID is required to enable Google Pay as a payment method.
   *   **`SERVICE_ID_GPAYMENTS`:** The Paydock service ID for Standalone 3DS (3D Secure) authentication, specifically using the GPayments service. This ID is used when you need to perform 3DS authentication outside of a regular payment flow.
   *   **`MERCHANT_ID_GOOGLE_PAY`:** Your Paydock merchant identifier, which is required for Google Pay. This identifier is used to associate your transactions with your Google Pay merchant account.
   *   **`WALLET_ID_COLES_PAY`:** The walled ID provided to Coles Pay. The wallet id is used to tag the wallet that is being used (e.g. Beem).

**Example `config.properties`:**
```
# Authentication
ACCESS_TOKEN_API="your_api_access_token"
ACCESS_TOKEN_WIDGET="your_widget_access_token"
# MPGS
SERVICE_ID_MPGS="your_gateway_id_mpgs"
# AFTERPAY
SERVICE_ID_AFTERPAY="your_gateway_id_after_pay"
# GOOGLE PAY
SERVICE_ID_GOOGLE_PAY="your_gateway_id_google_pay"
MERCHANT_ID_GOOGLE_PAY="your_merchant_id_google_pay"
# CLICK TO PAY
SERVICE_ID_CLICK_TO_PAY="your_service_id_click_to_pay"
# COLES PAY
SERVICE_ID_COLES_PAY="your_gateway_id_coles_pay" 
WALLET_ID_COLES_PAY="your_wallet_id_coles_pay"
# GPAYMENTS (Standalone 3ds)
SERVICE_ID_GPAYMENTS="your_gpayments_service_id"
# PAYPAL
SERVICE_ID_PAYPAL=your_gateway_id_pay_pal
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

## Telemetry

The SDK sends diagnostics to Paydock in every environment: crashes in your app, SDK errors, failed Paydock API calls,
which SDK widgets are opened and how payment attempts end. Each environment has its own Paydock collector, and the app holds no credential (no
`Authorization` header is sent):

| Environment | Collector |
|---|---|
| `PRODUCTION` | `https://mobile-telemetry.shared.apac.paydock.com/v1/logs` |
| `SANDBOX`, `STAGING` | `https://mobile-telemetry-dev.shared.apac.paydock.com/v1/logs` |

Records carry no card data, message text, request bodies or headers. There is nothing to set up and no API to call.

### Payment attempts

Five widgets record each payment attempt: `CardDetailsWidget`, `GooglePayWidget`, `PayPalWidget`,
`Standalone3DSWidget` and `MPGS3dsWidget`. An attempt starts when the shopper pays (the card submit, the Google Pay or
PayPal tap, the 3DS widget's launch) and ends at every path that ends it:

| Record | Attributes |
|---|---|
| `paydock.payment.started` | `paydock.widget`, `paydock.payment.id` (a random UUID per attempt) |
| `paydock.payment.finished` | the same, plus `paydock.payment.outcome`, `paydock.payment.duration_ms` and, only for `error`, `paydock.error.code` |

Both are `INFO`. The outcome is one of `success`, `declined` (a 3DS reject), `error` or `cancelled` (the shopper
cancelled the Google Pay sheet or the PayPal checkout). An error's code is the exception type the merchant receives
(`CHARGE_ERROR` for a 3DS charge error); a path that ends silently, handing the merchant nothing, has none. The duration
is measured inside the telemetry module, from the start on the monotonic clock. A finish is sent only for an attempt
started in the same session; at most 20 attempts are kept open, the oldest dropped first. An attempt with a start and
no finish was abandoned. Your results, callbacks and errors are unchanged.

### Data safety (Google Play)

Google Play asks you to declare the data the SDKs in your app collect. In your app's data safety form, declare under
**App info and performance**:

| Data type | Collected | Shared | Used for advertising | Encrypted in transit |
|---|---|---|---|---|
| **Crash logs** | Yes | No | No | Yes |
| **Diagnostics** | Yes | No | No | Yes |

No personal or financial data is sent.

### Running the sample with telemetry

```bash
./gradlew :sample:installSandboxDebug
```

Telemetry is active in every environment and every build type: the app sends to the collector above of the
environment the SDK is initialised with, `PRODUCTION` included. There is no local setting: the SDK no longer reads
any `paydock.telemetry.*` key from `local.properties`, so remove any you still have.

### The trigger screen

In a debug build, **Config > Telemetry triggers** has one button per record type. The screen shows each outcome as an
exception type name only.

| Button | Action | Record expected |
|---|---|---|
| Crash in SDK | Initialises the SDK a second time, uncaught on the main thread | Only `paydock.crash` (`exception`), on the next launch |
| ANR | Blocks the main thread for 120 s: tap the screen once more, wait for the "isn't responding" dialog, then tap **Close app** (waiting it out records nothing) | `paydock.crash` (`anr`) on the next launch, API 30+ |
| SDK error | Opens `Standalone3DSWidget` with an invalid token | `paydock.error` (`InvalidTokenException`, `Standalone3DSWidget`) and its `paydock.widget.opened` |
| Widget open | Opens `CardDetailsWidget` with the sample's widget access token | `paydock.widget.opened` (`CardDetailsWidget`), once per session |
| HTTP 4xx | Calls `PayPalDataCollectorUtil.initialise` with a blank access token and a test gateway ID | `paydock.network.failure` `http_4xx` on `GET /v1/gateways/{id}/wallet-config`, plus `paydock.error` |
| Timeout | The same call through a local stand-in proxy that never answers; repeat taps work | `paydock.network.failure` `timeout`, after about 60 s |
| No connection | The same call through a local stand-in proxy that refuses the connection; repeat taps work | `paydock.network.failure` `connection` |

- The crash and ANR records arrive on the **next launch**: reopen the app after it closes.
- Before Timeout and No connection, the screen closes the SDK's idle connections so the call goes through the stand-in
  proxy, and shows "connection reset" beside the result. If it shows "couldn't reset connections", the call may reuse
  a pooled connection and give the 4xx instead: tap right after launching.
- A **5xx** has no button: sandbox has no request that fails with a 5xx on demand. It takes the same path as the 4xx
  except for the status check, and the unit tests prove it: `NetworkFailurePluginTest` (a `MockEngine` 503) and
  `NetworkFailurePluginOkHttpTest` (a 500 over the real OkHttp chain), both in
  `mobile-sdk/src/test/kotlin/com/paydock/core/telemetry/`.

### Finding the records in devdock

Records sent to the dev collector (`SANDBOX` and `STAGING`) also land in devdock. In Grafana **Explore**, on the
devdock Loki data source:

- Debug records only (from a debuggable app, like the sample's debug builds): add `| paydock_app_debuggable="true"`.

- The whole run, by installation or by session:
  - `{service_name="paydock-mobile-sdk"} | app_installation_id="<id>"`
  - `{service_name="paydock-mobile-sdk"} | session_id="<id>"`
- Each record type: add `| event_name="paydock.crash"`, `| event_name="paydock.error"`,
  `| event_name="paydock.network.failure"`, `| event_name="paydock.widget.opened"` or
  `| event_name="paydock.telemetry.dropped"`.
- Payment attempts: `{service_name="paydock-mobile-sdk"} | event_name=~"paydock.payment.*"`, and by widget and
  outcome:
  - Started per widget:
    `sum by (paydock_widget) (count_over_time({service_name="paydock-mobile-sdk"} | event_name="paydock.payment.started" [1h]))`
  - Finished per widget and outcome:
    `sum by (paydock_widget, paydock_payment_outcome) (count_over_time({service_name="paydock-mobile-sdk"} | event_name="paydock.payment.finished" [1h]))`
  - Error codes: `{service_name="paydock-mobile-sdk"} | event_name="paydock.payment.finished" | paydock_payment_outcome="error"`,
    grouped by `paydock_error_code`.
- The network failures by kind:
  `{service_name="paydock-mobile-sdk"} | event_name="paydock.network.failure" | error_type="http_5xx"`.

The attribute names follow Loki's OTLP mapping (dots become underscores, so `event.name` is `event_name`); they are to be
confirmed at the first devdock run (ticket S2).

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