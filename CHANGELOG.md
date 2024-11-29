# Changelog

## [2.1.0] - 2024-11-27

### Added

- New `loadingDelegate` field added to `CardDetailsWidget`, `PayPalWidget` and `PayPalSavePaymentSourceWidget`
- New `enabled` flag added to `CardDetailsWidget`, `PayPalWidget` and `PayPalSavePaymentSourceWidget`

## [2.0.0] - 2024-11-22

### Added

- New Widget for PayPal Vault - `PayPalSavePaymentSourceWidget`
- Added `PayPalDataCollectorUtil` utility for PayPal fraud integration

# Changed

- Repackaged project structure (**breaking changes!**)
- Separated `Theme.cornerRadius` into 2 parts (`textFieldCornerRadius` & `buttonCornerRadius`)

# Fixed

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
