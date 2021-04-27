package me.lovesasuna.bilibililive

import com.fasterxml.jackson.databind.ObjectMapper
import me.lovesasuna.bilibililive.log.Logger
import me.lovesasuna.bilibililive.log.getText
import me.lovesasuna.bilibililive.log.impl.SystemLogger
import me.lovesasuna.bilibililive.util.ArgsParser
import org.fusesource.jansi.Ansi
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.TerminalBuilder

object BilibiliLive {
    var logger: Logger = SystemLogger()
    val mapper = ObjectMapper()
    val terminal = TerminalBuilder.builder()
        .system(false)
        .streams(System.`in`, System.out)
        .jansi(true)
        .build()
    val reader = LineReaderBuilder.builder()
        .terminal(terminal)
        .option(LineReader.Option.ERASE_LINE_ON_FINISH, true)
        .appName("BiliBiliLive")
        .build()
}

fun main(args: Array<String>) {
    ArgsParser.parseStarter(args)
    while (true) {
        var line: String?
        try {
            line = BilibiliLive.reader.readLine(getText("> ", Ansi.Color.BLUE))
            ArgsParser.parseCommand(line)
            if (line == "exit") {
                break
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}