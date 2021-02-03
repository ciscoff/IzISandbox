package s.yarlykov.izisandbox.matrix.avatar_maker.gesture

import android.graphics.RectF
import s.yarlykov.izisandbox.matrix.avatar_maker.*

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
 * На протяжении всего жеста (от ACTION_DOWN до ACTION_UP) значение scaleRemain не изменяется.
 * Изменяется только дистанция от угла TapArea
 *
 *
 *
 */
data class Gesture(val tapCorner: TapCorner, val distMax: Float, val bounds: RectF) {

    // Это X-дистанция пальца от точки tapCorner.cornerX
    var distPrev = tapCorner.tapX - tapCorner.cornerX
    var scalingMode: Mode.Scaling = Mode.Scaling.Init

    /**
     * После каждого ACTION_MOVE нужно определить режим скалирования: растягиваем/сжимаем
     */
    fun scalingSubMode(x: Float): Mode.Scaling {
        val distCurrent = x - tapCorner.tapX

        scalingMode = when (tapCorner.tapArea) {
            is lt, is lb -> {
                if (distCurrent > distPrev) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
            }
            is rt, is rb -> {
                if (distCurrent < distPrev) Mode.Scaling.Squeeze else Mode.Scaling.Shrink
            }
        }

        distPrev = distCurrent
        return scalingMode
    }

    /**
     * В этом методе нужно определить:
     * - если сжимаемся, то не уменьшаемся меньше допустимого.
     * - если растягиваемся, то не выходим за границу bounds.
     */
    fun confirmedOffset(proposedOffsetX: Float): Float {

        return when (scalingMode) {
            Mode.Scaling.Squeeze -> {
                // TODO distMax должна иметь знак наверное
                if (distPrev + proposedOffsetX >= distMax)
                    (distMax - distPrev)
                else
                    proposedOffsetX
            }
            Mode.Scaling.Shrink -> {
                val pointX = tapCorner.cornerX + distPrev + proposedOffsetX

                when {
                    (pointX < bounds.left) -> {
                        bounds.left - (tapCorner.cornerX + distPrev)
                    }
                    (pointX > bounds.right) -> {
                        bounds.right - (tapCorner.cornerX + distPrev)
                    }
                    else -> proposedOffsetX
                }


            }
            else -> proposedOffsetX

        }


    }
}
