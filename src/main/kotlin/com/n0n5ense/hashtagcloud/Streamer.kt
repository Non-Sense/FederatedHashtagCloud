package com.n0n5ense.hashtagcloud

import com.n0n5ense.hashtagcloud.common.TagData
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.Dispatcher
import com.sys1yagi.mastodon4j.api.Handler
import com.sys1yagi.mastodon4j.api.Shutdownable
import com.sys1yagi.mastodon4j.api.entity.Notification
import com.sys1yagi.mastodon4j.api.entity.Status
import com.sys1yagi.mastodon4j.api.exception.Mastodon4jRequestException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.time.Instant

class Streamer(
    private val client: MastodonClient,
    private val onReceiveHashTag: (List<TagData>) -> Unit
) {

    private val logger = LoggerFactory.getLogger(Streamer::class.java)

    private val handler = object: Handler {
        override fun onDelete(id: Long) {}

        override fun onNotification(notification: Notification) {}

        override fun onStatus(status: Status) {
//            println("${status.account?.id} ${status.account?.acct}  ${status.content}")
            if(status.tags.isNotEmpty()) {
                val instant = kotlin.runCatching { Instant.parse(status.createdAt) }.getOrDefault(Instant.now())
                val userId = status.account?.id ?: -1L
                onReceiveHashTag(status.tags.map { TagData(it.name, userId, instant) })
            }
        }
    }

    private var shutdownable: Shutdownable? = null

    fun start() {
        logger.info("stream start")
        shutdownable = federatedPublic(client, handler) {
            shutdownable?.shutdown()
            CoroutineScope(Dispatchers.Default).launch {
//                delay(60000)
                start()
            }
        }
    }
}

private fun federatedPublic(client: MastodonClient, handler: Handler, onClose: () -> Unit): Shutdownable {
    val response = client.get("streaming/public")
    if(response.isSuccessful) {
        val reader = response.body().byteStream().bufferedReader()
        val dispatcher = Dispatcher()
        dispatcher.invokeLater {
            while(true) {
                try {
                    val line = reader.readLine()
                    if(line == null || line.isEmpty()) {
                        continue
                    }
                    val type = line.split(":")[0].trim()
                    if(type != "event") {
                        continue
                    }
                    val event = line.split(":")[1].trim()
                    val payload = reader.readLine()
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
                    break
                }
            }
            reader.close()
            onClose()
        }
        return Shutdownable(dispatcher)
    } else {
        throw Mastodon4jRequestException(response)
    }
}