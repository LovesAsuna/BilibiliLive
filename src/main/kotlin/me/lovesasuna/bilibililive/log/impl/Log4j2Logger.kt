package me.lovesasuna.bilibililive.log.impl

import me.lovesasuna.bilibililive.log.Logger
import org.apache.logging.log4j.LogManager

class Log4j2Logger(obj: Any) : Logger {
    private val log = LogManager.getLogger(obj)

    override fun info(message: String) = log.info(message)

    override fun trace(message: String) = log.trace(message)

    override fun error(message: String) = log.error(message)

    override fun fatal(message: String) = log.fatal(message)
}