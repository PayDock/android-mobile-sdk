package com.paydock.core

@Suppress("MaxLineLength")
internal object MobileSDKTestConstants {

    object General {
        const val MOCK_GATEWAY_ID = "gateway_id"
        const val MOCK_INVALID_GATEWAY_ID = "invalid_gateway_id"
        const val MOCK_ACCESS_TOKEN = "mock_access_token"
    }

    object Wallet {
        const val MOCK_WALLET_TOKEN =
            "eyJpZCI6IjY2MjdhNTMxYzg5NjIyMzYyZDQ4NmU1OSIsIm1ldGEiOiJleUp0WlhSaElqcDdJbU5vWVhKblpTSTZleUpwWkNJNklqWTJNamRoTlR" +
                "NeFl6WmxZMkUyTXpZeU56RXhNVE5tTnlJc0ltRnRiM1Z1ZENJNk1UQXNJbU4xY25KbGJtTjVJam9pUVZWRUlpd2lZMkZ3ZEhWeVpTST" +
                "ZkSEoxWlN3aWNtVm1aWEpsYm1ObElqb2lOemxpWlRjeFpUY3RZMll3WmkwMFpXSTRMVGhoTUdZdE1UY3daRGxoTnpsbVlqazFJaXdpY" +
                "ldWMFlTSTZleUp6ZFdOalpYTnpYM1Z5YkNJNkltaDBkSEJ6T2k4dmNHRjVaRzlqYXkxcGJuUmxaM0poZEdsdmJpNXVaWFJzYVdaNUxt" +
                "RndjQzl6ZFdOalpYTnpJaXdpWlhKeWIzSmZkWEpzSWpvaWFIUjBjSE02THk5d1lYbGtiMk5yTFdsdWRHVm5jbUYwYVc5dUxtNWxkR3h" +
                "wWm5rdVlYQndMMlZ5Y205eUluMTlMQ0puWVhSbGQyRjVJanA3SW5SNWNHVWlPaUpCWm5SbGNuQmhlU0lzSW0xdlpHVWlPaUowWlhOME" +
                "luMTlmUT09IiwiaWF0IjoxNzEzODc0MjI1LCJleHAiOjE3MTM5NjA2MjV9"
        const val MOCK_INVALID_WALLET_TOKEN =
            "eyJpZCI6IjY2MjdhZjdmYWY0NDJhMzY0ODJlNmFlNyIsIm1ldGEiOiJleUp0WlhSaElqcDdJbU5vWVhKblpTSTZleUpwWkNJNklqWTJNamRoWmpkbU4yVTRZVE0zTXpZell6STJNV1k1TXlJc0ltRnRiM1Z1ZENJNk1Td2lZM1Z5Y21WdVkza2lPaUpCVlVRaUxDSmpZWEIwZFhKbElqcDBjblZsTENKeVpXWmxjbVZ1WTJVaU9pSTFNREV5TlRnM01pMHdNamRtTFRRM1pHRXRPRGhpTnkweFlUWXhabVl4TWpVeE4yRWlMQ0p0WlhSaElqcDdJbk4xWTJObGMzTmZkWEpzSWpvaWFIUjBjSE02THk5d1lYbGtiMk5yTFdsdWRHVm5jbUYwYVc5dUxtNWxkR3hwWm5rdVlYQndMM04xWTJObGMzTWlMQ0psY25KdmNsOTFjbXdpT2lKb2RIUndjem92TDNCaGVXUnZZMnN0YVc1MFpXZHlZWFJwYjI0dWJtVjBiR2xtZVM1aGNIQXZaWEp5YjNJaWZYMHNJbWRoZEdWM1lYa2lPbnNpZEhsd1pTSTZJa0ZtZEdWeWNHRjVJaXdpYlc5a1pTSTZJblJsYzNRaWZYMTkiLCJpYXQiOjE3MTM4NzY4NjMsImV4cCI6MTcxMzk2MzI2M30"
    }

    object Charge {
        const val MOCK_CHARGE_ID = "6627a531c6eca636271113f7"
        const val MOCK_INVALID_CHARGE_ID = "invalid-charge-id"
    }

    object Card {
        const val MOCK_CARD_TOKEN = "f6301700-dcfe-4640-aabf-eff4ee3d96a6"
    }

    object Address {
        const val MOCK_COUNTRY = "South Africa"
        const val MOCK_QUERY = "South"
    }

