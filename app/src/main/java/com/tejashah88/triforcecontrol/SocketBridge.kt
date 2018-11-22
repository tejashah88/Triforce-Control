package com.tejashah88.triforcecontrol

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import java.util.concurrent.TimeUnit

class SocketBridge(private var address: String, private var port: Int, listener: SocketFeedbackListener) {
    private var feedbackListener: SocketFeedbackListener = listener
    private lateinit var sender: WebSocket

    private var oldInput: ControllerInput = ControllerInput()
    var input: ControllerInput = ControllerInput()

    init {
        reInitBridge(address, port)
    }

    fun reInitBridge(_address: String = address, _port: Int = port) {
        address = _address
        port = _port

        if (::sender.isInitialized)
            sender.close(1000, "restart")

        val client = OkHttpClient.Builder() // HEAVY
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build() // HEAVY

        val request = Request.Builder()
            .url("ws://$_address:$_port")
            .build()

        sender = client.newWebSocket(request, feedbackListener)  // HEAVY

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown()
    }

    fun trySendingData() {
        val diffString = oldInput.diffString(input)
        if (diffString.isNotBlank()) {
            Log.i("socket-bridge", diffString)
            oldInput = input.duplicate()
            sender.send(diffString)
        }
    }
}