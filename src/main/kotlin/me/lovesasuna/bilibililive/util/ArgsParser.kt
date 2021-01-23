package me.lovesasuna.bilibililive.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.command.Connect

object ArgsParser {
    fun parseStarter(args: Array<String>) {
        GlobalScope.launch {
            for (i in args.indices) {
                when (args[i]) {
                    "--connect" -> {
                        Connect.connect(args[i + 1].toInt())
                    }
                }
            }
        }
    }

    fun parseCommand(args: String): Boolean {
        println(args)
        return when (args) {
            "exit" -> {
                Connect.disconnect()
                true
            }
            else -> {
                BilibiliLive.logger.error("指令错误!")
                false
            }
        }
    }
}