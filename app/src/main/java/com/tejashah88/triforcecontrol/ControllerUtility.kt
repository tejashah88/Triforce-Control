package com.tejashah88.triforcecontrol

import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

class ControllerUtility {
    companion object Shared {
        // Check that input comes from a device with letter buttons
        internal fun fromGamePad(event: KeyEvent): Boolean =
            event.source and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD

        ////////////////////////////////////////
        // Multiple Game Controller Functions //
        ////////////////////////////////////////

        // Checks if a given InputDevice is actually a compatible game controller
        private fun isGamePad(device: InputDevice): Boolean =
            device.sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD
                    || device.sources and InputDevice.SOURCE_DPAD == InputDevice.SOURCE_DPAD
                    || device.sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK

        // Retrieves a list of connected game controllers
        internal fun getGameControllerIds(): List<Int> =
            InputDevice.getDeviceIds().filter { deviceId -> isGamePad(InputDevice.getDevice(deviceId)) }

        ////////////////////
        // Dpad Functions //
        ////////////////////

        // Check that input comes from a device with directional pads
        // TODO: HOW THE HECK DOES THAT WORK??
        internal fun fromDpadDevice(event: MotionEvent): Boolean =
            event.source and InputDevice.SOURCE_DPAD != InputDevice.SOURCE_DPAD

        // Get D-PAD button that was pressed
        internal fun getDirectionPressed(event: MotionEvent): FloatArray {
            if (!fromDpadDevice(event)) return floatArrayOf(0f, 0f)

            val xAxis: Float = event.getAxisValue(MotionEvent.AXIS_HAT_X)
            // Note: minus sign added for vertical (HAT_Y) axis to standardize "up" and "right" as positive values
            val yAxis: Float = -event.getAxisValue(MotionEvent.AXIS_HAT_Y)

            return floatArrayOf(xAxis, yAxis)

            /*directionPressed = when {
                // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad LEFT and RIGHT direction accordingly.
                xAxis.compareTo(-1.0f) == 0 -> DPad.LEFT
                xAxis.compareTo(1.0f) == 0 -> DPad.RIGHT
                // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad UP and DOWN direction accordingly.
                yAxis.compareTo(-1.0f) == 0 -> DPad.UP
                yAxis.compareTo(1.0f) == 0 -> DPad.DOWN
                else -> directionPressed
            }*/
        }

        ////////////////////////
        // Joystick Functions //
        ////////////////////////

        // Get calibrated value from given analog axis
        private fun getCenteredAxis(event: MotionEvent, axis: Int, historyPos: Int): Float {
            val range: InputDevice.MotionRange? = event.device.getMotionRange(axis, event.source)

            // A joystick at rest does not always report an absolute position of
            // (0,0). Use the getFlat() method (or 'flat' property accessor) to
            // determine the range of values bounding the joystick axis center.
            range?.apply {
                val value: Float = if (historyPos < 0)
                    event.getAxisValue(axis)
                else
                    event.getHistoricalAxisValue(axis, historyPos)

                // Ignore axis values that are within the 'flat' region of the joystick axis center.
                if (Math.abs(value) > flat)
                    return value
            }

            return 0f
        }

        // Check that input comes from a device with joysticks and/or brakes
        internal fun fromJoystickDevice(event: MotionEvent): Boolean =
            event.source and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
                    && event.action == MotionEvent.ACTION_MOVE

        // Get historic values of left joystick axes
        internal fun getLeftJoystickHistory(event: MotionEvent, deadzone: Float = 0f): List<FloatArray> =
            (0 until event.historySize).map { getLeftJoystickAtPos(event, it, deadzone) }

        // Get historic values of right joystick axes
        internal fun getRightJoystickHistory(event: MotionEvent, deadzone: Float = 0f): List<FloatArray> =
            (0 until event.historySize).map { getRightJoystickAtPos(event, it, deadzone) }

        internal fun getLeftJoystickCurrent(event: MotionEvent, deadzone: Float = 0f): FloatArray =
            getLeftJoystickAtPos(event, -1, deadzone)

        internal fun getRightJoystickCurrent(event: MotionEvent, deadzone: Float = 0f): FloatArray =
            getRightJoystickAtPos(event, -1, deadzone)

        // Maps a given number from an input range to an output range
        // Used for normalizing the deadzoned values as if deadzoning never occurred
        private fun mapNumberRange(input: Float, inputRange: FloatArray, outputRange: FloatArray): Float =
            outputRange[0] + ((outputRange[1] - outputRange[0]) / (inputRange[1] - inputRange[0])) * (input - inputRange[0])

        // Return a normalized value where if the value is less than the deadzone, it'll be 0
        // Otherwise, the value will be mapped from the deadzone starting value to 1.0f
        private fun factorDeadzone(rawValues: FloatArray, deadzone: Float): FloatArray {
            return rawValues.map {
                if (Math.abs(it) <= deadzone) 0f
                else mapNumberRange(Math.abs(it), floatArrayOf(deadzone, 1f), floatArrayOf(0f, 1f)) * Math.signum(it)
            }.toFloatArray()
        }

        // Same as above method except only taking and spitting out a single float
        private fun factorDeadzone(rawValue: Float, deadzone: Float): Float {
            return if (Math.abs(rawValue) <= deadzone) 0f
            else mapNumberRange(Math.abs(rawValue), floatArrayOf(deadzone, 1f), floatArrayOf(0f, 1f)) * Math.signum(
                rawValue
            )
        }

        // Gets normalized left joystick values as a 2-element array
        // Note: minus sign added for vertical (Y) axis to standardize "up" and "right" as positive values
        private fun getLeftJoystickAtPos(event: MotionEvent, historyPos: Int, deadzone: Float = 0f): FloatArray {
            val leftValues = floatArrayOf(
                ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_X, historyPos),
                -ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_Y, historyPos)
            )

