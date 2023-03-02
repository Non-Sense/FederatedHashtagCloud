package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.ExcludeDomain
import com.n0n5ense.hashtagcloud.common.ExcludeTag
import com.n0n5ense.hashtagcloud.common.ExcludeUser
import com.n0n5ense.hashtagcloud.common.HashTags
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeDomainDataSource
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeTagDataSource
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeUserDataSource
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
    }

    route("/exclude") {
        excludeApi()
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
    val excludeUserDataSource: ExcludeUserDataSource by inject()
    val excludeDomainDataSource: ExcludeDomainDataSource by inject()

    route("/tags") {
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

    route("/user") {
        get {
            call.respond(HttpStatusCode.OK, excludeUserDataSource.getAll())
        }
        post {
            val excludeUser = call.getPostData<ExcludeUser>().getOrElse {
                call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                return@post
            }
            if (excludeUserDataSource.add(excludeUser)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }
        delete {
            val excludeUser = call.getPostData<ExcludeUser>().getOrElse {
                call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                return@delete
            }
            if (excludeUserDataSource.remove(excludeUser)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    route("/domain") {
        get {
            call.respond(HttpStatusCode.OK, excludeDomainDataSource.getAll())
        }
        post {
            val excludeDomain = call.getPostData<ExcludeDomain>().getOrElse {
                call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                return@post
            }
            if (excludeDomainDataSource.add(excludeDomain)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict)
            }
        }
        delete {
            val excludeDomain = call.getPostData<ExcludeDomain>().getOrElse {
                call.respond(HttpStatusCode.BadRequest, it.message ?: "")
                return@delete
            }
            if (excludeDomainDataSource.remove(excludeDomain)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}
