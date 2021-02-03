package s.yarlykov.izisandbox.matrix.avatar_maker.v4

import android.graphics.PointF
import android.graphics.RectF
import s.yarlykov.izisandbox.matrix.avatar_maker.*
import kotlin.math.abs
import kotlin.math.sign

enum class Direction {
    MoreClosure,
    MoreAway
}

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
    var distPrev = abs(tapCorner.tapX - tapCorner.cornerX)
    var distSign = sign(tapCorner.tapX - tapCorner.cornerX)
    val distSigned: Float
        get() = distPrev * distSign

    /**
     * После каждого ACTION_MOVE нужно определить режим скалирования: растягиваем/сжимаем
     */
    fun scalingSubMode(x: Float): Mode.Scaling {
        val distCurrent = abs(x - tapCorner.tapX)

        val mode = when (tapCorner.tapArea) {
            is lt, is lb -> {
                // Находимся правее левой границы rectClip
                if (x > tapCorner.cornerX && distCurrent > distPrev) {
                    Mode.Scaling.Squeeze
                }
                // Находимся правее левой границы rectClip
                else if (x > tapCorner.cornerX && distCurrent < distPrev) {
                    Mode.Scaling.Shrink
                }
                // Находимся левее левой границы rectClip
                else /*if(x < tapCorner.cornerX)*/ {
                    Mode.Scaling.Shrink
                }
            }
            is rt, is rb -> {
                // Находимся левее правой границы rectClip
                if (x < tapCorner.cornerX && distCurrent > distPrev) {
                    Mode.Scaling.Squeeze
                }
                // Находимся левее правой границы rectClip
                else if (x < tapCorner.cornerX && distCurrent > distPrev) {
                    Mode.Scaling.Shrink
                }
                // Находимся правее правой границы rectClip
                else /*if(x > tapCorner.cornerX)*/ {
                    Mode.Scaling.Shrink
                }
            }
        }

        distPrev = distCurrent
        return mode
    }

    fun confirmedOffset(proposedOffsetX: Float): Float {

        when (tapCorner.tapArea) {
            is lt, is lb -> {
                // Находимся правее левой границы rectClip
                if (distSigned + proposedOffsetX > distMax) {
                    distMax - distPrev
                }
                // Находимся левее левой границы rectClip
                else if (distPrev * distSign + proposedOffsetX < bounds.left) {
                    Mode.Scaling.Shrink
                } else {
                    proposedOffsetX
                }
            }
            is rt, is rb -> {
                // Находимся левее правой границы rectClip
                if (x < tapCorner.cornerX && distCurrent > distPrev) {
                    Mode.Scaling.Squeeze
                }
                // Находимся левее правой границы rectClip
                else if (x < tapCorner.cornerX && distCurrent > distPrev) {
                    Mode.Scaling.Shrink
                }
                // Находимся правее правой границы rectClip
                else /*if(x > tapCorner.cornerX)*/ {
                    Mode.Scaling.Shrink
                }
            }
        }
    }
}
