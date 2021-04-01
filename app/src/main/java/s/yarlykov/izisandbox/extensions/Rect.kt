package s.yarlykov.izisandbox.extensions

import android.graphics.Point
import android.graphics.PointF
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
 * Используется для работы с прямоугольниками от ItemDecoration.
 * Там могут присутствовать отрицательные значения.
 */
val Rect.min: Int
    get() = listOf(left, top, right, bottom).filter { it != 0 }.map { abs(it) }.min() ?: 0


fun RectF.reset() {
    set(0f, 0f, 0f, 0f)
}

val RectF.center: PointF
    get() = PointF(left + width() / 2f, top + height() / 2f)

val RectF.lt: PointF
    get() = PointF(left, top)

val RectF.rt: PointF
    get() = PointF(right, top)

val RectF.lb: PointF
    get() = PointF(left, bottom)

val RectF.rb: PointF
    get() = PointF(right, bottom)

val Rect.center: Point
    get() = Point(left + width() / 2, top + height() / 2)