            return if (deadzone > 0) factorDeadzone(leftValues, deadzone) else leftValues
        }

        // Gets normalized right joystick values as a 2-element array
        // Note: minus sign added for vertical (RZ) axis to standardize "up" and "right" as positive values
        private fun getRightJoystickAtPos(event: MotionEvent, historyPos: Int = -1, deadzone: Float = 0f): FloatArray {
            val rightValues = floatArrayOf(
                ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_Z, historyPos),
                -ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_RZ, historyPos)
            )

            return if (deadzone > 0) factorDeadzone(rightValues, deadzone) else rightValues
        }

        /////////////////////
        // Trigger Buttons //
        /////////////////////

        internal fun getLeftBrakeCurrent(event: MotionEvent, deadzone: Float = 0f): Float =
            getLeftBrakeAtPos(event, -1, deadzone)

        internal fun getRightBrakeCurrent(event: MotionEvent, deadzone: Float = 0f): Float =
            getRightBrakeAtPos(event, -1, deadzone)

        // Get historic values of left joystick axes
        internal fun getLeftBrakeHistory(event: MotionEvent, deadzone: Float = 0f): List<Float> =
            (0 until event.historySize).map { getLeftBrakeAtPos(event, it, deadzone) }

        // Get historic values of right joystick axes
        internal fun getRightBrakeHistory(event: MotionEvent, deadzone: Float = 0f): List<Float> =
            (0 until event.historySize).map { getRightBrakeAtPos(event, it, deadzone) }

        private fun getLeftBrakeAtPos(event: MotionEvent, historyPos: Int, deadzone: Float = 0f): Float {
            val leftValue = ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_LTRIGGER, historyPos)
            return if (deadzone > 0) factorDeadzone(leftValue, deadzone) else leftValue
        }

        private fun getRightBrakeAtPos(event: MotionEvent, historyPos: Int = -1, deadzone: Float = 0f): Float {
            val rightValue = ControllerUtility.getCenteredAxis(event, MotionEvent.AXIS_RTRIGGER, historyPos)
            return if (deadzone > 0) factorDeadzone(rightValue, deadzone) else rightValue
        }
    }
}
