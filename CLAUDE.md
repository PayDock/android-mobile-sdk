# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Part of the **SDKDev** workspace — see the [workspace overview](../CLAUDE.md) for how this repo relates to the other Paydock SDK repos (client-sdk, mobile-sdk-ios, the networking libs, checkout-service).

## What this is

The Paydock Android **MobileSDK** — a Jetpack Compose UI kit of pre-built, customisable payment widgets (cards, gift cards, Google Pay, PayPal, Afterpay, Coles Pay, Click to Pay, Zip, address capture, 3DS, tokenisation). Published to Maven Central as **`com.paydock:mobile-sdk`** (current `versionName` 5.6.0, in `gradle.properties`). Kotlin **2.2.20**, `minSdk` **24** (Android 7.0), `compileSdk`/`targetSdk` **37**.

## Toolchain & setup

- **JDK 17** (`.java-version` → `17`, managed via jenv). Both `:mobile-sdk` and `:sample` compile/target Java 17 (JVM target 17).
- **Gradle 9.4.1** via the wrapper (`./gradlew`); **AGP 9.2.0**. Note AGP 9 ships built-in Kotlin support; `gradle.properties` sets `android.disallowKotlinSourceSets=false` as a bridge flag so KSP-generated sources still register.
- Versions are centralised in `gradle/libs.versions.toml` (typesafe accessors + `libs.bundles.*`; typesafe project accessors are also enabled). Several versions are deliberately pinned — the Compose BOM (`2025.06.01`), `lifecycle` (2.8.7), and `koin` (4.1.0) carry "do not bump" comments tying them to Compose 1.8.3. Respect those.
- **`convention-plugins/`** is a separate Gradle build wired in via `includeBuild("convention-plugins")` in `settings.gradle.kts`. It publishes precompiled script plugins (applied by id, e.g. `detekt-convention`, `github-publish-convention`, `maven-central-publish-convention`, `test-coverage-convention`, `dependency-analysis-convention`) that `mobile-sdk/build.gradle.kts` applies. Shared build logic lives here, not duplicated per module.
- Git hooks are auto-installed: `:mobile-sdk:preBuild` depends on `:installGitHooks`, which copies `scripts/pre-commit` into `.git/hooks/`.

## Common commands

Run from the repo root.

```bash
# Build the library
./gradlew :mobile-sdk:assemble

# Unit tests (library)
./gradlew :mobile-sdk:test
./gradlew :mobile-sdk:testDebugUnitTest
# Single test class
./gradlew :mobile-sdk:testDebugUnitTest --tests "*ClassName*"

# bin-processor unit tests
./gradlew :bin-processor:testDebugUnitTest

# Instrumented tests via Gradle Managed Device (CI-friendly, no manual emulator)
./gradlew :mobile-sdk:pixel6Api35DebugAndroidTest        # ceiling device (Pixel 6, API 35, aosp-atd)
./gradlew :mobile-sdk:minApi24DebugAndroidTest           # floor device (minSdk 24, aosp image)
./gradlew :mobile-sdk:osMatrixGroupDebugAndroidTest      # run the suite on both floor + ceiling

# Build / install the sample app (flavors: staging | sandbox | prod; build types: debug | release)
./gradlew :sample:installStagingDebug
./gradlew :sample:installSandboxDebug
./gradlew :sample:installProdDebug
```

Code quality & coverage:

```bash
# Detekt (bundles ktlint formatting rules; config in config/detekt/detekt.yml)
./gradlew detektAll          # whole project, GitLab Code Quality reports (does not fail build)
./gradlew detektFormat       # auto-format in place
./gradlew :mobile-sdk:detekt # module-scoped

# Coverage
./gradlew :mobile-sdk:testDebugUnitTest   # produces JaCoCo exec data (JaCoCo 0.8.11)
./gradlew jacocoMergedReport              # merged bin-processor + mobile-sdk report

# Dependency validation (both fail the build in CI)
./gradlew buildHealth        # dependency-analysis: unused/misconfigured deps
./gradlew :mobile-sdk:dependencyGuardBaseline   # regenerate transitive-dep baseline after intentional changes
```

The pre-commit hook runs `detektFormat` then `detektAll` and blocks commits on invalid branch names.

## Architecture

### Module graph

- **`:mobile-sdk`** — the published library (`com.android.library`, namespace `com.paydock`). The SDK entry point is `MobileSDK` / `Context.initializeMobileSDK(...)` (`mobile-sdk/src/main/kotlin/com/paydock/MobileSDK.kt`): a singleton created via `MobileSDK.Builder`, holding `environment` + `enableTestMode`, that stands up a `MobileSDKKoinContext` on init.
- **`:bin-processor`** — card BIN / scheme detection (`com.paydock.binprocessor`, ships `card-schemes.json`). **Currently compiled into `:mobile-sdk` via `sourceSets` srcDir hack** (`../bin-processor/src/main/kotlin`), not as a real module dependency — see the TODO in `mobile-sdk/build.gradle.kts`. It also has its own test suite run as `:bin-processor:testDebugUnitTest`.
- **`:sample`** — demo app (`com.android.application`, namespace `com.paydock.sample`). Uses **Hilt** for its own DI and Retrofit to call the Paydock API directly (creating customers, tokens, etc.). Excluded from the build on JitPack.
- **`convention-plugins`** — included build providing the shared script plugins (see Toolchain).

