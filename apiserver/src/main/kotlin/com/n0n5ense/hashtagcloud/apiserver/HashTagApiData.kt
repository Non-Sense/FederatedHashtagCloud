package com.n0n5ense.hashtagcloud.apiserver

import com.n0n5ense.hashtagcloud.common.AggregatedTagData
import com.n0n5ense.hashtagcloud.database.datasource.HashTagDataSource
import kotlinx.coroutines.Job
import org.koin.java.KoinJavaComponent.inject
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset

internal class HashTagApiData {
    companion object {
        private val logger = LoggerFactory.getLogger(HashTagApiData::class.java)
        private val datasource: HashTagDataSource by inject(HashTagDataSource::class.java)

        var data = listOf<AggregatedTagData>()
            private set

        var generatedData: String = ""
            private set

        private var job: Job? = null

        fun updateGenerated(filePath: String) {
            val reader = File(filePath).inputStream().bufferedReader(Charset.forName("UTF-8"))
            generatedData = reader.readLines().joinToString("")
        }

        fun update(data: List<AggregatedTagData>) {
            this.data = data
        }

        fun stopJob() {
            job?.cancel()
        }
    }
}
