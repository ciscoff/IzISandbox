package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import kotlin.math.abs
import kotlin.math.sign

/**
 * @param tapCorner - угол и координата первого касания
 * @param bitmapRatio - текущий видимый и минимальный размер битмапы (в отношении к реальному)
 * @param frameRatio - величина от 1 до 0.X, где 0.X - размер минимального квадрата
 * @param size - размер рамки
 */
data class ScaleDetector(
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
     * Дельта - начальное знаковое смещение точки касания от ближайшей вертикальной стороны рамки
     */
    private var dX = tapCorner.tap.x - tapCorner.pivot.x

    /**
     * Знаковая X-дистанция от пальца до точки tapCorner.cornerX в предыдущем событии.
     */
    private var prevDist = dX

    /**
     * Знаковая X-дистанция от пальца до точки tapCorner.cornerX в текущем событии.
     */
    private var currentDist = 0f


    private val distAvailable: Float = sign(direction.x) * size * (1f - frameRatio.min) + dX

    init {

        /**
         * TODO !!!
         * Когда битмапа уже сильно увеличена, то значение distAvailable приближается к 0 и
         * в новом Gesture может случиться так, что абсолютное значение prevDist превысит
         * абсолютное значение distAvailable. Этого быть не должно.
         */
//        if (abs(prevDist) > abs(distAvailable)) prevDist = distAvailable
    }

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
     * - если сжимаемся, то не уменьшаемся меньше допустимого distAvailable.
     * - если растягиваемся, то не выходим за границу bounds.
     *
     * @param proposedOffsetX - знаковое смещение от ПРЕДЫДУЩЕГО ПОЛОЖЕНИЯ,
     * а не от tapCorner.cornerX. Соотв нужно сравнивать со знаковым distMax
     *
     * NOTE: В состоянии когда зум максимальный (максимальное увеличение), то
     *  distAvailable == 0.0 и в этом случае нельзя делать Squeeze (ниже есть проверка)
     */

    private var isOverAvail = false
    private var squeezeDist = 0f

    fun onMove(proposedOffsetX: Float, proposedOffsetY: Float): Offset {

        var offsetX = proposedOffsetX

        return when (scalingMode) {
            // При сжатии не должны "перелететь" за distAvailable.
            Mode.Scaling.Squeeze -> {

                if (distAvailable != 0f) {

                    isOverAvail = if (direction.x > 0) {
                        currentDist >= distAvailable
                    } else {
                        currentDist <= distAvailable
                    }

                    if (isOverAvail) {
                        offsetX =
                            if (squeezeDist < distAvailable) distAvailable - squeezeDist else 0f
                        squeezeDist = distAvailable
                    } else {
                        squeezeDist = currentDist
                    }

                    prevDist = currentDist

                    if (offsetX == -0.0f || offsetX == 0.0f) {
                        emptyOffset
                    } else {
                        Offset(offsetX to abs(offsetX) * direction.y)
                    }

                } else emptyOffset

            }
            // При расширении не делаем никаких проверок (например выход за пределы
            // родительского View). Это выполнит внешний код.
            Mode.Scaling.Shrink -> {

                if (currentDist < squeezeDist) {
                    squeezeDist = currentDist
                }

                prevDist = currentDist
                Offset(proposedOffsetX to abs(proposedOffsetX) * sign(proposedOffsetY))
            }
            else -> {
                invalidOffset
            }
        }
    }

    // Это Bitmap Scale Ratio
    val squeezeRatioLeft: Float
        get() {
            val realOffsetX = squeezeDist - dX
            val frameScaleRatio = (size - realOffsetX)/size
            return bitmapRatio.max + frameScaleRatio
        }

    val shrinkRatio: Float
        get() = 1f // TODO

}