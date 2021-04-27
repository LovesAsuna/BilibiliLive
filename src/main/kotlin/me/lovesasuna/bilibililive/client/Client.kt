package me.lovesasuna.bilibililive.client

import me.lovesasuna.bilibililive.BilibiliLive
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author LovesAsuna
 **/
interface Client {
    var token: String
    var host: String
    var port: Int

    fun connect(roomID: Int)

    fun disconnect()

    fun fetchMessage(roomID: Int) {
        val url = URL("https://api.live.bilibili.com/room/v1/Danmu/getConf?room_id=${roomID}")
        val conn = url.openConnection() as HttpURLConnection
        conn.doInput = true
        conn.doOutput = true
        conn.readTimeout = 5000
        conn.allowUserInteraction = true
        conn.connect()
        val root = BilibiliLive.mapper.readTree(conn.inputStream)

        token = root["data"]["token"].asText()
        host = root["data"]["host"].asText()
        port = root["data"]["port"].asInt()
    }

    fun processData()
}