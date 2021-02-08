package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import android.graphics.PointF

/**
 * Класс для хранения смещений по осям. Создан для удобства, чтобы
 * указывать названия осей: X/Y, а не просто first/second как в Pair.
 */
class Offset(pair: Pair<Float, Float>) : PointF(pair.first, pair.second)
