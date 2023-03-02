package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeTagDataSource
import com.n0n5ense.hashtagcloud.database.datasource.ExcludeUserDataSource
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class CallHandler {
    fun updateTagData(data: List<AggregatedTagData>) {
        HashTagApiData.update(data)
    }
    fun onGenerate(filePath: String) {
        HashTagApiData.updateGenerated(filePath)
    }
}

fun startServer(port: Int, database: HashTagDatabase, instanceDomain: String, reactPath: String): CallHandler {
    embeddedServer(
        Netty,
        port = port
    ) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = false
            })
        }
        install(Koin) {
            modules(
                module {
                    single { ExcludeTagDataSource(database) }
                    single { HashTagDataSource(database) }
                    single { ExcludeUserDataSource(database) }
                }
            )
        }
        install(Routing)
        configureRouting(instanceDomain, reactPath)

    }.start(wait = false)
    return CallHandler()
}