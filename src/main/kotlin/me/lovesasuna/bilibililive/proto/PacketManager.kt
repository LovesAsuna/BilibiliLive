package me.lovesasuna.bilibililive.proto

import com.fasterxml.jackson.databind.node.ObjectNode
import me.lovesasuna.bilibililive.BilibiliLive
import me.lovesasuna.bilibililive.client.Client
import me.lovesasuna.bilibililive.util.MethodHandleUtil
import java.io.IOException

/**
 * @author LovesAsuna
 **/
interface PacketManager {
    val client: Client

    fun sendJoinChannel(roomID: Int, token: String) {
        val mapper = BilibiliLive.mapper
        val objectNode: ObjectNode = mapper.createObjectNode()
            .put("roomid", roomID)
            .put("uid", 0)
            .put("key", token)
            .put("platform", "MC BC M/P")
            .put("protover", 2)
        sendPacket(7, objectNode.toString())
        BilibiliLive.logger.info("成功连接直播间: ${roomID}\n===================================")
    }

    @Throws(IOException::class)
    fun sendHeartPacket() {
        sendPacket(0, 16.toShort(), 1.toShort(), 2, 1, "")
    }

    fun sendPacket(packetType: Int, body: String) {
        sendPacket(0, 16.toShort(), 1.toShort(), packetType, 1, body)
    }

    fun sendPacket(
        originPacketLength: Int,
        packetHeadLength: Short,
        version: Short,
        packetType: Int,
        magic: Int,
        body: String
    )

    fun readHead(msg: Any): Header {
        try {
            return object : Header {
                val readIntHandle = MethodHandleUtil.getHandle(msg, "readInt", Int::class.java)
                val readShortHandle = MethodHandleUtil.getHandle(msg, "readShort", Short::class.java)
                override var packetLength: Int = readIntHandle.invoke() as Int
                override var packetHeadLength: Short = readShortHandle.invoke() as Short
                override var version: Short = readShortHandle.invoke() as Short
                override var packetType: Int = readIntHandle.invoke() as Int
                override var magic: Int = readIntHandle.invoke() as Int
            }
        } catch (e : Throwable) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }
}

interface Header {
    /**
     * 消息总长度 (协议头 + 数据长度)
     */
    var packetLength: Int

    /**
     * 头长度 固定16 Bytes
     */
    var packetHeadLength: Short
    var version: Short

    /**
     * 消息类型
     */
    var packetType: Int

    /**
     * 参数 固定为1
     */
    var magic: Int
}