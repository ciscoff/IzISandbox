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