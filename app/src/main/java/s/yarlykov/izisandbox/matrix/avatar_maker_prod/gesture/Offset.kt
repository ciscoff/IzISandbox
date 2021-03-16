package s.yarlykov.izisandbox.matrix.avatar_maker_prod.gesture

import android.graphics.PointF

/**
 * Класс для хранения смещений по осям. Создан для удобства, чтобы
 * указывать названия осей: X/Y, а не просто first/second как в Pair.
 */
class Offset(pair: Pair<Float, Float>) : PointF(pair.first, pair.second)

val Offset.invalid: Boolean
    get() = x == Float.MAX_VALUE ||
            x == Float.MIN_VALUE ||
            y == Float.MIN_VALUE ||
            y == Float.MAX_VALUE

val Offset.zero: Boolean
    get() = (x == 0f || x == -0f) && (y == 0f || y == -0f)
