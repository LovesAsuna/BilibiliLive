package me.lovesasuna.bilibililive.proto

import me.lovesasuna.bilibililive.client.Client
import java.io.DataOutputStream
import java.net.Socket

object PacketManagerFactory {
    fun getManager(type: ManagerType, client: Client): PacketManager {
        return when (type) {
            ManagerType.JAVA_SOCKET -> {
                PacketManager4Socket(client)
            }
            ManagerType.NETTY -> {
                PacketManager4Netty(client)
            }
        }
    }
}

enum class ManagerType {
    JAVA_SOCKET,
    NETTY;
}