package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import android.graphics.PointF
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs

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
 * rectClip, поэтому здесь не может произойти выхода за границы родительского View.
 *
 * Растяжение данный класс отслеживать не может потому что ничего не знает о размерах и
 * положении rectClip внутри родительского View.
 */
data class Gesture(val tapCorner: TapCorner, val distMax: Float) {

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
        is lb -> Direction(1f to (-1f))
        is rt -> Direction(-1f to 1f)
        is rb -> Direction(-1f to (-1f))
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
    fun detectScalingSubMode(x: Float): Mode.Scaling {
        currentDist = x - tapCorner.pivot.x

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
    fun confirmSqueezeOffset(proposedOffsetX: Float): PointF {

        if (scalingMode != Mode.Scaling.Squeeze) return PointF(0f, 0f)

        var offsetX = proposedOffsetX

        if (direction.x > 0) {
            if (prevDist + proposedOffsetX >= distMax) {
                offsetX = distMax - prevDist
                prevDist = distMax
            } else prevDist = currentDist
        } else {
            if (prevDist + proposedOffsetX <= distMax * direction.x) {
                offsetX = distMax - prevDist
                prevDist = distMax * direction.x
            } else prevDist = currentDist
        }

        return PointF(offsetX, abs(offsetX) * direction.y)
    }
}