package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

internal fun Route.v1Api() {
    route("/tags") {
        get {
            call.respond(HttpStatusCode.OK, HashTagApiData.data)
        }
    }
}

class HashTagApiData {
    companion object {
        internal var data = listOf<AggregatedTagData>()
        fun setData(data: List<AggregatedTagData>) {
            HashTagApiData.data = data
        }
    }
}