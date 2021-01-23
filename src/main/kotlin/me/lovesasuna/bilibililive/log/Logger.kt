package me.lovesasuna.bilibililive.log

/**
 * @author LovesAsuna
 **/
interface Logger {
    fun log(message: String)
    fun info(message: String)
    fun trace(message: String)
    fun error(message: String)
    fun fatal(message: String)
}