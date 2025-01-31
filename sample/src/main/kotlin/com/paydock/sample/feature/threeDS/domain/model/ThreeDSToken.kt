package com.paydock.sample.feature.threeDS.domain.model

data class ThreeDSToken(
    val token: String?,
    val status: ThreeDSStatus?,
    val id: String?
) {
    enum class ThreeDSStatus(val type: String) {
        NOT_SUPPORTED("authentication_not_supported"), PRE_AUTH_PENDING("pre_authentication_pending");

        companion object {
            fun byNameIgnoreCaseOrNull(input: String): ThreeDSStatus? {
                return entries.firstOrNull { it.type.equals(input, true) }
            }
        }
    }

    override fun toString(): String {
        return "ThreeDSToken(token='$token', status=$status)"
    }
}