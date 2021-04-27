package me.lovesasuna.bilibililive.proto

import io.netty.buffer.ByteBuf
import me.lovesasuna.bilibililive.client.Client
import me.lovesasuna.bilibililive.client.NettyClient
import java.nio.charset.StandardCharsets

/**
 * @author LovesAsuna
 **/
class PacketManager4Netty(override val client: Client) : PacketManager {
    private val ctx = (client as NettyClient).ctx

    override fun sendPacket(
        originPacketLength: Int,
        packetHeadLength: Short,
        version: Short,
        packetType: Int,
        magic: Int,
        body: String
    ) {
        val buf = ctx.alloc().buffer()
        var packetLength = originPacketLength
        val bodyData = body.toByteArray(StandardCharsets.UTF_8)
        if (packetLength == 0) {
            packetLength = bodyData.size + 16
        }
        buf.writeInt(packetLength)
        buf.writeShort(packetHeadLength.toInt())
        buf.writeShort(version.toInt())
        buf.writeInt(packetType)
        buf.writeInt(magic)
        if (bodyData.isNotEmpty()) {
            buf.writeBytes(bodyData)
        }
        ctx.writeAndFlush(buf)
    }

}