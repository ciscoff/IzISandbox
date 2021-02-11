package s.yarlykov.izisandbox.matrix.avatar_maker.v4

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import s.yarlykov.izisandbox.matrix.avatar_maker.BitmapPreScaleParams
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.ceil

/**
 * Класс управляет масштабом и позиционированием фонового рисунка.
 */
class AvatarBackViewV4 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV4(context, attrs, defStyleAttr) {

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
     *
     * @param factor - показывает сколько ОСТАНЕТСЯ пройти от положения после анимации
     * до наименьшего положения. Допустим что это первый зум: scaleMax = 1, scaleMin = 0.25
     * и factor = 3/4. Это значит, что после выполнения анимации останется
     * пройти 3/4 "дистанции" между scaleMax и scaleMin.
     */
    override fun onPreScale(factor: Float, pivot: PointF) {
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

        lastFactor = factor
    }

    /**
     * 2. Отдельная итерация в цикле ValueAnimator'а. Нужно расчитать очередную порцию
     *    исходной битмапы (rectBitmapVisible), которая будет отображена в следующем onDraw.
     */
    override fun onScale(fraction: Float) {
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
     * Опять на примере Squeeze: после завершения анимации rectBitmapVisible уменьшается, а значит
     * увеличивается процентное соотношение высоты rectBitmapVisibleHeightMin в высоте
     * rectBitmapVisible. Это нужно учесть и пересчитать scaleMin.
     */
    override fun onPostScale() {
        if (!isScaleUpAvailable) return

        scaleController.scaleMin = if (rectBitmapVisible.height() <= ceil(rectBitmapVisibleHeightMin).toInt()) {
            scaleController.onScaleUpAvailable(false)
            0f
        } else {
            scaleController.onScaleUpAvailable(true)
            rectBitmapVisibleHeightMin / rectBitmapVisible.height().toFloat()
        }
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