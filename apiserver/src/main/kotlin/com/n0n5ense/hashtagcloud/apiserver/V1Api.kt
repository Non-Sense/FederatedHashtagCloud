package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.ExcludeTag
import com.n0n5ense.hashtagcloud.common.HashTags
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeTagDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

internal fun Route.v1Api(instanceDomain: String) {
    route("/tags") {
        get {
            // FIXME: ワイルドカードやめたほうがいい
            call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
            call.respond(HttpStatusCode.OK, HashTags(instanceDomain, HashTagApiData.data))
        }
        route("/exclude") {
            excludeApi()
        }
    }

    route("/generated") {
        get {
            // FIXME: ワイルドカードやめたほうがいい
            call.response.headers.append(HttpHeaders.AccessControlAllowOrigin, "*")
            call.response.headers.append(HttpHeaders.ContentType, "application/json")
            call.respond(HttpStatusCode.OK, HashTagApiData.generatedData)
        }
    }
}

private fun Route.excludeApi() {
    val datasource: ExcludeTagDataSource by inject()

    get {
        call.respond(HttpStatusCode.OK, datasource.getAll())
    }
    post {
        val excludeTag = call.getPostData<ExcludeTag>().getOrElse {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
            return@post
        }
        if (datasource.add(excludeTag.name)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.Conflict)
        }
    }
    delete {
        val excludeTag = call.getPostData<ExcludeTag>().getOrElse {
            call.respond(HttpStatusCode.BadRequest, it.message ?: "")
            return@delete
        }
        if (datasource.remove(excludeTag.name)) {
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
