package com.tejashah88.triforcecontrol

const val DEFAULT_PRECISION: Int = 3
const val sep = "\n"
const val join = ","
const val valSep = "="

fun Boolean.toInt() = if (this) 1 else 0
fun Float.format(digits: Int = DEFAULT_PRECISION) = "%.${digits}f".format(this)

fun Boolean.payloadify(key: String): String = "$key$valSep${this.toInt()}$sep"
fun Float.payloadify(key: String): String = if (this == 0f) "$key${valSep}0$sep" else "$key$valSep${this.format()}$sep"

data class LetterButtons(
    var A: Boolean = false,
    var B: Boolean = false,
    var X: Boolean = false,
    var Y: Boolean = false
)

fun LetterButtons.diffString(other: LetterButtons): String {
    var diffString = "BTN:"
    val oldDiffString = "" + diffString

    if (other.A != this.A) diffString += other.A.payloadify("A")
    if (other.B != this.B) diffString += other.B.payloadify("B")
    if (other.X != this.X) diffString += other.X.payloadify("X")
    if (other.Y != this.Y) diffString += other.Y.payloadify("Y")

    return if (diffString == oldDiffString) "" else diffString.trim().replace(sep, join)
}

data class DpadButtons(
    var up    : Boolean = false,
    var down  : Boolean = false,
    var left  : Boolean = false,
    var right : Boolean = false
)

fun DpadButtons.diffString(other: DpadButtons): String {
    var diffString = "DPAD:"
    val oldDiffString = "" + diffString

    if (other.up    != this.up)     diffString += other.up.payloadify("U")
    if (other.down  != this.down)   diffString += other.down.payloadify("D")
    if (other.left  != this.left)   diffString += other.left.payloadify("L")
    if (other.right != this.right)  diffString += other.right.payloadify("R")

    return if (diffString == oldDiffString) "" else diffString.trim().replace(sep, join)
}

data class ShoulderTriggers(
    var left  : Boolean = false,
    var right : Boolean = false
)

fun ShoulderTriggers.diffString(other: ShoulderTriggers): String {
    var diffString = "TRIG:"
    val oldDiffString = "" + diffString

    if (other.left  != this.left)   diffString += other.left.payloadify("L")
    if (other.right != this.right)  diffString += other.right.payloadify("R")

    return if (diffString == oldDiffString) "" else diffString.trim().replace(sep, join)
}

data class Joystick(
    var side       : String,
    var vertical   : Float = 0.0f,
    var horizontal : Float = 0.0f
)

fun Joystick.diffString(other: Joystick): String {
    var diffString = "JOY-${other.side}:"
    val oldDiffString = "" + diffString

    if (other.vertical.format()   != this.vertical.format())   diffString += other.vertical.payloadify("V")
    if (other.horizontal.format() != this.horizontal.format()) diffString += other.horizontal.payloadify("H")

    return if (diffString == oldDiffString) "" else diffString.trim().replace(sep, join)
}

data class ShoulderBrakes(
    var left  : Float = 0.0f,
    var right : Float = 0.0f
)

fun ShoulderBrakes.diffString(other: ShoulderBrakes): String {
    var diffString = "BRAKE:"
    val oldDiffString = "" + diffString

    if (other.left.format()  != this.left.format())  diffString += other.left.payloadify("L")
    if (other.right.format() != this.right.format()) diffString += other.right.payloadify("R")

    return if (diffString == oldDiffString) "" else diffString.trim().replace(sep, join)
}

data class ControllerInput(
    var buttons: LetterButtons = LetterButtons(),
    var dpad: DpadButtons = DpadButtons(),
    var triggers: ShoulderTriggers = ShoulderTriggers(),
    var leftJoystick: Joystick = Joystick("L"),
    var rightJoystick: Joystick = Joystick("R"),
    var brakes: ShoulderBrakes = ShoulderBrakes()
)

fun ControllerInput.diffString(other: ControllerInput): String {
    return arrayOf(
        buttons.diffString(other.buttons),
        dpad.diffString(other.dpad),
        triggers.diffString(other.triggers),
        leftJoystick.diffString(other.leftJoystick),
        rightJoystick.diffString(other.rightJoystick),
        brakes.diffString(other.brakes)
    ).joinToString(sep).trim().replace(Regex("\\s+"), sep).replace(sep, ";")
}

fun ControllerInput.duplicate(
    buttons: LetterButtons = this.buttons.copy(),
    dpad: DpadButtons = this.dpad.copy(),
    triggers: ShoulderTriggers = this.triggers.copy(),
    leftJoystick: Joystick = this.leftJoystick.copy(),
    rightJoystick: Joystick = this.rightJoystick.copy(),
    brakes: ShoulderBrakes = this.brakes.copy()
) = ControllerInput(
    buttons,
    dpad,
    triggers,
    leftJoystick,
    rightJoystick,
    brakes
)