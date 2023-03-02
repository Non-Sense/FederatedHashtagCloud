package com.n0n5ense.hashtagcloud

import com.google.gson.annotations.SerializedName
import com.n0n5ense.hashtagcloud.common.TagData
import com.sys1yagi.mastodon4j.MastodonClient
import com.sys1yagi.mastodon4j.api.entity.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

class Streamer(
    private val client: MastodonClient,
    private val ignoreBot: Boolean,
    private val hostName: String,
    private val onReceiveHashTag: (List<TagData>) -> Unit
) {

    companion object {
        const val STREAM_TIMEOUT = 16000
    }

    private val logger = LoggerFactory.getLogger(Streamer::class.java)

    fun start() {
        CoroutineScope(Dispatchers.Default).launch {
            while(true) {
                logger.info("stream start")
                federatedPublic(client) { status ->

                    if(status.tags.isNotEmpty()) {
                        if(ignoreBot && status.account?.bot == true) {
                            logger.info("pass bot: ${status.tags.joinToString { it.name }}")
                            return@federatedPublic
                        }
                        val instant = kotlin.runCatching { Instant.parse(status.createdAt) }.getOrDefault(Instant.now())
                        val userId = status.account?.id ?: -1L
                        val acct = status.account?.acct?.split("@")
                        val userName = acct?.getOrNull(0) ?: ""
                        val domain = acct?.getOrElse(1) { hostName } ?: ""
                        onReceiveHashTag(status.tags.map { TagData(it.name, userId, instant, userName, domain) })
                    }
                }
                logger.info("stream end")

                delay(10.seconds)
            }
        }

    }

    private suspend fun federatedPublic(client: MastodonClient, handler: (Status) -> Unit) {
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
                    handler(status)
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

private data class Account(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("username") val userName: String = "",
    @SerializedName("acct") val acct: String = "",
    @SerializedName("display_name") val displayName: String = "",
    @SerializedName("note") val note: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("avatar") val avatar: String = "",
    @SerializedName("header") val header: String = "",
    @SerializedName("locked") val isLocked: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("followers_count") val followersCount: Int = 0,
    @SerializedName("following_count") val followingCount: Int = 0,
    @SerializedName("statuses_count") val statusesCount: Int = 0,
    @SerializedName("emojis") val emojis: List<Emoji> = emptyList(),
    @SerializedName("bot") val bot: Boolean = false
)

private data class Status(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("uri") val uri: String = "",
    @SerializedName("url") val url: String = "",
    @SerializedName("account") val account: Account? = null,
    @SerializedName("in_reply_to_id") val inReplyToId: Long? = null,
    @SerializedName("in_reply_to_account_id") val inReplyToAccountId: Long? = null,
    @SerializedName("reblog") val reblog: Status? = null,
    @SerializedName("content") val content: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("emojis") val emojis: List<Emoji> = emptyList(),
    @SerializedName("replies_count") val repliesCount: Int = 0,
    @SerializedName("reblogs_count") val reblogsCount: Int = 0,
    @SerializedName("favourites_count") val favouritesCount: Int = 0,
    @SerializedName("reblogged") val isReblogged: Boolean = false,
    @SerializedName("favourited") val isFavourited: Boolean = false,
    @SerializedName("sensitive") val isSensitive: Boolean = false,
    @SerializedName("spoiler_text") val spoilerText: String = "",
    @SerializedName("visibility") val visibility: String = Visibility.Public.value,
    @SerializedName("media_attachments") val mediaAttachments: List<Attachment> = emptyList(),
    @SerializedName("mentions") val mentions: List<Mention> = emptyList(),
    @SerializedName("tags") val tags: List<Tag> = emptyList(),
    @SerializedName("application") val application: Application? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("pinned") val pinned: Boolean? = null
) {
    enum class Visibility(val value: String) {
        Public("public"),
        Unlisted("unlisted"),
        Private("private"),
        Direct("direct")
    }
}

