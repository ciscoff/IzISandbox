package s.yarlykov.izisandbox.extensions

import android.content.res.Resources

/**
 * Dp to Pix
 * Pix to Dp
 */
fun Int.doDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Или в такой форме
 */

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * Int - это количество часов, которые конвертирууем в минуты
 */
val Int.minutes: Int
    get() = this * 60

/**
 * Округлить до ближайшего кратного аргументу arg
 */
fun Int.multipleOf(arg: Int): Int {

    val remainder = this % arg
    val diff = arg - remainder

    return if (remainder != 0) {
        if (remainder >= diff)
            this + diff
        else
            this - remainder
    } else this
}

/**
 * Минуты в строку вида 07:15
 */
fun Int.hhMm(): String {
    val h = this / 60
    val m = this % 60
    return "${"%02d".format(h)}:${"%02d".format(m)}"
}

/**
 * Минуты в строки вида "1 h 20 min", "20 min"
 */
fun Int.hhMmFormatted(): String {

    return if (this < 60) {
        "${"%02d".format(this)} min"
    } else {
        "${"%02d".format(this / 60)} h  ${"%02d".format(this % 60)} min"
    }
}