### DI is split by tool

The **SDK uses Koin** (`core/injection/SdkModule.kt` aggregates `presentationModule`, `domainModule`, `dataModule`; each `feature/*` also has its own Koin module, e.g. `feature/card/injection/CardDetailsModule.kt`). The **sample app uses Hilt/KSP** — these are unrelated DI systems; don't conflate them.

### Package structure under `mobile-sdk/src/main/kotlin/com/paydock`

- **`core/`** — infrastructure: `data` (networking client wiring, DTOs), `domain` (models like `Environment`, mappers such as `mapToBaseUrl`), `injection` (Koin), `presentation` (shared UI/extensions), `utils`, plus `MobileSDKConstants`/`ClientSDKConstants`.
- **`feature/`** — one subpackage per payment method (`card`, `googlepay`, `paypal`, `afterpay`, `colespay`, `zip`, `wallet`, `address`, `threeDS`). Each feature is internally layered `data` (dto / repository / mapper) → `domain` (model / repository interface) → `presentation` (the `*Widget` composables, `viewmodels`, `state`, and input `components`/`validators`/`transformations`). Example: `feature/card/presentation/CardDetailsWidget.kt`.
- **`designsystems/`** — Compose theming and reusable UI: `theme` (e.g. `Color.kt`), `components`, `core`. Theming is host-app customisable.

### How a payment widget flows

A consumer initialises the SDK once (`initializeMobileSDK`), then drops a feature composable (e.g. `CardDetailsWidget`) into their UI. The composable resolves its ViewModel and repository from the feature's Koin module → the repository calls out through `core`'s HTTP client, which is provided by the external **`com.paydock.core:network`** KMP library (declared `api(libs.paydock.core.networking)`, version 1.5.0 — the same networking lib the iOS SDK consumes). WebView-driven flows (Click to Pay, 3DS) render HTML/JS inside a WebView rather than native Compose.

## Conventions & gotchas

- **Sample app config** — each flavor you run needs `sample/src/<flavor>/config.properties` (`staging`/`sandbox`/`prod`). `sample/build.gradle.kts` reads it (falling back to env vars of the same name, suffixed by flavor) and injects the values as `BuildConfig` fields; a missing file just logs "Properties file not found" and yields empty values. Keys (the `BuildVariable` enum in `sample/build.gradle.kts`): `ACCESS_TOKEN_API`, `ACCESS_TOKEN_WIDGET`, `SERVICE_ID_MPGS`, `SERVICE_ID_MPGS_TEST`, `SERVICE_ID_CYBERSOURCE`, `SERVICE_ID_PAYPAL`, `SERVICE_ID_COLES_PAY`, `SERVICE_ID_AFTERPAY`, `SERVICE_ID_CLICK_TO_PAY`, `SERVICE_ID_GOOGLE_PAY`, `SERVICE_ID_GPAYMENTS` (marked "to be removed"), `SERVICE_ID_ZIP`, plus the shared `MERCHANT_ID_GOOGLE_PAY` and `WALLET_ID_COLES_PAY`. The `MERCHANT_ID_*`/`WALLET_ID_*` keys are shared across flavors; the `SERVICE_ID_*`/`ACCESS_TOKEN_*` keys are looked up per-flavor (env-var fallback name is suffixed with the uppercased flavor, e.g. `SERVICE_ID_MPGS_STAGING`). Values may be quoted (`KEY="value"`); the build strips the quotes. These are secrets — never commit real tokens.
- **WebView + rotation** — JS-driven webflows (Click to Pay, 3DS) reinitialise their in-page JS runtime if the hosting Activity is recreated (orientation/size change), losing in-page state. Host these in a dedicated Activity that opts out of recreation (`android:configChanges="orientation|screenSize"`), or pass a resumable session/token so the flow can re-bootstrap after restore. See the README's "WebView-based flows and rotation" section.
- **Dependency validation is strict** — `dependency-analysis` (`buildHealth`) fails on unused/incorrectly-configured deps (with a curated exclude list for SDKs the plugin can't detect, e.g. Afterpay/PayPal/mockk-android), and `dependency-guard` fails when the `releaseRuntimeClasspath` transitive graph changes. Regenerate the guard baseline intentionally after real dependency changes.
- **Which deps are public API** — the SDK exposes some third-party types via `api(...)` (networking lib, Google Pay compose button, Afterpay, PayPal payment buttons). Changing those affects consumers' classpaths; most other deps are `implementation`.
- **Branch naming** — enforced by `scripts/validate-branch-name.sh` (via the pre-commit hook): branches must start with `bug/`, `task/`, `feature/`, `deploy/`, or `spike/` (`main`/`release` exempt).
- **Publishing** — handled by the convention plugins (GitHub + Maven Central); publishing metadata (`groupName`, `versionName`, `libraryName`, developer info) lives in `gradle.properties`. CI is GitLab (`.gitlab-ci.yml`) with fastlane; `jitpack.yml` supports JitPack builds (which skip `:sample`).
