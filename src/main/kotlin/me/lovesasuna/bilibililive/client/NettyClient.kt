package me.lovesasuna.bilibililive.client

import com.fasterxml.jackson.databind.JsonNode
import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
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
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * @author LovesAsuna
 **/
class NettyClient : AbstractClient() {
    lateinit var ctx: ChannelHandlerContext
    private lateinit var manager: PacketManager
    private lateinit var schedule: PluginScheduler.RepeatTaskReceipt

    override fun connect(roomID: Int) {
        fetchMessage(roomID)
        val worker = NioEventLoopGroup()

        Bootstrap()
            .channel(NioSocketChannel::class.java)
            .group(worker)
            .handler(object : ChannelInitializer<NioSocketChannel>() {
                override fun initChannel(ch: NioSocketChannel) {
                    ch.pipeline()
                        // .addLast(LoggingHandler(LogLevel.DEBUG))
                        .addLast("Read", object : ChannelInboundHandlerAdapter() {
                            override fun channelActive(ctx: ChannelHandlerContext) {
                                this@NettyClient.ctx = ctx
                                manager = PacketManagerFactory.getManager(ManagerType.NETTY, this@NettyClient)
                                manager.sendJoinChannel(roomID, token)
                                ctx.fireChannelActive()
                            }

                            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                val header = manager.readHead(msg)
                                val buf = msg as ByteBuf
                                if (header.packetType != 5 || header.packetHeadLength < 16) {
                                    return
                                }
                                val payloadLength: Int = header.packetLength - 16
                                if (payloadLength <= 0 || buf.readableBytes() < payloadLength) {
                                    return
                                }
                                val tmpBuf = ctx.alloc().buffer(payloadLength)
                                tmpBuf.writeBytes(buf, payloadLength)
                                if (header.version == 2.toShort()) {
                                    try {
                                        val bytes = ByteArray(tmpBuf.readableBytes())
                                        tmpBuf.readBytes(bytes)
                                        InflaterInputStream(
                                            ByteArrayInputStream(bytes, 0, bytes.size),
                                            Inflater(false)
                                        ).use { inflater ->
                                            val dataInputStream = DataInputStream(inflater)
                                            manager.readHead(dataInputStream)
                                            val node = BilibiliLive.mapper.readTree(
                                                dataInputStream.readBytes().toString(Charset.defaultCharset())
                                            )
                                            val pair = Pair<Int, JsonNode>(
                                                header.packetType,
                                                node
                                            )
                                            ctx.fireChannelRead(pair)
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    ctx.fireChannelRead(BilibiliLive.mapper.readTree(tmpBuf.toString(Charset.defaultCharset())))
                                }
                            }

                            override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
                                cause.printStackTrace()
                            }
                        })
                        .addLast("Process", object : ChannelInboundHandlerAdapter() {
                            override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
                                val pair = msg as Pair<*, *>
                                process(roomID, pair.first as Int, pair.second as JsonNode)
                            }
                        })
                        .addLast("Heart", object : ChannelInboundHandlerAdapter() {
                            override fun channelActive(ctx: ChannelHandlerContext) {
                                schedule = BasicUtil.scheduleWithFixedDelay({
                                    try {
                                        if (ctx.channel().isActive) {
                                            manager.sendHeartPacket()
                                        } else {
                                            GlobalScope.launch {
                                                when {
                                                    !ctx.channel().isActive -> BilibiliLive.logger.info("channel is inactive")
                                                }
                                            }
                                        }
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                }, 0, 30, TimeUnit.SECONDS).second
                            }
                        })
                }

            })
            .connect(host, port)
            .sync()
            .channel()
            .closeFuture()
            .addListener {
                worker.shutdownGracefully()
            }
    }

    override fun disconnect() {
        ctx.channel().close()
    }

    override fun processData() {

    }
}