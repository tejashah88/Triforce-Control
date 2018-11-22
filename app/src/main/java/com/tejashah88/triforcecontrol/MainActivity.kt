package com.tejashah88.triforcecontrol

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.KeyEvent
import android.view.MotionEvent
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.Response
import okhttp3.WebSocket
import org.jetbrains.anko.doAsync
import java.net.InetAddress
import java.net.UnknownHostException

// Main docs: https://developer.android.com/training/game-controllers/controller-input
// NVIDIA input docs: https://docs.nvidia.com/gameworks/content/technologies/mobile/game_controller_quickdoc_amalgamated_gamepad.htm

@SuppressLint("SetTextI18n")
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var bridge: SocketBridge

    private fun initSocketClient(addr: String, port: Int) {
        if (!::bridge.isInitialized)
            bridge = SocketBridge(addr, port, object: SocketFeedbackListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    runOnUiThread { lblComputerStatus.text = "Connected" }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    val msg = t.message!!

                    val outputText: String = when {
                        msg == "Connection reset" -> "Error: Server just went down!"
                        msg.startsWith("failed to connect") -> "Error: Can't connect to server!"
                        else -> {
                            t.printStackTrace()
                            "Error: ${t.message}"
                        }
                    }

                    runOnUiThread { lblComputerStatus.text = outputText }
                }
            })
        else
            bridge.reInitBridge(addr, port)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Default server hostname to connect to
        txtComputerName.setText("${Constants.DEFAULT_HOST_NAME}:${Constants.DEFAULT_PORT}")

        btnRefresh.setOnClickListener {
            lblControllerIDs.text = "Loading..."
            lblControllerStatus.text = "Loading..."
            lblComputerStatus.text = "Loading..."

            val connectedIDs = ControllerUtility.getGameControllerIds().filter { id -> id > 0 }
            lblControllerIDs.text = connectedIDs.joinToString(prefix = "[", postfix = "]")
            lblControllerStatus.text = if (connectedIDs.isNotEmpty()) "Connected" else "Not Connected"

            doAsync {
                try {
                    val (addrStr, portStr) = txtComputerName.text.toString().split(":")
                    val addr = InetAddress.getByName(addrStr)
                    val port = portStr.toInt()
                    initSocketClient(addr.hostAddress, port)
                } catch (ex: Exception) {
                    val msg: String = ex.message!!
                    val outputText: String = if (ex is IndexOutOfBoundsException && msg.startsWith("Index:"))
                        "Error: Malformed 'address:port' combo!"
                    else if (ex is NumberFormatException && msg.startsWith("For input string"))
                        "Error: Invalid port specified!"
                    else if (ex is UnknownHostException && msg.startsWith("Unable to resolve host"))
                        "Error: Can't resolve hostname!"
                    else
                        msg

                    runOnUiThread { lblComputerStatus.text = outputText }
                }
            }
        }
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        // Check if this event is from a Direction Pad button and process accordingly.
        var handled = false

        if (ControllerUtility.fromDpadDevice(event)) {
            val (xAxis: Float, yAxis: Float) = ControllerUtility.getDirectionPressed(event)

            bridge.input.dpad.up = yAxis == 1f
            if (yAxis == 1f) {
                lblDpadButtonUp.text = Html.fromHtml("<b>Up</b>")
            } else {
                lblDpadButtonUp.text = "Up"
            }

            bridge.input.dpad.down = yAxis == -1f
            if (yAxis == -1f) {
                lblDpadButtonDown.text = Html.fromHtml("<b>Down</b>")
            } else {
                lblDpadButtonDown.text = "Down"
            }

            bridge.input.dpad.right = xAxis == 1f
            if (xAxis == 1f) {
                lblDpadButtonRight.text = Html.fromHtml("<b>Right</b>")
            } else {
                lblDpadButtonRight.text = "Right"
            }

            bridge.input.dpad.left = xAxis == -1f
            if (xAxis == -1f) {
                lblDpadButtonLeft.text = Html.fromHtml("<b>Left</b>")
            } else {
                lblDpadButtonLeft.text = "Left"
            }

            handled = true
        }

        // Check if this event is from a joystick and process accordingly.

        // Check that the event came from a game controller that supports joysticks and/or brakes
        if (ControllerUtility.fromJoystickDevice(event)) {
            // Process joystick input
            val leftJoystick: FloatArray = ControllerUtility.getLeftJoystickCurrent(event, Constants.JOYSTICK_DEADZONE)
            lblLeftJoystickHorizontal.text = leftJoystick[0].format()
            bridge.input.leftJoystick.horizontal = leftJoystick[0]

            lblLeftJoystickVertical.text = leftJoystick[1].format()
            bridge.input.leftJoystick.vertical = leftJoystick[1]

            val rightJoystick: FloatArray = ControllerUtility.getRightJoystickCurrent(event, Constants.JOYSTICK_DEADZONE)
            lblRightJoystickHorizontal.text = rightJoystick[0].format()
            bridge.input.rightJoystick.horizontal = rightJoystick[0]

            lblRightJoystickVertical.text = rightJoystick[1].format()
            bridge.input.rightJoystick.vertical = rightJoystick[1]

            // Process brakes input
            val leftBrake: Float = ControllerUtility.getLeftBrakeCurrent(event, Constants.BRAKE_DEADZONE)
            lblShoulderBrakesLeft.text = leftBrake.format()
            bridge.input.brakes.left = leftBrake

            val rightBrake: Float = ControllerUtility.getRightBrakeCurrent(event, Constants.BRAKE_DEADZONE)
            lblShoulderBrakesRight.text = rightBrake.format()
            bridge.input.brakes.right = rightBrake

            handled = true
        }

        bridge.trySendingData()

        return handled || super.onGenericMotionEvent(event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (ControllerUtility.fromGamePad(event)) {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                bridge.input.buttons.A = true
                lblLetterButtonA.text = Html.fromHtml("<b>A</b>")
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                bridge.input.buttons.B = true
                lblLetterButtonB.text = Html.fromHtml("<b>B</b>")
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                bridge.input.buttons.X = true
                lblLetterButtonX.text = Html.fromHtml("<b>X</b>")
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
                bridge.input.buttons.Y = true
                lblLetterButtonY.text = Html.fromHtml("<b>Y</b>")
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
                bridge.input.triggers.left = true
                lblShoulderTriggerLeft.text = Html.fromHtml("<b>Left</b>")
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
                bridge.input.triggers.right = true
                lblShoulderTriggerRight.text = Html.fromHtml("<b>Right</b>")
            }

            bridge.trySendingData()

            return true
        }

        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (ControllerUtility.fromGamePad(event)) {
            // Handle releasing buttons
            if (keyCode == KeyEvent.KEYCODE_BUTTON_A) {
                bridge.input.buttons.A = false
                lblLetterButtonA.text = "A"
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_B) {
                bridge.input.buttons.B = false
                lblLetterButtonB.text = "B"
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_X) {
                bridge.input.buttons.X = false
                lblLetterButtonX.text = "X"
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
                bridge.input.buttons.Y = false
                lblLetterButtonY.text = "Y"
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_L1) {
                bridge.input.triggers.left = false
                lblShoulderTriggerLeft.text = "Left"
            }

            if (keyCode == KeyEvent.KEYCODE_BUTTON_R1) {
                bridge.input.triggers.right = false
                lblShoulderTriggerRight.text = "Right"
            }

            bridge.trySendingData()

            return true
        }

        return super.onKeyUp(keyCode, event)
    }
}
