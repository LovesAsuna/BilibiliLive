package me.lovesasuna.bilibililive.util

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.client.Client
import me.lovesasuna.bilibililive.client.NettyClient
import me.lovesasuna.bilibililive.client.SocketClient

object ArgsParser {
    lateinit var client : Client

    fun parseStarter(args: Array<String>) {
        GlobalScope.launch {
            for (i in args.indices) {
                when (args[i]) {
                    "--connect" -> {
                        client.connect(BasicUtil.getOriginRoom(BasicUtil.extractInt(args[i + 1])))
                    }
                }
            }
        }
    }

    fun parseCommand(args: String): Boolean {
        return when (args) {
            "" -> false
            "exit" -> {
                client.disconnect()
                true
            }
            else -> {
                BilibiliLive.logger.error("指令错误!")
                false
            }
        }
    }
}