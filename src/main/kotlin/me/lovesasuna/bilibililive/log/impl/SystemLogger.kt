package me.lovesasuna.bilibililive.log.impl

import me.lovesasuna.bilibililive.log.Logger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SystemLogger : Logger {
    private val pattern = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun log(message: String) = println("[${pattern.format(LocalDateTime.now())}] $message")

    override fun info(message: String) = println("[${pattern.format(LocalDateTime.now())}] $message")

    override fun trace(message: String) = println("[${pattern.format(LocalDateTime.now())}] $message")

    override fun error(message: String) = System.err.println("[${pattern.format(LocalDateTime.now())}] $message")

    override fun fatal(message: String) = System.err.println("[${pattern.format(LocalDateTime.now())}] $message")
}