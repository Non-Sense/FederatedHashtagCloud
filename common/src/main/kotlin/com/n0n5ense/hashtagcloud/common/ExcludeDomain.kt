package com.n0n5ense.hashtagcloud.common

import kotlinx.serialization.Serializable

@Serializable
data class ExcludeDomain(
    val domain: String
)
