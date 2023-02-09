package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.Duration.Companion.minutes

internal class HashTagApiData {
    companion object {
        private val logger = LoggerFactory.getLogger(HashTagApiData::class.java)
        private val datasource: HashTagDataSource by inject(HashTagDataSource::class.java)

        var data = listOf<AggregatedTagData>()
            private set

        private var job: Job? = null

        fun startJob() {
            job = CoroutineScope(Dispatchers.Default).launch {
                while(true) {
                    update()
                    delay(5.minutes)
                }
            }
        }

        suspend fun update() {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    datasource.aggregateWithExclude(Instant.now().minusSeconds(86400), 100)
                }
            }.onFailure {
                logger.error(it.stackTraceToString())
            }.onSuccess {
                data = it
            }
            logger.info("data updated")
        }

        fun stopJob() {
            job?.cancel()
        }
    }
}
