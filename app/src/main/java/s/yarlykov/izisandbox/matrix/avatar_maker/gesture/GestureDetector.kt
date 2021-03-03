package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import s.yarlykov.izisandbox.extensions.normalize
import kotlin.math.abs
import kotlin.math.sign

/**
 * @param tapCorner - угол и координата первого касания
 * @param bitmapRatio - текущий видимый и минимальный размер битмапы (в отношении к реальному)
 * @param frameRatio - величина от 1 до 0.X, где 0.X - размер минимального квадрата
 * @param size - размер рамки
 */
data class GestureDetector(
    val tapCorner: TapCorner,
    val bitmapRatio: Ratio,
    val frameRatio: Ratio,
    val size: Float
) {

    private val invalidOffset = Offset(Float.MIN_VALUE to Float.MIN_VALUE)
    private val emptyOffset = Offset(0f to 0f)

    /**
     * Знак изменения значения координаты (+1, -1). Например, если тапнули в LB, то в режиме
     * Mode.Scaling.Squeeze X будет изменяться в "естественном" направлении от меньшего к
     * большему (+1), а Y - в противоположном : от большего к меньшему (-1)
     */
    val direction = when (tapCorner.tapArea) {
        is lt -> Direction(1f to 1f)
        is lb -> Direction(1f to -1f)
        is rt -> Direction(-1f to 1f)
        is rb -> Direction(-1f to -1f)
    }

    /**
     * Начальное знаковое смещение точки касания от ближайшей вертикальной стороны рамки
     */
    private val touchOffset = tapCorner.tap.x - tapCorner.pivot.x

    /**
     * Знаковая X-дистанция от пальца до точки tapCorner.cornerX в предыдущем событии.
     */
    private var prevDist = touchOffset

    /**
     * Знаковая X-дистанция от пальца до точки tapCorner.cornerX в текущем событии.
     */
    private var currentDist = 0f

    private var offsetAcc = 0f
    private var offsetSqueezeAvail: Float =
        (sign(direction.x) * size * (1f - frameRatio.min)).normalize()

    private var offsetShrinkAvail: Float =
        ((-1f) * sign(direction.x) * size * (1f - bitmapRatio.max) / bitmapRatio.max).normalize()

    private var isOverSqueeze = false
    private var isOverShrink = false

    /**
     * Режим скалирования
     */
    private var scalingMode: Mode.Scaling = Mode.Scaling.Init

    /**
     * После каждого ACTION_MOVE нужно определить режим скалирования: растягиваем/сжимаем
     * @param eventOffsetX - это знаковое смещение между предыдущим event.x и текущим event.x
     *
     * Есть проблема: если от LB делаем сжатие и выходим за пределы rectVisible, а потом идем
     * обратно, то в момент разворота prevDist == distAvailSigned, а на самом деле палец далеко
     * левее. В результате ...
     */
    fun detectScalingSubMode(eventOffsetX: Float): Mode.Scaling {

        currentDist = prevDist + eventOffsetX

        scalingMode = when (tapCorner.tapArea) {
            is lt, is lb -> {
                if (currentDist > prevDist) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
            }
            is rt, is rb -> {
                if (currentDist < prevDist) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
            }
        }

        return scalingMode
    }

    /**
     * В этом методе нужно определить:
     * - если сжимаемся, то не уменьшаемся меньше допустимого offsetSqueezeAvail.
     * - если растягиваемся, то не выходим за пределы offsetShrinkAvail.
     *
     * @param proposedOffsetX - знаковое смещение от ПРЕДЫДУЩЕГО ПОЛОЖЕНИЯ.
     */
    fun onMove(proposedOffsetX: Float, proposedOffsetY: Float): Offset {

        var offsetX = proposedOffsetX.normalize()
        if (offsetX == 0.0f) return emptyOffset

        return when (scalingMode) {
            // При сжатии не должны "перелететь" за offsetSqueezeAvail.
            //
            // NOTE: При сжатии важно чтобы смещение по Y соответствовало "знаку" direction.y
            Mode.Scaling.Squeeze -> {

                isOverSqueeze = if (direction.x > 0) {
                    (offsetAcc + offsetX) >= offsetSqueezeAvail
                } else {
                    (offsetAcc + offsetX) <= offsetSqueezeAvail
                }

                if (isOverSqueeze) {
                    offsetX =
                        if (offsetAcc < offsetSqueezeAvail)
                            (offsetSqueezeAvail - offsetAcc).normalize()
                        else
                            0f
                }

                offsetAcc += offsetX
                prevDist = currentDist

                if (offsetX == 0.0f) {
                    emptyOffset
                } else {
                    Offset(offsetX to abs(offsetX) * direction.y)
                }
            }
            // При расширении нужно возвращать proposedOffsetX, НО все равно вычислять
            // offsetX и вместе с ним offsetAcc. Благодаря этому мы можем растягивать рамку пальцем
            // без ограничений, но при отпускании пальца реальное скалирование будет расчитано
            // по значению offsetAcc.
            //
            // NOTE: При растягивании важно чтобы смещение по Y сохраняло свой исходный "знак"
            Mode.Scaling.Shrink -> {

                isOverShrink = if (direction.x > 0) {
                    (offsetAcc + offsetX) < offsetShrinkAvail
                } else {
                    (offsetAcc + offsetX) > offsetShrinkAvail
                }

                if (isOverShrink) {
                    offsetX =
                        if (offsetAcc > offsetShrinkAvail)
                            (offsetShrinkAvail - offsetAcc).normalize()
                        else
                            0f
                }

                offsetAcc += offsetX
                prevDist = currentDist

                Offset(proposedOffsetX to abs(proposedOffsetX) * sign(proposedOffsetY))
            }
            else -> {
                invalidOffset
            }
        }
    }

    // Это Bitmap Scale Ratio
    val scaleRatio: Float
        get() {
            val frameScaleRatio = (size - offsetAcc * sign(direction.x)) / size
            return bitmapRatio.max * frameScaleRatio
        }
}