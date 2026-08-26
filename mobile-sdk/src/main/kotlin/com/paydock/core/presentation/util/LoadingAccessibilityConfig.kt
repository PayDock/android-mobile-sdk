package com.paydock.core.presentation.util

/**
 * Configuration for loading accessibility announcements.
 *
 * @property initialAnnouncementDelayMs Delay in milliseconds before the first announcement.
 *                                      Defaults to 0 (immediate).
 * @property reAnnouncementIntervalMs Interval in milliseconds before re-announcement.
 *                                   Defaults to 5000ms (5 seconds).
 * @property reAnnounceOnce If true, only re-announce once. If false, re-announce periodically.
 *                         Defaults to false (periodic re-announcements).
 */
data class LoadingAccessibilityConfig(
    val initialAnnouncementDelayMs: Long = 0L,
    val reAnnouncementIntervalMs: Long = 5000L,
    val reAnnounceOnce: Boolean = false
)
