package s.yarlykov.izisandbox.extensions

import android.graphics.Rect
import android.view.View
import kotlin.math.abs


/**
 * Используется для поиска View, которая должна получить touch event.
 * Выбираем View, чей видимый rect принимает координаты касания и у которого
 * наименьшая ширина.
 */
fun Map<Rect, View>.findMostSuitable(x: Float, y: Float): View? {

    var suitable: Pair<Rect, View>? = null

    entries.forEach {
        val (r, v) = it
        if (r.contains(x.toInt(), y.toInt())) {

            if (suitable == null) {
                suitable = r to v
            } else {
                if (abs(r.width()) < abs(suitable!!.first.width())) {
                    suitable = r to v
                }
            }
        }
    }

    return suitable?.second
}