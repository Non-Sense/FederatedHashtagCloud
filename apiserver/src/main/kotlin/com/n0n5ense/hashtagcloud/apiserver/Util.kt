package com.n0n5ense.hashtagcloud.apiserver

import io.ktor.server.application.*
import io.ktor.server.request.*

internal suspend inline fun <reified T: Any> ApplicationCall.getPostData(): Result<T> {
    return kotlin.runCatching { receive() }
}