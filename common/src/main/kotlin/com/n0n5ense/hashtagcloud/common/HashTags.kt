package com.n0n5ense.hashtagcloud.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HashTags(
    @SerialName("hostname")
    val hostName: String,
    val tags: List<AggregatedTagData>
)
