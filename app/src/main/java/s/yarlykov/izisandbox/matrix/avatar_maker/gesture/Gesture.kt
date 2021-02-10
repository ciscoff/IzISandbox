package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.sign

/**
 * Палец идет от LB угла нижнего квадрата по диагонали вверх. При этом по этой же тректории
 * перемещается "фантом" начального квадрата образуя с ним пересечение, которое показано
 * заштрихованной областью. В какой-то момент сторона квадрата пересечения достигает минимального
 * значения и LB такого квадрата пересечения есть pointOfHeightMin.
 *
 * Когда происходит событие ACTION_DOWN, то мы знаем область тача, размер стороны начального
 * квадрата (rectClip) и hMin. Если мы тапнули LB, то тянуть палец должны в область RT и мы можем
 * сразу вычислить координаты pointOfHeightMin. Теперь при каждом ACTION_MOVE нужно измерять
 * расстояние от начальной LB до pointOfHeightMin. Идея в том, что до тех пор пока не достигнута
 * минимальная высота (или точка pointOfHeightMin), то мы можем как приближаться к ней, так и
 * удаляться. Но после достижения мы можем только ПРИБЛИЖАТЬСЯ к стартовой точке pointDown.
 * Это исключает "перетяг" когда продолжая тянуть палец вправо вверх мы снова начнем растягивать
 * квадрат.
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
 * задачей является контролировать процесс сжатия, чтобы не перейти через пороговое значение.
 *
 * При сжатии, независимо от того где находится pivot, всё действие направлено внутрь исходного
 * rectClip, поэтому здесь не может произойти выхода за границы родительского View (потому что
 * rectClip полностью внутри родительской View и он не растягивается).
 *
 * Растяжение данный класс отслеживать не может потому что ничего не знает о размерах и
 * положении rectClip внутри родительского View.
 */
data class Gesture(val tapCorner: TapCorner, val distMax: Float) {

    private val invalidOffset = Offset(Float.MIN_VALUE to Float.MIN_VALUE)

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

    /**
     * Режим скалировани
     */
    private var scalingMode: Mode.Scaling = Mode.Scaling.Init

    init {
        logIt("tapArea=${tapCorner.tapArea::class.java.simpleName}, distPrev=$prevDist, distMax=$distMax, direction=$direction")
    }

    /**
     * После каждого ACTION_MOVE нужно определить режим скалирования: растягиваем/сжимаем
     */
    fun detectScalingSubMode(dX: Float): Mode.Scaling {
        currentDist = prevDist + dX

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
     * - если сжимаемся, то не уменьшаемся меньше допустимого.
     * - если растягиваемся, то не выходим за границу bounds.
     *
     * @param proposedOffsetX - знаковое смещение от ПРЕДЫДУЩЕГО ПОЛОЖЕНИЯ,
     * а не от tapCorner.cornerX. Соотв нужно сравнивать со знаковым distMax
     */
    fun onMove(proposedOffsetX: Float, proposedOffsetY: Float): Offset {

        var offsetX = proposedOffsetX

        return when (scalingMode) {
            // При сжатии не должны "перелететь" за distMax.
            Mode.Scaling.Squeeze -> {
                if (direction.x > 0) {
                    if (prevDist + proposedOffsetX >= distMax) {
                        offsetX = distMax - prevDist
                        prevDist = distMax
                        isSqueezed = true
                    } else {
                        isSqueezed = false
                        prevDist = currentDist
                    }
                } else {
                    if (prevDist + proposedOffsetX <= distMax * direction.x) {
                        offsetX = distMax - prevDist
                        prevDist = distMax * direction.x
                        isSqueezed = true
                    } else {
                        isSqueezed = false
                        prevDist = currentDist
                    }
                }
                Offset(offsetX to abs(offsetX) * direction.y)
            }
            // При расширении не делаем никаких проверок (например выход за пределы
            // родительского View). Это выполнит внешний код.
            Mode.Scaling.Shrink -> {
                prevDist = currentDist
                isSqueezed = false

                val d = abs(proposedOffsetX)
                Offset(d * sign(proposedOffsetX) to d * sign(proposedOffsetY))
            }
            else -> {
                isSqueezed = false
                invalidOffset
            }
        }
    }

    var isSqueezed: Boolean = false
        private set

    // Это на сколько сдвинули от начального положения
    val ratioPassed: Float
        get() = prevDist / distMax

    // Это сколько осталось до конечного положения (подходит для зума битмапы)
    val ratioLeft: Float
        get() = (distMax - prevDist) / distMax
}
