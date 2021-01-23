package me.lovesasuna.bilibililive.util

import kotlinx.coroutines.*
import me.lovesasuna.bilibililive.BilibiliLive
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


/**
 * @author LovesAsuna
 */
object BasicUtil {
    fun extractInt(string: String, defaultValue: Int = 0): Int {
        val pattern = Pattern.compile("\\d+")
        val buffer = StringBuffer()
        val matcher = pattern.matcher(string)
        while (matcher.find()) {
            buffer.append(matcher.group())
        }
        return if (buffer.toString().isEmpty()) defaultValue else buffer.toString().toInt()
    }

    /**
     * @param command 任务
     * @param delay 延迟(单位:秒)
     */
    fun scheduleWithFixedDelay(
        command: Runnable,
        initialDelay: Long,
        delay: Long,
        unit: TimeUnit
    ): Pair<Job, PluginScheduler.RepeatTaskReceipt> {
        val receipt = PluginScheduler.RepeatTaskReceipt()
        val job = GlobalScope.launch {
            delay(unit.toMillis(initialDelay))
            while (!receipt.cancelled && this.isActive) {
                withContext(Dispatchers.IO) {
                    command.run()
                }
                delay(unit.toMillis(delay))
            }
        }
        return Pair(job, receipt)
    }

    fun getOriginRoom(shortID: Int) =
        BilibiliLive.mapper.readTree(URL("https://api.live.bilibili.com/room/v1/Room/room_init?id=$shortID"))["data"]["room_id"].asInt()
}