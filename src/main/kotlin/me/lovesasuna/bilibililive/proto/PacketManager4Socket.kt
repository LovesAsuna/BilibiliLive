package me.lovesasuna.bilibililive.proto

import me.lovesasuna.bilibililive.client.Client
import me.lovesasuna.bilibililive.client.SocketClient
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class PacketManager4Socket(override val client: Client) : PacketManager {
    val out = DataOutputStream((client as SocketClient).socket.getOutputStream())

    @Throws(IOException::class)
    override fun sendPacket(packetType: Int, body: String) {
        sendPacket(0, 16.toShort(), 1.toShort(), packetType, 1, body)
    }

    @Throws(IOException::class)
    override fun sendPacket(
        originPacketLength: Int,
        packetHeadLength: Short,
        version: Short,
        packetType: Int,
        magic: Int,
        body: String
    ) {
        var packetLength = originPacketLength
        val bodyData = body.toByteArray(StandardCharsets.UTF_8)
        if (packetLength == 0) {
            packetLength = bodyData.size + 16
        }
        out.writeInt(packetLength)
        out.writeShort(packetHeadLength.toInt())
        out.writeShort(version.toInt())
        out.writeInt(packetType)
        out.writeInt(magic)
        if (bodyData.isNotEmpty()) {
            out.write(bodyData)
        }
        out.flush()
    }

    fun sendDanmu(roomID: Int, text: String) {
        val url = URL("https://api.live.bilibili.com/msg/send")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("origin", "https://live.bilibili.com")
        conn.setRequestProperty("referer", "https://live.bilibili.com/$roomID")
        conn.setRequestProperty("cookie", "")
        conn.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36"
        )
        conn.doOutput = true
        conn.connect()
        val outputStream = conn.outputStream


        val dataOutputStream = DataOutputStream(outputStream)
        val msg =
            "color=16777215&fontsize=25&mode=1&msg=${text}&rnd=1591590584&roomid=${roomID}&bubble=0&csrf_token=be130912bad65f05efa355ea02b67f7c&csrf=be130912bad65f05efa355ea02b67f7c"
        val bytes = msg.toByteArray(StandardCharsets.UTF_8)
        dataOutputStream.write(bytes)
        dataOutputStream.flush()
        conn.disconnect()
    }
}