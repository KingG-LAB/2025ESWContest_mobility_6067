package com.example.emergencypriority.socket

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

class SocketManager(
    private val serverIp: String,
    private val serverPort: Int
) {
    fun sendMessage(
        message: String,
        onSent: (Boolean, String) -> Unit = { _, _ -> },
        onError: (Exception) -> Unit = {}
    ) {
        Thread {
            try {
                // ✅ 1) 연결 시도 (3초 안에 연결 실패 시 SocketTimeoutException)
                val socket = Socket()
                socket.connect(InetSocketAddress(serverIp, serverPort), 3000)

                // ✅ 2) 응답 대기 시간 제한 (3초 동안 응답 없으면 SocketTimeoutException)
                socket.soTimeout = 3000

                val out = PrintWriter(socket.getOutputStream(), true)
                out.println(message)
                out.flush()

                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val response = reader.readLine() ?: "서버 응답 없음"

                Log.d("SOCKET", "📤 보냄: $message")
                Log.d("SOCKET", "📥 응답: $response")

                socket.close()

                onSent(true, response)   // ✅ 성공 시 true
            } catch (e: Exception) {
                Log.e("SOCKET", "❌ 소켓 통신 실패", e)
                onError(e)
                onSent(false, e.message ?: "연결 실패")   // ✅ 실패 시 false
            }
        }.start()
    }
}
