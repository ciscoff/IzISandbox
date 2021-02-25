package s.yarlykov.izisandbox.matrix.avatar_maker.v5

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker.BitmapPreScaleParams
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs

/**
 * Класс управляет масштабом и позиционированием фонового рисунка.
 */
class AvatarBackViewV5 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV5(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground: Paint? = null
    private val scaleMatrix = Matrix()

    private val rectTemp = Rect()
    private val rectFrom = RectF()
    private val rectTo = RectF()

    /**
     * При максимальном увеличении размера rectBitmapVisible не удается сделать его по высоте
     * равным bitmapHeight с точностью до пикселя. Поэтому если разница высот в пределах
     * measurementError, то считается что они равны и scaleController.scaleMax = 1
     */
    private val measurementError = context.resources.getInteger(R.integer.measurement_error)

    /**
     * Данные, которые нужно собрать перед началом анимации.
     */
    private var preScaleParams: BitmapPreScaleParams? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    /**
     * 1. Первый шаг в цикле анимации скалирования. Подготовить данные.
     *
     * @param factor:
     * - ПРИ Squeeze показывает реальный scale для видимой части битмапы, то есть после анимации
     * отношение rectBitmapVisible.height() /  sourceImageBitmap.height будет равно factor.
     *
     * - ПРИ РАСТЯЖЕНИИ ......
     */
    override fun onPreAnimate(factor: Float, pivot: PointF) {

        // Нужно сконвертировать pivot из координат view в координаты битмапы.
        // Сначала определяем отношение между двумя pivot'ами с точки зрения соотношения
        // сторон прямоугольников. Затем переносим pivot на координаты битмапы.
        val pivotBitmap = PointF(0f, 0f).apply {

            val ratioX = rectBitmapVisible.width().toFloat() / rectVisible.width()
            val ratioY = rectBitmapVisible.height().toFloat() / rectVisible.height()

            offset(
                rectBitmapVisible.left + pivot.x * ratioX,
                rectBitmapVisible.top + pivot.y * ratioY
            )
        }

        val pivotRatioX = (pivotBitmap.x - rectBitmapVisible.left) / rectBitmapVisible.width()
        val pivotRatioY = (pivotBitmap.y - rectBitmapVisible.top) / rectBitmapVisible.height()

        val startW = rectBitmapVisible.width()
        val startH = rectBitmapVisible.height()

        preScaleParams = BitmapPreScaleParams(pivotBitmap, pivotRatioX, pivotRatioY, startW, startH)

        /**
         * Теперь нужно factor (который показывает абсолютное значение scale) сконвертировать
         * в относительное значение scale. Под относительным отношением scale понимается
         * следующее: как нужно отскалировать rectBitmapVisible.height от её текущего значения,
         * до её нового значения. Текущее значение rectBitmapVisible.height принимается за 1.
         */
        val postScaleVisibleHeight = sourceImageBitmap!!.height * factor

        // от текущего состояния rectBitmapVisible
        scaleFrom = 1f
        // до нового состояния
        scaleTo = postScaleVisibleHeight / rectBitmapVisible.height()

        logIt("onPreAnimate abs_factor=$factor, relative_factor=${postScaleVisibleHeight / rectBitmapVisible.height()}")

        lastFactor = factor // TODO ??
    }

    /**
     * 2. Отдельная итерация в цикле ValueAnimator'а. Нужно расчитать очередную порцию
     *    исходной битмапы (rectBitmapVisible), которая будет отображена в следующем onDraw.
     */
    override fun onAnimate(fraction: Float) {

        val bitmap = requireNotNull(sourceImageBitmap)
        val params = requireNotNull(preScaleParams)

        val scale = evaluator.evaluate(fraction, scaleFrom, scaleTo)

        val w = (params.startWidth * scale).toInt()
        val h = (params.startHeight * scale).toInt()

        rectTemp.apply {
            left = (params.pivot.x - w * params.pivotRatioX).toInt()
            top = (params.pivot.y - h * params.pivotRatioY).toInt()
            right = left + w
            bottom = top + h
        }

        val offsetX = when {
            rectTemp.left < 0 -> -rectTemp.left
            rectTemp.right > bitmap.width -> bitmap.width - rectTemp.right
            else -> 0
        }

        val offsetY = when {
            rectTemp.top < 0 -> -rectTemp.top
            rectTemp.bottom > bitmap.height -> bitmap.height - rectTemp.bottom
            else -> 0
        }

        rectTemp.offset(offsetX, offsetY)
        rectBitmapVisible.set(rectTemp)
    }

    /**
     * После анимации данная view отвечает за перерасчет scaleMin,scaleMax и за определение
     * значений для animationScaleUpAvailable/animationScaleDownAvailable.
     *
     * Напоминаю что:
     *  ScaleDown - визуально уменьшаем, но при этом УВЕЛИЧИВАЕМ размер rectBitmapVisible
     *  ScaleUp - визуально увеличиваем, но при этом УМЕНЬШАЕМ размер rectBitmapVisible
     *
     * NOTE: scale_min -> на примере Squeeze: после завершения анимации rectBitmapVisible
     * уменьшается, а значит увеличивается процентное соотношение высоты rectBitmapVisibleHeightMin
     * в высоте rectBitmapVisible. Это нужно учесть и пересчитать scaleMin.
     */
    override fun onPostAnimate() {

        val bitmapHeight = sourceImageBitmap?.height!!
        val heightDifference = bitmapHeight - rectBitmapVisible.height()

        scaleController.scaleShrink = if (abs(heightDifference) < measurementError) {
            1f
        } else {
            1f + heightDifference.toFloat() / bitmapHeight
        }

        scaleController.onScaleDownAvailable(scaleController.scaleShrink > 1f)

        val scaleUpAvailable =
            rectBitmapVisible.height() >= (bitmapVisibleHeightMin).toInt()

        scaleController.onScaleUpAvailable(scaleUpAvailable)

        /**
         * При максимальном зуме rectBitmapVisibleHeightMin == rectBitmapVisible.height
         */
        scaleController.scaleSqueeze =
            if (scaleUpAvailable) {
                bitmapVisibleHeightMin / rectBitmapVisible.height()
            } else {
                1f
            }

        scaleController.bitmapScaleCurrent = rectBitmapVisible.height().toFloat() / bitmapHeight
    }

    /**
     * @rectBitmapVisible - задает видимую часть битмапы в координатах битмапы.
     * @rectDest - целевой прямоугольник в координатах канвы.
     * То есть берем некую часть из битмапы и переносим в указанную область канвы.
     */
    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {

            // TODO Вот так делать не надо. Тормозит отрисовка.
//            canvas.drawBitmap(it, rectBitmapVisible, rectDest, paintBackground)

            // TODO Вот так надо.
            rectFrom.set(rectBitmapVisible)
            rectTo.set(rectVisible)
            scaleMatrix.reset()
            scaleMatrix.setRectToRect(rectFrom, rectTo, Matrix.ScaleToFit.FILL)
            canvas.drawBitmap(it, scaleMatrix, paintBackground)
        }
    }
}