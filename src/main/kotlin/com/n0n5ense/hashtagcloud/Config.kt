package com.n0n5ense.hashtagcloud

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val generateCommand: String,
    val generateIntervalSec: Long,
    val generatedJsonFile: String,
    val targetHostName: String,
    val postgresUri: String,
    val postgresUserName: String,
    val postgresPassword: String,
    val apiServerPort: Int,
    val reactPath: String,
    val ignoreBot: Boolean = true,
    val aggregateRangeSec: Long,
    val deleteBeforeSec: Long,
    val deletePeriodSec: Long,
    val accessToken: String,
)