package com.n0n5ense.hashtagcloud

import com.charleskorn.kaml.Yaml
import com.google.gson.Gson
import com.n0n5ense.hashtagcloud.apiserver.startServer
import com.n0n5ense.hashtagcloud.database.HashTagDatabase
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import com.sys1yagi.mastodon4j.MastodonClient
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.path.Path
import kotlin.system.exitProcess


fun main(args: Array<String>) {

    val service = Executors.newSingleThreadScheduledExecutor()
    val service2 = Executors.newSingleThreadScheduledExecutor()

    val logger = LoggerFactory.getLogger("com.n0n5ense.hashtagcloud.MainKt")
    val commandLineArgs = CommandLineArgs.parse(args)

    val config = Yaml.default.decodeFromStream(Config.serializer(), Path(commandLineArgs.configFile).toFile().inputStream())

    val client = MastodonClient.Builder(config.targetHostName, OkHttpClient.Builder(), Gson())
        .useStreamingApi()
        .build()

    val database = HashTagDatabase.connect(config.postgresUri, config.postgresUserName, config.postgresPassword)
    val datasource = HashTagDataSource(database)
    database.init()

    val streamer = Streamer(client) {
        if(logger.isInfoEnabled)
            logger.info("got: ${it.joinToString { t -> t.name }}")
        datasource.addAll(it)
    }

    val handler = startServer(config.apiServerPort, database, config.targetHostName, config.reactPath)

    val wordCloudGenerator = WordCloudGenerator(config)


    service.scheduleAtFixedRate({
        runCatching {
            datasource.aggregateWithExclude(Instant.now().minusSeconds(86400),250)
        }.onFailure {
            logger.error(it.stackTraceToString())
        }.onSuccess {
            handler.updateTagData(it)
        }
        logger.info("data updated")
        if(wordCloudGenerator.generate())
            handler.onGenerate(config.generatedJsonFile)
    }, 0, config.generateIntervalSec, TimeUnit.SECONDS)

    service2.scheduleAtFixedRate({
        runCatching {
            val lines = datasource.deleteWhere(Instant.now().minusSeconds(86400*3))
            logger.info("$lines rows deleted")
        }
    }, 0, 24, TimeUnit.HOURS)

    streamer.start()

    while(true) {
        val input = readLine()
        if(input?.startsWith("exit") == true)
            break
    }

    exitProcess(0)
}