    object GooglePay {
        const val REF_TOKEN =
            "{\"signature\":\"MEQCIDPyn7sbShwHJgfp7+odS4csaJKZwL9cWkvLNXfl8aIwAiAiQEJT3p+O0Ul8zeKxf5UkjCb/YNfiDCjT0kWAD7M3Xg\\u003d\\u003d\"," +
                "\"intermediateSigningKey\":{\"signedKey\":\"{\\\"keyValue\\\":\\\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEwyp2Bhw5FzeKh/1p+XEBjdnb" +
                "jhKFRXs46fM2SAI787SmNn45I3sKsw3hpkMalE/LVUN6nH/k2dW9L6rvuyueoQ\\\\u003d\\\\u003d\\\",\\\"keyExpiration\\\":\\\"1698720666540\\\"}" +
                "\",\"signatures\":[\"MEYCIQCKbMoZ4ZkcmBVDr7rHsHQfOHOHTuQ2SjWggWfak0TSMAIhAIwfjJMfwcIl4aOmOzTfHk75VKlMuKTiVCSFn6PGeYgO\"]}," +
                "\"protocolVersion\":\"ECv2\",\"signedMessage\":\"{\\\"encryptedMessage\\\":\\\"5xKfO44VN1wnvquWpYDPB0outuJrlXGV7E/AdK2/snKT0z/f" +
                "aInFxUL2urp9WF826qMl0UlAppmm2hUtQeTRxO8v6WJXx+XYAH128Uoc1lKDAsZfZRycq1czYkUnaYHT0OPdkqTUlI9/k3o5vClOyq3YoEVv1e4nUVKGjPTALEEC7Qe3" +
                "fCReIWIH9RnIP0HQdfYcv6cKWaZHxt7ZJUf3nqSSDeU3H7lKdKJ8zajOC/9erXcm6a2KymtDpz9dIxn/WNBeJjDSYIrPg1RPWm0PRkvU///HuAJrBBJF/SgnQVnotYHk" +
                "cZnZyFLoo5oByBMR/X5+NM5TyknpoLjVAophJQ/pBiUhsA+dZ0TF4mUBa3bRcMgjhnmR49zRBrf8C5MT+JbyV1ltifWi8t/Vil4ILpIy3CG5hcX50+xbZssUzkEtS6Hd" +
                "M2X7REVZwuZc5x2eHVTY+MXi2oBdyPljxb4Kfstbf94lZy9R2zY\\\\u003d\\\",\\\"ephemeralPublicKey\\\":\\\"BOZLC8D8kjf9q+qXHR2GumIkeAsIpoad9A" +
                "bvQwLKp7NjJFSTQuww82Ukyw/YpYEKUtZ1swHrr9RbfJsrFsjjj9Q\\\\u003d\\\",\\\"tag\\\":\\\"f5lDx7XlR8mbHgV9ivXjiULNIekvd8pf6Z/UuOVKnoY\\\\u003d\\\"}\"}"
    }

    object PayPal {
        const val MOCK_PAYMENT_METHOD_ID = "06S13800C2876432A"
        const val MOCK_PAYER_ID = "H2G7GULMXJZU6"
        const val MOCK_CALLBACK_URL =
            "https://www.sandbox.paypal.com/checkoutnow?token=63124973UW6479408"
        const val MOCK_TOKEN = "2V6045660E724300D"
    }

    object PayPalVault {
        const val MOCK_ACCESS_TOKEN =
            "A21AAIzaP6XjWLlzu0wbGu4oifnHyCzn0j9k_lReSbNQbzF8WvoSz0xkL_SwR5YqY5GoPOsI5Zj-_nk1cBsDgx86hSX_gRguA"
        const val MOCK_ID_TOKEN =
            "\"eyJraWQiOiJhMDRiMjEwZDYzYjU0NWQzODEwOWUxNDM3ZjQ3ZWYxMiIsInR5cCI6IkpXVCIsImFsZyI6IkVTMjU2In0.eyJpc3M" +
                "iOiJodHRwczovL2FwaS5zYW5kYm94LnBheXBhbC5jb20iLCJzdWIiOiJGRkREU0VYR0U3SEdVIiwiYWNyIjpbImNsaWVudCJdLCJzY29wZSI6WyJCcmFpbnR" +
                "yZWU6VmF1bHQiXSwib3B0aW9ucyI6e30sImF6IjoiY2NnMTguc2xjIiwiZXh0ZXJuYWxfaWQiOlsiUGF5UGFsOkZGRERTRVhHRTdIR1UiLCJCcmFpbnRyZ" +
                "WU6OW1mcWhiaGRzY2pkajZ4dCJdLCJleHAiOjE3Mjc3OTAwOTYsImlhdCI6MTcyNzc4OTE5NiwianRpIjoiVTJBQUtyODlQenMtTmcwc2lpUk5sNlNWZlp" +
                "lSjhaanJJUzc0MnVrekRlM2lxNUhKb3BRM2lCR1J2dDFfQTlwbktmTV9QS09mMTA2MV9kaUotdTM3TGpQcnhyMmlEVHhyV0IyOVFicWR3ZWxtWFpka0pxelh" +
                "OUFoyZXQ0c3N6REEifQ.xlq8HPsjM4hviIL7VKEZiQ3XvXR_PL2ITq6jiA-QuVaVN4lvv_lolBQzuSx18eV9UaZkzSktqo3Fne05WwylKQ\""
        const val MOCK_SETUP_TOKEN = "XObCsxdHXe"
        const val MOCK_CLIENT_ID = "AY-iOYV1QKAX6ZRomt-gXigd0-pToRMwdoLW4UxFSITOApI2jUa5UgM39MKC0qeip3SCbPozbAusuGO0"
        const val MOCK_PAYMENT_TOKEN = "f6301700-dcfe-4640-aabf-eff4ee3d96a6"
        const val MOCK_EMAIL = "test@email.com"
    }

