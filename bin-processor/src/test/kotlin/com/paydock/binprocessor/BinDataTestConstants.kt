package com.paydock.binprocessor

/**
 * Test constants for BIN data mocking.
 */
object BinDataTestConstants {
    const val MOCK_LAST_MODIFIED = "Wed, 18 Feb 2026 10:00:00 GMT"
    const val MOCK_LAST_MODIFIED_OLD = "Wed, 17 Feb 2026 10:00:00 GMT"
    const val MOCK_ETAG = "\"abc123\""
    const val MOCK_ETAG_OLD = "\"old-etag\""
    const val MOCK_BIN_DATA_CONTENT = """{"version":1,"schemes":{"v":"visa"},"2":{},"4":{},"6":{},"r":{"2":[],"4":[],"6":[]}}"""
}
