package com.n0n5ense.hashtagcloud

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

class CommandLineArgs private constructor(
    val configFile: String,
) {
    companion object {
        fun parse(args: Array<String>): CommandLineArgs {
            val parser = ArgParser("FederatedHashtagCloud-Backend")
            val configFile by parser.argument(ArgType.String)

            parser.parse(args)
            return CommandLineArgs(
                configFile
            )
        }
    }
}