package me.lovesasuna.bilibililive.log.impl

import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.log.Logger
import me.lovesasuna.bilibililive.log.getText
import org.fusesource.jansi.Ansi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SystemLogger : Logger {
    private val pattern = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun log(message: String) =
        BilibiliLive.reader.printAbove(getText("[${pattern.format(LocalDateTime.now())}] $message"))

    override fun info(message: String) =
        BilibiliLive.reader.printAbove(getText("[${pattern.format(LocalDateTime.now())}] $message"))

    override fun trace(message: String) =
        BilibiliLive.reader.printAbove(getText("[${pattern.format(LocalDateTime.now())}] $message"))

    override fun error(message: String) =
        BilibiliLive.reader.printAbove(getText("[${pattern.format(LocalDateTime.now())}] $message", Ansi.Color.RED))

    override fun fatal(message: String) =
        BilibiliLive.reader.printAbove(getText("[${pattern.format(LocalDateTime.now())}] $message", Ansi.Color.RED))
}