package me.lovesasuna.bilibililive

import com.fasterxml.jackson.databind.ObjectMapper
import me.lovesasuna.bilibililive.log.Logger
import me.lovesasuna.bilibililive.log.impl.SystemLogger
import me.lovesasuna.bilibililive.util.ArgsParser
import java.util.*

object BilibiliLive {
    var logger: Logger = SystemLogger
    val mapper = ObjectMapper()
}

fun main(args: Array<String>) {
    val scanner = Scanner(System.`in`)
    ArgsParser.parseStarter(args)
    var exit = false
    while (!exit) {
        exit = ArgsParser.parseCommand(scanner.nextLine())
    }
}