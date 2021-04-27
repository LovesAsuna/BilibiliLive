package me.lovesasuna.bilibililive.client

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.LiveData
import org.apache.logging.log4j.LogManager

/**
 * @author LovesAsuna
 **/
abstract class AbstractClient(
    override var token: String,
    override var host: String,
    override var port: Int) :
    Client {
    protected val log = LogManager.getLogger()
    constructor() : this("", "", 0)

    protected fun process(roomID: Int, packetType: Int, node : JsonNode) {
        //3是人气回调 无视无视（
        when (packetType) {
            5 -> {
                try {
                    val bulletData = LiveData(node, 2)
                    if (bulletData.type != null) {
                        GlobalScope.launch {
                            when (bulletData.type) {
                                LiveData.COMMENT_TYPE -> {
                                    BilibiliLive.logger.info(bulletData.toString())
                                }
                                LiveData.LIVE_START_TYPE -> {
                                    BilibiliLive.logger.info("${roomID}开启了直播")
                                }
                                LiveData.LIVE_STOP_TYPE -> {
                                    BilibiliLive.logger.info("${roomID}关闭了直播")
                                    disconnect()
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
            3, 8 -> {
            }
            else -> {
            }
        }
    }
}