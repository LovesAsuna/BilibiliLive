package me.lovesasuna.bilibililive.command

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.LiveData
import me.lovesasuna.bilibililive.PacketManager
import me.lovesasuna.bilibililive.util.BasicUtil
import me.lovesasuna.bilibililive.util.PluginScheduler
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * @author LovesAsuna
 **/
class Connect(val roomID: Int) : AbstractCommand() {
    private var closed: Boolean = false
    private lateinit var conn: HttpURLConnection
    private lateinit var socket: Socket
    private lateinit var schedule: PluginScheduler.RepeatTaskReceipt

    companion object {
        private lateinit var instance: Connect

        fun connect(roomID: Int) {
            instance = Connect(roomID)
            instance.execute()
        }

        fun disconnect() {
            BilibiliLive.logger.info("与直播间断开连接!")
            instance.closed = true
            instance.schedule.cancelled = true
        }
    }

    override fun run() {
        val url = URL("https://api.live.bilibili.com/room/v1/Danmu/getConf?room_id=${roomID}")
        conn = url.openConnection() as HttpURLConnection
        conn.doInput = true
        conn.doOutput = true
        conn.readTimeout = 5000
        conn.allowUserInteraction = true
        conn.connect()
        val root = BilibiliLive.mapper.readTree(conn.inputStream)

        val token = root["data"]["token"].asText()
        val host = root["data"]["host"].asText()
        val port = root["data"]["port"].asText()

        socket = Socket(host, port.toInt())
        PacketManager.sendJoinChannel(
            roomID,
            DataOutputStream(socket.getOutputStream()),
            token
        )
        schedule = BasicUtil.scheduleWithFixedDelay(kotlinx.coroutines.Runnable {
            try {
                if (!socket.isClosed && !closed) {
                    val out = DataOutputStream(socket.getOutputStream())
                    PacketManager.sendHeartPacket(out)
                } else {
                    GlobalScope.launch {
                        when {
                            socket.isClosed -> BilibiliLive.logger.info("socket is closed")
                            closed -> BilibiliLive.logger.info("live is closed")
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }, 0, 30, TimeUnit.SECONDS).second

        while (!closed && !socket.isClosed) {
            try {
                val inputStream = DataInputStream(socket.getInputStream())
                socket.soTimeout = 0
                var header = PacketManager.Header(inputStream)
                require(header.packetLength >= 16) { "协议失败: (L:" + header.packetLength.toString() + ")" }
                val payloadLength: Int = header.packetLength - 16
                if (payloadLength == 0) {
                    continue
                }
                val buffer = ByteArray(payloadLength)
                var read = 0
                do {
                    read += inputStream.read(buffer, read, payloadLength - read)
                } while (read < payloadLength)
                if (header.version == 2.toShort() && header.packetType == 5) {
                    try {
                        InflaterInputStream(
                            ByteArrayInputStream(buffer, 0, buffer.size),
                            Inflater(false)
                        ).use { inflater ->
                            val dataInputStream = DataInputStream(inflater)
                            header = PacketManager.Header(dataInputStream)
                            process(header.packetType, dataInputStream)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    process(header.packetType, DataInputStream(ByteArrayInputStream(buffer)))
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
        socket.close()
    }

    private fun process(packetType: Int, `in`: DataInputStream) {
        //3是人气回调 无视无视（
        when (packetType) {
            5 -> {
                val mapper = ObjectMapper()
                val jsonNode = mapper.readTree(`in`)
                try {
                    val bulletData = LiveData(jsonNode, roomID, 2)
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