package com.n0n5ense.hashtagcloud

import com.n0n5ense.hashtagcloud.common.TagData
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Handler
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.entity.Status
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class Streamer(
    private val client: MastodonClient,
    private val onReceiveHashTag: (List<TagData>) -> Unit
) {

    companion object {
        const val STREAM_TIMEOUT = 16000
    }

    private val logger = LoggerFactory.getLogger(Streamer::class.java)

    private val handler = object: Handler {
        override fun onDelete(id: Long) {}

        override fun onNotification(notification: Notification) {}

        override fun onStatus(status: Status) {
            if(status.tags.isNotEmpty()) {
                val instant = kotlin.runCatching { Instant.parse(status.createdAt) }.getOrDefault(Instant.now())
                val userId = status.account?.id ?: -1L
                onReceiveHashTag(status.tags.map { TagData(it.name, userId, instant) })
            }
        }
    }

    fun start() {
        CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                logger.info("stream start")
                federatedPublic(client, handler)
                logger.info("stream end")

                delay(10.seconds)
            }
        }

    }

    private suspend fun federatedPublic(client: MastodonClient, handler: Handler) {
        val response = runCatching { client.get("streaming/public") }.getOrNull()
        if(response?.isSuccessful != true)
            return

        val reader = response.body().byteStream().bufferedReader()

        suspend fun readLineWithTimeout(): String? {
            return withTimeoutOrNull(3.seconds) {
                runCatching { reader.readLine() }
            }?.getOrThrow()
        }

        var lastThump = System.currentTimeMillis()
        while(true) {
            try {
                val line = readLineWithTimeout()
                val now = System.currentTimeMillis()
                if((now - lastThump) > STREAM_TIMEOUT) {
                    logger.info("stream timeout ${now - lastThump}ms")
                    break
                }
                if(line == null || line.isEmpty()) {
                    continue
                }
                if(line.startsWith(":thump")) {
                    logger.info("thump ${now - lastThump}ms")
                    lastThump = now
                    continue
                }
                val type = line.split(":")[0].trim()
                if(type != "event") {
                    continue
                }
                val event = line.split(":")[1].trim()
                val payload = readLineWithTimeout() ?: continue
                val payloadType = payload.split(":")[0].trim()
                if(payloadType != "data") {
                    continue
                }
                if(event == "update") {
                    val start = payload.indexOf(":") + 1
                    val json = payload.substring(start).trim()
                    val status = client.getSerializer().fromJson(
                        json,
                        Status::class.java
                    )
                    handler.onStatus(status)
                }
            } catch(e: Exception) {
                logger.error(e.stackTraceToString())
                break
            }
        }
        runCatching {
            reader.close()
        }
    }
}

