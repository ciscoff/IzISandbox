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
 * Округлить до ближайшего числа, кратного значению multiplicity
 */
fun Int.roundTo(multiplicity: Int): Int {

    val remainder = this % multiplicity
    val diff = multiplicity - remainder

    return if (remainder != 0) {
        if (remainder >= diff)
            this + diff
        else
            this - remainder
    } else this
}

/**
 * Минуты в строку вида 07:15
 *
 * Для улучшения читабельности округляем исходное число до ближайшего приближенного, кратного
 * значению multiplicity. В результате будем получать строки вида 13:00-14:00, вместо 13:00-13:59.
 */
fun Int.hhMm(multiplicity : Int = 5): String {

    val time = this.roundTo(multiplicity)

    val h = time / 60
    val m = time % 60
    return "${"%02d".format(h)}:${"%02d".format(m)}"
}

/**
 * Минуты в строки вида "1 h 20 min", "20 min"
 */
fun Int.hhMmFormatted(multiplicity : Int = 5): String {

    val time = this.roundTo(multiplicity)

    return if (time < 60) {
        "${"%02d".format(time)} min"
    } else {
        "${"%02d".format(time / 60)} h  ${"%02d".format(time % 60)} min"
    }
}