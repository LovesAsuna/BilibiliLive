package me.lovesasuna.bilibililive

import me.lovesasuna.bilibililive.log.Logger
import me.lovesasuna.bilibililive.log.impl.SystemLogger
import me.lovesasuna.bilibililive.util.ArgsParser
import java.util.*

object BilibiliLive {
    var logger: Logger = SystemLogger
}

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    ArgsParser.parseStarter(args)
    var exit = false
    while (!exit) {
        exit = ArgsParser.parseCommand(scanner.nextLine())
    }
}