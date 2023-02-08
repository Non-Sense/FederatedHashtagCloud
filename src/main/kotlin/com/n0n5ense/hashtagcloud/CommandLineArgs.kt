package com.n0n5ense.hashtagcloud

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

class CommandLineArgs private constructor(
    val instanceDomain: String,
    val postgresUri: String,
    val port: Int
) {
    companion object {
        fun parse(args: Array<String>): CommandLineArgs {
            val parser = ArgParser("FederatedHashtagCloud-Backend")
            val instanceDomain by parser.argument(ArgType.String)
            val postgresUri by parser.argument(ArgType.String)
            val port by parser.argument(ArgType.Int)

            parser.parse(args)
            return CommandLineArgs(
                instanceDomain,
                postgresUri,
                port
            )
        }
    }
}