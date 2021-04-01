package s.yarlykov.izisandbox.ui

import android.animation.ArgbEvaluator
import android.animation.TypeEvaluator

/**
 * Из двух массивов ОДИНАКОВОЙ длины собираем третий, такой же длины, прогоняя элементы
 * исходных массивов через evaluate. fraction предоставляется ValueAnimator'ом.
 *
 * evaluate(...):
 *
 * startValue: [colorA1, colorA2, colorA3]  -
 *                                          -> Loop: argbEvaluator.evaluate(f, colorAx, colorBx) -> [colorA1B1', colorA2B2', colorA3B3']
 * endValue:   [colorB1, colorB2, colorB3]  -
 */
class GradientArgbEvaluator : TypeEvaluator<IntArray> {

    private val argbEvaluator = ArgbEvaluator()

    override fun evaluate(fraction: Float, startValue: IntArray, endValue: IntArray): IntArray {

        // LOG
        //logIt("fraction=$fraction, startValue=${startValue.printable}, endValue=${endValue.printable}")

        require(startValue.size == endValue.size)

        return startValue.mapIndexed { index, item ->
            argbEvaluator.evaluate(fraction, item, endValue[index]) as Int
        }.toIntArray()
    }
}

val IntArray.printable: String
    get() {
        val sb = StringBuilder()
        forEach { sb.append("${it.toString(16)} ") }
        return sb.toString()
    }