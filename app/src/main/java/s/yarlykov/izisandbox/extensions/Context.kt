package s.yarlykov.izisandbox.extensions

import android.content.Context

/**
 * Прочитать ресурс dimen и вернуть его значение в px
 */
fun Context.dimensionPix(dimenId: Int): Int {
    return this.resources.getDimensionPixelOffset(dimenId)
}