package com.n0n5ense.hashtagcloud

import com.google.gson.Gson
import com.n0n5ense.hashtagcloud.apiserver.HashTagApiData
import com.n0n5ense.hashtagcloud.apiserver.startServer
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import com.sys1yagi.mastodon4j.MastodonClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.minutes


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

    val updateJob = CoroutineScope(Dispatchers.Default).launch {
        while(true) {
            HashTagApiData.setData(datasource.aggregateWithExclude(Instant.now().minusSeconds(86400), 100))
            delay(5.minutes)
        }
    }

    startServer(commandLineArgs.port)

    streamer.start()

    while(true) {
        val input = readLine()
        if(input?.startsWith("exit") == true)
            break
    }

    updateJob.cancel()
    exitProcess(0)
}