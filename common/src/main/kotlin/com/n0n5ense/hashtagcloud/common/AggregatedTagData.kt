package com.n0n5ense.hashtagcloud.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AggregatedTagData(
    val name: String,
    val count: Long,
    @SerialName("latest")
    val leastPostTime: String
)