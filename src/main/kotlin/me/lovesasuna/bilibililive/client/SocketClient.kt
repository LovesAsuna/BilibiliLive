package me.lovesasuna.bilibililive.client

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.proto.ManagerType
import me.lovesasuna.bilibililive.proto.PacketManager
import me.lovesasuna.bilibililive.proto.PacketManagerFactory
import me.lovesasuna.bilibililive.util.BasicUtil
import me.lovesasuna.bilibililive.util.PluginScheduler
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.IOException
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * @author LovesAsuna
 **/
class SocketClient : AbstractClient() {
    lateinit var socket: Socket
    private var closed: Boolean = false
    private lateinit var schedule: PluginScheduler.RepeatTaskReceipt
    private lateinit var manager: PacketManager
    private var roomID: Int = 0
    private lateinit var inputStream: DataInputStream

    override fun connect(roomID: Int) {
        this.roomID = roomID
        run(roomID)
    }

    override fun disconnect() {
        BilibiliLive.logger.info("与直播间断开连接!")
        closed = true
        schedule.cancelled = true
    }

    private fun run(roomID: Int) {
        fetchMessage(roomID)
        socket = Socket(host, port)
        manager = PacketManagerFactory.getManager(ManagerType.JAVA_SOCKET, this)
        manager.sendJoinChannel(
            roomID,
            token
        )
        schedule = BasicUtil.scheduleWithFixedDelay({
            try {
                if (!socket.isClosed && !closed) {
                    manager.sendHeartPacket()
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

        inputStream = DataInputStream(socket.getInputStream())
        processData()
    }

    override fun processData() {
        while (!closed && !socket.isClosed) {
            try {
                socket.soTimeout = 0
                val header = manager.readHead(inputStream)
                if (header.packetLength < 16 || header.packetType != 5) {
                    continue
                }
                val payloadLength: Int = header.packetLength - 16
                if (payloadLength == 0) {
                    continue
                }
                val buffer = ByteArray(payloadLength)
                var read = 0
                do {
                    read += inputStream.read(buffer, read, payloadLength - read)
                } while (read < payloadLength)
                // println(ByteBufUtil.prettyHexDump(ByteBufAllocator.DEFAULT.heapBuffer().writeBytes(buffer)))
                if (header.version == 2.toShort()) {
                    try {
                        InflaterInputStream(
                            ByteArrayInputStream(buffer, 0, buffer.size),
                            Inflater(false)
                        ).use { inflater ->
                            val dataInputStream = DataInputStream(inflater)
                            manager.readHead(dataInputStream)
                            process(roomID, header.packetType, BilibiliLive.mapper.readTree(dataInputStream))
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    process(
                        roomID,
                        header.packetType,
                        BilibiliLive.mapper.readTree(DataInputStream(ByteArrayInputStream(buffer)))
                    )
                }
            } catch (e: RuntimeException) {
                e.printStackTrace()
            }
        }
        socket.close()
    }


}