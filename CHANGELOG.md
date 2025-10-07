# Changelog

## [5.1.0] - 2025-10-07

### Changed
- Implemented native PayPal checkout flow using the latest PayPal SDK (`2.0.0`)
- Updated to use PayPal's native compose button; removed reliance on web views for PayPal
- Updated Compose BOM to `2025.06.01`
- Updated internal dependencies (AndroidX, Koin, coroutines, etc.)
- General improvements to `AddressWidget` (input UX and defaults)

### Fixed
- Coles Pay: false positive error shown after entering OTP
- Generic error handling for network failures across payment methods
- Cardholder name validation issues (including hyphen handling and invalid characters)
- Gift Card PIN length validation
- Flows resuming correctly after app interruptions
- Fallback behavior when no external browser is available
- Afterpay webview stuck due to double‑tap during loading
- Click to Pay: repeated "No Internet Connection" toasts
- Address search: "No address found" incorrectly populating input; country name validation; keyboard casing defaults
- Google Pay: misleading setup error messages
- PayPal: activity state management with "Don't keep activities" enabled (prevents re-launch loops)

### Removed
- All WebView dependencies related to PayPal checkout

## [5.0.0] - 2025-07-04

### Added
- New customisations for all Widgets with `Appearance` added to all Widget contracts
- Material Theming inheritance

### Changed
- `Config` param standardised to all Widget contracts
- Handling of token callback results for Wallets now within SDK processing
- Coles Pay redirects to success without showing success landing page

## [4.1.0] - 2025-05-30

### Changed
- Rebranded FlyPay to Coles Pay
- Refactored references from FlyPay to Coles Pay
- Gift Card pin validation logic (minLength 4)

### Fixed
- Google Pay fixed merchant identifier
- Keyboard accessibility navigation with dropdowns
- PayPal redirect cancellation flow from web

## [4.0.0] - 2025-03-28

### Changed
- Split ThreeDSWidget into Integrated3DS and Standalone3DS widgets
- Updated dependencies and gradle version to `8.8.2`
- SDK theme colours to be accessibility compliant
- Content descriptions for implied buttons (Used by accessibility TalkBack)
- Replaced `HyperlinkText` with `LinkText` composable
- `PayPalDataCollectorUtil` calling function from `collectDeviceInfo()` to `collectDeviceId`

### Fixed
- SDK Theming consistency (removed scale factor, device size)
- `LinkText` tappable area to use `minHeight = 24.dp` for better accessibility
- Updated network version to `1.3.0` containing fix for different API error response mappings

### Removed
- `verticalScroll()` from `AddressDetailsWidget`
- Settings screen from sample app
- `secretKey` functionality in place of `apiAccessToken`

## [3.1.0] - 2025-02-04

### Added
- Card schema file for card number validation
- Logic to validate card number ranges

### Changed
- Card schema regex to use bin file validation
- Client-SDK version to `v1.117.0`
- Compose BOM version to `2024.12.01`
- Refactored `CardScheme` enum to data class
- Refactoring code (package restructure)

## [3.0.0] - 2025-01-14

### Added

- New `CardDetailsWidgetConfig` to manage card details
- Supported card scheme functionality (optional)

### Changed

- `CardDetailsWidget` contract with config
- Card scheme list matching supported schemes
- Card security code to match web (CSC > CID)
- Updated font scaling (input fields and buttons) 
- Internal UI state (state = action) for Google Pay widgets

## [2.3.0] - 2024-12-18

### Added

- Icon to `PayPalVaultConfig` to apply custom icon or none
- Logic to validate card input on empty state

### Changed

- `PayPalPaymentSourceWidget` internal API flow requirements
- `PayPalPaymentSourceWidget` modifier to use default button height
- `PayPalPaymentSourceWidget` button will always have black border and text.
- Internal UI state (state = action) for CardDetails, Gift Card, 3DS, PayPal, FlyPay and Afterpay widgets

### Removed

- Create session auth token flow from PayPalVault flow as well as associated logic, exception etc

## [2.2.0] - 2024-12-06

### Added

- Added `AccountScreen` as an example for collection of PayPal Vault token and creating of Customer

### Changed

- Updated `ThreeDSEvent`s so that `chargeAuthReject` is returned as a succcesful response

### Fixed

- PayPal Vault endpoints updated to work with latest changes

## [2.1.0] - 2024-11-27

### Added

- New `loadingDelegate` field added to `CardDetailsWidget`, `PayPalWidget` and `PayPalSavePaymentSourceWidget`
- New `enabled` flag added to `CardDetailsWidget`, `PayPalWidget` and `PayPalSavePaymentSourceWidget`

## [2.0.0] - 2024-11-22

### Added

- New Widget for PayPal Vault - `PayPalSavePaymentSourceWidget`
- Added `PayPalDataCollectorUtil` utility for PayPal fraud integration

### Changed

- Repackaged project structure (**breaking changes!**)
- Separated `Theme.cornerRadius` into 2 parts (`textFieldCornerRadius` & `buttonCornerRadius`)

### Fixed

- Removed `DisposableEffect` functionality from all widgets causing issues with rotation
- Issue with state not resetting on flow completion (success or failure) - LaunchedEffect re-firing

## [1.3.0] - 2024-10-18

### Added

- `collectCardholderName` flag added to `CardDetailsWidget`

### Changed

- Targeting Compose BOM version `2024.08.00`

## [1.2.0] - 2024-10-01

### Added

- `enableTestMode` flag to `MobileSDK` initialisation
- Autofill feature to `CardDetailsWidget` and `AddressWidget`
- Web Activity for FlyPay and PayPal flows

### Changed

- Updated NetworkLib to `1.1.0`

### Fixed

- FlyPay Url and redirect Url
- PayPal redirect Url

### Removed

- Payment Workflow placeholder
- `SdkBottomSheet` embedded in widgets (3DS, PayPal and FlyPay)

## [1.1.1] - 2024-07-30

### Added

- Initial stages for Payment Workflow
- Access token functionality
- Function to validate if SDK is initialised

### Changed

- Moved network logic into separate dependency module
- Card widget input field error labels
- Consent text (for accessibility)
- Downgraded Compose BOM dependency (2023.10.01)
- Removed `publicKey` functionality, in place of `accessToken`
- Renamed "MastercardSRC" to "ClickToPay"

## [1.1.0] - 2024-06-25

### Added

- Widget based error/exception handling
- Afterpay SDK widget with Sample App integration
- Mastercard SRC SDK widget with Sample App integration
- Set fixed version for client-sdk (v1.108.0)

### Changed

- Card Widget to include save card toggle with consent text
- Minor improvements
- Sample app updates for SDK

### Fixed

- Updated FlyPay url's with other minor changes
- PayPal redirect url's handling
- SSL pinning hashes

## [1.0.1] - 2024-03-15

### Added

- README file with SDK documentation repo references

### Changed

- Minor improvements
- Sample app updates for SDK

### Fixed

- Updated FlyPay sandbox URL
- Widget state issues

## [1.0.0] - 2024-01-29

### Added

- Initial Paydock Android MobileSDK release
- Widgets for checkout integration (Card Tokenisation, PayPal, Google Pay, 3DS, Address, FlyPay)
