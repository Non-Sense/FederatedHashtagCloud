package com.n0n5ense.hashtagcloud.common

import java.time.Instant

data class TagData(
    val name: String,
    val userId: Long,
    val createdAt: Instant,
    val userName: String,
    val domain: String
)