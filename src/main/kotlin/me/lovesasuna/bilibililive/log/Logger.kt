package me.lovesasuna.bilibililive.log

import org.fusesource.jansi.Ansi

/**
 * @author LovesAsuna
 **/
interface Logger {
    fun info(message: String)
    fun trace(message: String)
    fun error(message: String)
    fun fatal(message: String)
}

fun getText(text: String, color: Ansi.Color = Ansi.Color.DEFAULT): String = Ansi.ansi().eraseScreen().fg(color).a(text).reset().toString()