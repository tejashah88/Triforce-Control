package com.tejashah88.triforcecontrol

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

open class SocketFeedbackListener : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        webSocket.send("I am here!")
        Log.i("feedback-socket", "OPEN")
    }

    override fun onMessage(webSocket: WebSocket?, text: String?) {
        Log.i("feedback-socket", "MESSAGE: " + text!!)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String?) {
        Log.i("feedback-socket", "CLOSE: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("feedback-socket", "ERROR ${t.message}")
    }
}