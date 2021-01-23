package me.lovesasuna.bilibililive.command

/**
 * @author LovesAsuna
 **/
abstract class AbstractCommand : CommandHandler {
    override fun execute(): Boolean {
        run()
        return true
    }

    abstract fun run()
}