package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.ExcludeTag
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeTagDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

internal fun Route.v1Api() {
    route("/tags") {
        get {
            call.respond(HttpStatusCode.OK, HashTagApiData.data)
        }
        route("/exclude") {
            excludeApi()
        }
    }
}

internal fun Route.excludeApi() {
    val datasource: ExcludeTagDataSource by inject()

    get {
        call.respond(HttpStatusCode.OK, datasource.getAll())
    }
    post {
        val excludeTag = call.getPostData<ExcludeTag>().getOrElse {
            call.respond(HttpStatusCode.BadRequest, it.message?:"")
            return@post
        }
        if(datasource.add(excludeTag.name)) {
            HashTagApiData.update()
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.Conflict)
        }
    }
    delete {
        val excludeTag = call.getPostData<ExcludeTag>().getOrElse {
            call.respond(HttpStatusCode.BadRequest, it.message?:"")
            return@delete
        }
        if(datasource.remove(excludeTag.name)) {
            HashTagApiData.update()
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}
