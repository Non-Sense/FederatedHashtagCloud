package com.n0n5ense.hashtagcloud

import org.slf4j.LoggerFactory

class WordCloudGenerator(
    private val config: Config
) {

    private val logger = LoggerFactory.getLogger(WordCloudGenerator::class.java)

    fun generate(): Boolean {
        val runtime = Runtime.getRuntime()
        val process = runCatching {
            runtime.exec(config.generateCommand)
        }.getOrElse {
            logger.error(it.stackTraceToString())
            return false
        }
        runCatching {
            process.waitFor()
        }.getOrElse {
            logger.error(it.stackTraceToString())
            return false
        }
        if(process.exitValue() == 0) {
            logger.info("generated")
            return true
        }
        logger.error("error occurred")
        logger.error(process.inputStream.bufferedReader().readLines().joinToString("\n"))
        logger.error(process.errorStream.bufferedReader().readLines().joinToString("\n"))
        return false
    }
}