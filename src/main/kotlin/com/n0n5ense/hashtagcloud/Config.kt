package com.n0n5ense.hashtagcloud

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val generateCommand: String,
    val generateIntervalSec: Long,
    val generatedJsonFile: String,
    val targetHostName: String,
    val postgresUri: String,
    val apiServerPort: Int,
)