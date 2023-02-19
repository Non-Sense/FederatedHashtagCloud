package com.n0n5ense.hashtagcloud.apiserver

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*

internal fun Application.configureRouting(instanceDomain: String, reactPath: String) {
    routing {

        singlePageApplication {
            useResources = false
            filesPath = reactPath
            defaultPage = "index.html"
        }

        route("/api") {
            route("/v1") {
                v1Api(instanceDomain)
            }
        }
    }
}