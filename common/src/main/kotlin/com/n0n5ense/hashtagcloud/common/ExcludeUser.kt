package com.n0n5ense.hashtagcloud.common

import kotlinx.serialization.Serializable

@Serializable
data class ExcludeUser(
    val name: String,
    val domain: String
)