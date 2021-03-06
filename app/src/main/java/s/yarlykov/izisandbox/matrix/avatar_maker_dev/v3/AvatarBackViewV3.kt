package s.yarlykov.izisandbox.matrix.avatar_maker_dev.v3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import s.yarlykov.izisandbox.matrix.avatar_maker_dev.BitmapPreScaleParams

/**
 * Класс управляет масштабом и позиционированием фонового рисунка.
 */
class AvatarBackViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground: Paint? = null
    private val scaleMatrix = Matrix()

    private val rectTemp = Rect()
    private val rectFrom = RectF()
    private val rectTo = RectF()

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
     */
    override fun onPreAnimate(factor: Float, pivot: PointF) {
        if (!isScaleUpAvailable) return

        // Нужно сконвертировать pivot из кординат view в координаты битмапы.
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

        scaleFrom = 1f      // от текущего состояния
        scaleTo = factor    // до состояния factor
    }

    /**
     * 2. Отдельная итерация в цикле ValueAnimator'а. Нужно расчитать очередную порцию
     *    исходной битмапы (rectBitmapVisible), которая будет отображена в следующем onDraw.
     */
    override fun onAnimate(fraction: Float) {
        if (!isScaleUpAvailable) return

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

    /**
     * NOTE: Надо дебажить. По моему при увеличении битмапы больше её собственного
     * разрешения начинает лагать отрисовка. До превышения этого значения все ОК, ниже линии.
     *
     * @param pivot - точка в координатах канвы. Её нужно смапить на координаты битмапы.
     */
    private fun scaleAnimated(scaleFactor: Float, pivot: PointF) {

//        logIt("scaleAnimated scaleFactor=$scaleFactor pivot=$pivot")

        sourceImageBitmap?.let { bitmap ->

            // Нужно сконвертировать pivot из кординат view в координаты rectBitmapVisible.
            // Сначала определяем отношение между двумя pivot'ами с точки зрения соотношения
            // сторон прямоугольников. Затем переносим pivot на координаты rectBitmapVisible.
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

//            logIt("view params: rectVisible=${rectVisible}, pivot=$pivot")
//            logIt("bitmap params: rectBitmapVisible=$rectBitmapVisible, pivotBitmap=$pivotBitmap, pivotRatioX=$pivotRatioX, pivotRatioY=$pivotRatioY")

            val startW = rectBitmapVisible.width()
            val startH = rectBitmapVisible.height()

            ValueAnimator.ofFloat(1f, scaleFactor).apply {
                duration = animDuration

                addUpdateListener { animator ->
                    val scale = animator.animatedValue as Float

                    val w = (startW * scale).toInt()
                    val h = (startH * scale).toInt()

                    rectTemp.apply {
                        left = (pivotBitmap.x - w * pivotRatioX).toInt()
                        top = (pivotBitmap.y - h * pivotRatioY).toInt()
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
//                    logIt("scaleAnimated rectBitmapVisible=$rectBitmapVisible")

                    invalidate()
                }
            }.start()
        }
    }

}