package com.paydock.sample.designsystems.theme

/**
 * Represents the size of a device screen.
 *
 * This enum provides a simple way to categorize devices based on their
 * screen dimensions, allowing for different layouts and UI elements to be
 * applied according to the device size.
 *
 * The following sizes are defined:
 * - [SMALL]: Represents devices with small screens, such as compact phones.
 * - [MEDIUM]: Represents devices with medium-sized screens, such as most
 *   standard smartphones and some smaller tablets.
 * - [LARGE]: Represents devices with large screens, such as tablets and
 *   desktop monitors.
 *
 * Note: The specific screen dimensions associated with each size are not
 * defined here, and may vary depending on the application's specific needs
 * and target audience. This enum primarily serves as a high-level
 * classification system.
 */
enum class DeviceSize {
    SMALL, MEDIUM, LARGE
}