    object Afterpay {
        const val MOCK_CHECKOUT_TOKEN = "001.6i9ukio2hmngq2ddkrf0h5cdhr0ntjqoqc6h6lrc92gvdpn3"
        const val MOCK_INVALID_CHECKOUT_TOKEN = "invalid-checkout-token"
    }

    object FlyPay {
        const val MOCK_ORDER_ID = "721hnve4yh6fdf"
    }

    object ClickToPay {
        const val MOCK_CHECKOUT_TOKEN = "6627a531c6eca636271113f7"
    }

    object ThreeDS {
        const val MOCK_CHARGE_ID = "ba7d839a-a868-4bb0-9763-015451fea9fb"
    }
    object Jwt {
        const val MOCK_WALLET_TOKEN_PAYLOAD =
            "{\"id\":\"6627a531c89622362d486e59\",\"meta\":\"eyJtZXRhIjp7ImNoYXJnZSI6eyJpZCI6IjY2MjdhNTMxYzZlY2E2MzYyNzExMTN" +
                "mNyIsImFtb3VudCI6MTAsImN1cnJlbmN5IjoiQVVEIiwiY2FwdHVyZSI6dHJ1ZSwicmVmZXJlbmNlIjoiNzliZTcxZTctY2YwZi00Z" +
                "WI4LThhMGYtMTcwZDlhNzlmYjk1IiwibWV0YSI6eyJzdWNjZXNzX3VybCI6Imh0dHBzOi8vcGF5ZG9jay1pbnRlZ3JhdGlvbi5uZXR" +
                "saWZ5LmFwcC9zdWNjZXNzIiwiZXJyb3JfdXJsIjoiaHR0cHM6Ly9wYXlkb2NrLWludGVncmF0aW9uLm5ldGxpZnkuYXBwL2Vycm9y" +
                "In19LCJnYXRld2F5Ijp7InR5cGUiOiJBZnRlcnBheSIsIm1vZGUiOiJ0ZXN0In19fQ==\",\"iat\":1713874225," +
                "\"exp\":1713960625}"
        const val MOCK_META_TOKEN_PAYLOAD =
            "{\"meta\":{\"charge\":{\"id\":\"6627a531c6eca636271113f7\",\"amount\":10,\"currency\":\"AUD\"," +
                "\"capture\":true,\"reference\":\"79be71e7-cf0f-4eb8-8a0f-170d9a79fb95\"," +
                "\"meta\":{\"success_url\":\"https://paydock-integration.netlify.app/success\"," +
                "\"error_url\":\"https://paydock-integration.netlify.app/error\"}},\"gateway\":" +
                "{\"type\":\"Afterpay\",\"mode\":\"test\"}}}"
        const val MOCK_EXPIRATION_DATE_STRING = "Wed Apr 24 14:10:25 SAST 2024"
    }

    object Errors {
        const val MOCK_GENERAL_ERROR = "Unexpected error has occurred!"
        const val MOCK_INVALID_GATEWAY_ID_ERROR = "invalid gateway_id"
        const val MOCK_TOKENIZATION_ERROR = "Tokenization failed"
        const val MOCK_WALLET_TYPE_ERROR = "wallet_type must be a valid enum value"
        const val MOCK_INVALID_CARD_DETAILS_ERROR = "Invalid Transaction Details"
        const val MOCK_INVALID_GIFT_CARD_DETAILS_ERROR = "Card scheme is required"
        const val MOCK_AFTER_PAY_LOCALE_ERROR =
            "Locale contains an unsupported country: CN. Supported countries include: AU,CA,CA,GB,NZ,US,IT,FR,ES"
        const val MOCK_AFTER_PAY_TOKEN_ERROR = "Error fetching checkout token"
        const val MOCK_INVALID_OAUTH_TOKEN_ERROR = "Token signature verification failed"
        const val MOCK_AUTH_TOKEN_ERROR = "Error creating session auth token"
        const val MOCK_SETUP_TOKEN_ERROR = "Error creating setup token"
        const val MOCK_CLIENT_ID_ERROR = "Error fetching gateway clientId setup token"
    }
}