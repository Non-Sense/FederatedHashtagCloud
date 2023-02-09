package com.n0n5ense.hashtagcloud

import com.google.gson.Gson
import com.n0n5ense.hashtagcloud.apiserver.startServer
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import com.sys1yagi.mastodon4j.MastodonClient
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("com.n0n5ense.hashtagcloud.MainKt")
    val commandLineArgs = CommandLineArgs.parse(args)

    val client = MastodonClient.Builder(commandLineArgs.instanceDomain, OkHttpClient.Builder(), Gson())
        .useStreamingApi()
        .build()

    val database = HashTagDatabase.connect(commandLineArgs.postgresUri, "postgres", "postgrespw")
    val datasource = HashTagDataSource(database)
    database.init()

    val streamer = Streamer(client) {
        if(logger.isInfoEnabled)
            logger.info("got: ${it.joinToString { t -> t.name }}")
        datasource.addAll(it)
    }

    startServer(commandLineArgs.port, database)

    streamer.start()

    while(true) {
        val input = readLine()
        if(input?.startsWith("exit") == true)
            break
        if(input?.startsWith("s") == true){
            println("accept")
            datasource.tc(30).map { println(it) }
        }
        if(input?.startsWith("d") == true){
            println("accept")
            datasource.aggregateWithExclude(Instant.now().minusSeconds(600000), 30).map { println(it) }
        }

    }

    exitProcess(0)
}