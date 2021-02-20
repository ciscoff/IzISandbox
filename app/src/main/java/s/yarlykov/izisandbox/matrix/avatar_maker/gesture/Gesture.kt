package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import android.graphics.RectF
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.sign

/**
 * Пример жеста сжатия:
 * Палец идет от LB угла нижнего квадрата по диагонали вверх. При этом по этой же траектории
 * перемещается "фантом" начального квадрата образуя с ним пересечение, которое показано
 * заштрихованной областью. В какой-то момент сторона квадрата пересечения достигает минимального
 * значения, а длина проекции на ось Х траектории пальца - величины distAvailable.
 *
 * Когда происходит событие ACTION_DOWN, то мы знаем область тача, размер стороны начального
 * квадрата rectClip (то есть distMax). Если мы тапнули LB, то для сжатия тянем палец в
 * положительном направлении Х (направление Y любое). Теперь при каждом ACTION_MOVE нужно измерять
 * расстояние от начальной LB, то есть дистанцию prevDist. Идея в том, что пока не достигнута
 * distAvailable, мы можем как приближаться к ней, так и удаляться - растягивая и сжимая рамку.
 * Но после достижения distAvailable и продолжении движения пальца по оси +Х рамка перестает
 * сжиматься. Это исключает "перетяг" когда продолжая тянуть палец вправо вверх мы снова начнем
 * растягивать квадрат.
 */
//                  _____________________
//                 |                     |
//                 |                     |
//                 |                     |
//       __________|__________           |
//      |          | . . . . .|          |
//      |          |. . . . . |hMin      |
//      |          | . . . . .|          |
//      |          0__________|__________|
//      |                     |
//      |hCurrent             |
//      |                     |
//      0_____________________|
//   touchDown

/**
 * Класс Gesture отслеживает все передвижения пальца от ACTION_DOWN до ACTION_UP и его
 * задачей является контролировать процесс СЖАТИЯ, чтобы не перейти через пороговое значение.
 *
 * При сжатии, независимо от того где находится pivot, всё действие направлено внутрь исходного
 * rectClip, поэтому здесь не может произойти выхода за границы родительского View (потому что
 * rectClip полностью внутри родительской View и он не растягивается).
 *
 * Растяжение данный класс отслеживать не может потому что ничего не знает положении rectClip
 * внутри родительского View.
 *
 * @param tapCorner - область тача
 * @param distAvailable - разрешенная дистанция для squeeze
 * @param distMax - длина стороны квадрата
 */
data class Gesture(
    val tapCorner: TapCorner,
    val distAvailable: Float,
    val distMax: Float,
    val bounds: RectF
) {

    private val invalidOffset = Offset(Float.MIN_VALUE to Float.MIN_VALUE)
    private val emptyOffset = Offset(0f to 0f)

    /**
     * X-дистанция от пальца до точки tapCorner.cornerX в предыдущем событии.
     */
    private var prevDist = tapCorner.tap.x - tapCorner.pivot.x

    /**
     * X-дистанция от пальца до точки tapCorner.cornerX в текущем событии.
     */
    private var currentDist = 0f

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

    init {

        /**
         * Когда битмапа уже сильно увеличена, то значение distAvailable приближается к 0 и
         * в новом Gesture может случиться так, что абсолютное значение prevDist превысит
         * абсолютное значение distAvailable. Этого быть не должно.
         */
        if (abs(prevDist) > abs(distAvailable)) prevDist = distAvailable
    }

    /**
     * distAvailable и distMax с учетом направления их вектора по оси Х. То есть это
     * значение со знаком.
     */
    private val distMaxSigned: Float
        get() = distMax * direction.x

    private val distAvailSigned: Float
        get() = distAvailable * direction.x

    /**
     * Режим скалировани
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


                if (/*prevDist != distAvailSigned && */distAvailable != 0f) {

                    logIt("WWWW Squeeze: prevDist=$prevDist, currentDist=$currentDist, squeezeDist=$squeezeDist, distAvailSigned=$distAvailSigned")

                    isOverAvail = if (direction.x > 0) {
                        currentDist >= distAvailSigned
                    } else {
                        currentDist <= distAvailSigned
                    }

                    if (isOverAvail) {
                        offsetX =
                            if (squeezeDist < distAvailSigned) distAvailSigned - squeezeDist else 0f
                        squeezeDist = distAvailSigned
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

//                if (prevDist != distAvailSigned && distAvailable != distMax) {
//
//                    isOverAvail = if (direction.x > 0) {
//                        prevDist + proposedOffsetX >= distAvailSigned
//                    } else {
//                        prevDist + proposedOffsetX <= distAvailSigned
//                    }
//
//                    if (isOverAvail) {
//                        offsetX = distAvailSigned - prevDist
//                        prevDist = distAvailSigned
//                    } else {
//                        prevDist = currentDist
//                    }
//
//                    logIt("Squeeze: prevDist=$prevDist, distAvailSigned=$distAvailSigned")
//
//                    if (offsetX == -0.0f || offsetX == 0.0f) {
//                        emptyOffset
//                    } else {
//                        Offset(offsetX to abs(offsetX) * direction.y)
//                    }
//
//                } else emptyOffset
            }
            // При расширении не делаем никаких проверок (например выход за пределы
            // родительского View). Это выполнит внешний код.
            Mode.Scaling.Shrink -> {

                if (currentDist < squeezeDist) {
                    squeezeDist = currentDist
                }

                prevDist = currentDist
                logIt("WWWW Shrink: prevDist=$prevDist, currentDist=$currentDist, squeezeDist=$squeezeDist, distAvailSigned=$distAvailSigned")
                Offset(proposedOffsetX to abs(proposedOffsetX) * sign(proposedOffsetY))
            }
            else -> {
                invalidOffset
            }
        }
    }

    //
    //     prevDist(4)  distAvailable (20)
    //     0--->--------------->/////////|
    //       ^            ^           distMax(30)
    //     passed        left [= distMax - prevDist]

    // Это на сколько сдвинули от начального положения
    val squeezeRatioPassed: Float
        get() = prevDist / distMaxSigned

    // Это сколько осталось (но не превышая distAvailable)
    val squeezeRatioLeft: Float
        get() = (distMaxSigned - squeezeDist) / distMaxSigned

    val shrinkRatio: Float
        get() = (abs(prevDist) + abs(distMax)) / abs(distMax)
}
