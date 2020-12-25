package s.yarlykov.izisandbox.extensions

import android.graphics.Rect
import android.graphics.RectF
import androidx.annotation.FloatRange
import kotlin.math.abs

/**
 * https://github.com/rock3r/uplift/blob/master/app/src/main/java/me/seebrock3r/elevationtester/OutlineProvider.kt#L22
 *
 * Здесь pivot - это центр исходного Rect
 *
 */
fun Rect.scale(
    @FloatRange(from = -1.0, to = 1.0) scaleX: Float,
    @FloatRange(from = -1.0, to = 1.0) scaleY: Float
) {
    val newWidth = width() * scaleX
    val newHeight = height() * scaleY
    val deltaX = (width() - newWidth) / 2
    val deltaY = (height() - newHeight) / 2

    set(
        (left + deltaX).toInt(),
        (top + deltaY).toInt(),
        (right - deltaX).toInt(),
        (bottom - deltaY).toInt()
    )
}

/**
 * Модифицированный вариант. Pivot - левый верхний угол
 */
fun RectF.scale(
    @FloatRange(from = 0.0, to = 1.0) scaleX: Float,
    @FloatRange(from = 0.0, to = 1.0) scaleY: Float
) {
    val newWidth = width() * scaleX
    val newHeight = height() * scaleY

    set(left, top, left + newWidth, top + newHeight)
}

/**
 * Используется для работы с прямоугольниками от ItemDecoration
 */
val Rect.min: Int
    get() = abs(listOf(left, top, right, bottom).min() ?: 0)




