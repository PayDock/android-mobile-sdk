# Changelog

## [5.3.2] - 2026-03-25

### Fixed
- `build.gradle.kts` _sourcesets_ overriding SDK `AndroidManifest.xml`


## [5.3.1] - 2026-03-13

### Fixed
- `bin-processor` module issue for publishing (moved to sourceSets - WIP)

## [5.3.0] - 2026-03-12

### Added
- Implemented `ZipWidget` for Zip Money support
- Disabled state alpha configurable for image button appearance (`ImageButtonAppearance.disabledImageAlpha`)
- Added config page to sample app (Global Config, Checkout Config, Widgets Config)

### Changed
- Updated bin processing for `CardDetailsWidget` (BIN file loading, validation, and scheme detection)
- Updated form validations for `CardDetailsWidget` and `GiftCardWidget`
- Security code and PIN code entry in `CardDetailsWidget` and `GiftCardWidget` now use secure text fields (`PasswordVisualTransformation`)
- Saving of CVV now configurable for `CardDetailsWidget` via `storeSecurityCode` in `CardDetailsWidgetConfig`
- Removed legacy "Solo" and "AUSBC" from supported card schemes for `CardDetailsWidget`
- Removed autocorrect for cardholder name for `CardDetailsWidget`
- Updated Amex card scheme icon for `CardDetailsWidget`
- Font for widgets now defaults to system font (`MaterialTheme.typography`)
- Improved error mappings to readable strings

### Fixed
- Action buttons disabled state styling
- Gift Card and Card number input field pasting options
- Incorrect fallback validation for unrecognised card schemes

## [5.2.0] - 2025-11-28

### Added
- UnionPay support for card schemes
- Callback for info events to enable analytical collection
- Dependency resolution library for better dependency management
- Customisation options: spacing control for input form widgets (widget + input spacing)
- Updated checkout implementation for ExampleApp (demo app - E2E flows)

### Changed
- Updated internal libraries for `MPGS3dsWidget`, `Standalone3DSWidget` and `ClickToPayWidget`
- Updated PayPal SDK from version `2.0.0` to `2.3.0`
- Configuration improvements: address moved to Config object for Address Widget; 
- Text and Icon customisation moved into appearance objects for SDK button widgets (ie. `PayPalSavePaymentSourceWidget`, `CardDetailsWidget`, `AddressDetailsWidget` and `GiftCardWidget`)
- Updated logic for no card scheme detection - applying default validation

### Fixed
- Accessibility improvements: keyboard navigation focus issues, Large Text support across widgets, Dark Mode input text contrast, button text truncation, and 3DS Widget visibility issues
- Payment methods: PayPal cancellation and state management 
- ColesPay widget closing and back to store cancellation 
- Google Pay cancellation feedback
- UI/UX: input field text vertical centering, disabled button styling, Click to Pay WebView session management, and app theme persistence on device rotation

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
- Split ThreeDSWidget into MPGS 3DS and Standalone3DS widgets
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
