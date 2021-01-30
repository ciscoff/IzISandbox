package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import s.yarlykov.izisandbox.utils.logIt

class AvatarBackViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground: Paint? = null
    private val rectTemp = Rect()

    /**
     * Данные, которые нужно собрать перед началом анимации.
     */
    private var preScaleParams: BitmapPreScaleParams? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    /**
     * @rectBitmapVisible - задает видимую часть битмапы в координатах битмапы.
     * @rectDest - целевой прямоугольник в координатах канвы.
     * То есть берем некую часть из битмапы и переносим в указанную область канвы.
     */
    val m = Matrix()

    val rectFrom = RectF()
    val rectTo = RectF()

    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {
//            canvas.drawBitmap(it, rectBitmapVisible, rectDest, paintBackground)

            // надо так попробовать
            m.reset()
            rectFrom.set(rectBitmapVisible)
            rectTo.set(rectVisible)
            m.setRectToRect(rectFrom, rectTo, Matrix.ScaleToFit.FILL)
            canvas.drawBitmap(it, m, paintBackground)

        }
    }

    override fun onPreScale(factor: Float, pivot: PointF) {
        if(!isScaleDownAvailable) return



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

        val startW = rectBitmapVisible.width()
        val startH = rectBitmapVisible.height()

        preScaleParams = BitmapPreScaleParams(pivotBitmap, pivotRatioX, pivotRatioY, startW, startH)

        scaleFrom = 1f
        scaleTo = factor
    }

    /**
     * Отдельная итерация в цикле ValueAnimator'а
     */
    override fun onScale(fraction: Float) {
        if(!isScaleDownAvailable) return

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
     * NOTE: Надо дебажить. По моему при увеличении битмапы больше её собственного
     * разрешения начинает лагать отрисовка. До превышения этого значения все ОК, ниже линии.
     *
     * @param pivot - точка в координатах канвы. Её нужно смапить на координаты битмапы.
     */
    private fun scaleAnimated(scaleFactor: Float, pivot: PointF) {

        logIt("scaleAnimated scaleFactor=$scaleFactor pivot=$pivot")

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

            logIt("view params: rectVisible=${rectVisible}, pivot=$pivot")
            logIt("bitmap params: rectBitmapVisible=$rectBitmapVisible, pivotBitmap=$pivotBitmap, pivotRatioX=$pivotRatioX, pivotRatioY=$pivotRatioY")

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
                    logIt("scaleAnimated rectBitmapVisible=$rectBitmapVisible")

                    invalidate()
                }
            }.start()
        }
    }

}