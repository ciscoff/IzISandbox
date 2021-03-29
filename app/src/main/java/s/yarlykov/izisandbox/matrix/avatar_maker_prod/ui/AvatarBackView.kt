package s.yarlykov.izisandbox.matrix.avatar_maker_prod.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.core.view.drawToBitmap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.MediaData
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale.BitmapPreScaleParams
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Класс управляет масштабом и позиционированием фонового рисунка.
 */
class AvatarBackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseView(context, attrs, defStyleAttr) {

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

    private fun Bitmap.cropTo(crop: RectF): Bitmap {

        val rect = Rect()
        crop.round(rect)

        return Bitmap.createBitmap(
            this,
            rect.left,
            rect.top,
            rect.width(),
            rect.height()
        )
    }

    private fun extractBitmap(clip: RectF): Bitmap? {
        return try {
            drawToBitmap().cropTo(clip)
        } catch (e: Exception) {
            logIt("${this::class.simpleName}::extractBitmap null")
            null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        scaleController.onBackSizeChanged(w to h)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        logIt("AvatarBackView viewModel=$viewModel")

        viewModel.viewModelScope.launch {

            viewModel.avatarClipFlow.collect { clip ->
                logIt("AvatarBackView collect clip from avatarClipFlow: $clip")
                extractBitmap(clip)?.let {
                    logIt("AvatarBackView got bitmap from extractBitmap: w/h ${it.width}/${it.height} and push it into bitmapFlow")
                    viewModel.bitmapFlow.emit(it)
                }
            }
        }
    }

    override fun onBitmapReady(mediaData: MediaData) {
        super.onBitmapReady(mediaData)
        invalidate()
    }

    /**
     * 1. Первый этап в цикле анимации скалирования: подготовить данные.
     *
     * @param scaleFactor:
     * - Показывает реальный scale для видимой части битмапы, то есть после анимации
     * отношение rectBitmapVisible.height() /  sourceImageBitmap.height будет равно scaleFactor.
     */
    override fun onPreAnimate(scaleFactor: Float, pivot: PointF) {
        val bitmap = requireNotNull(sourceImageBitmap)

        // Нужно сконвертировать pivot из координат view в координаты битмапы.
        // Сначала определяем отношение между двумя pivot'ами с точки зрения соотношения
        // сторон прямоугольников. Затем переносим pivot на координаты битмапы.
        val pivotBitmap = PointF(0f, 0f).apply {

            val ratioX = rectBitmapVisible.width().toFloat() / rectViewPort.width()
            val ratioY = rectBitmapVisible.height().toFloat() / rectViewPort.height()

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
         * Теперь нужно scaleFactor (который показывает абсолютное значение scale) сконвертировать
         * в относительное значение scale. Под относительным отношением scale понимается
         * следующее: как нужно отскалировать rectBitmapVisible.height от её текущего значения,
         * до её нового значения. Текущее значение rectBitmapVisible.height принимается за 1.
         */
        val postScaleVisibleHeight = bitmap.height * scaleFactor

        // от текущего состояния rectBitmapVisible
        scaleFrom = 1f
        // до нового состояния
        scaleTo = postScaleVisibleHeight / rectBitmapVisible.height()
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
     * После анимации данная view отвечает за определение состояний для animationScaleUpAvailable и
     * animationScaleDownAvailable. Однако AvatarFrontView в событии ACTION_UP делает свою проверку
     * и запрещает анимацию ScaleUp, если в режиме squeeze размер рамки больше указанного порога.
     *
     * Напоминаю что:
     *  ScaleDown - визуально уменьшаем, но при этом УВЕЛИЧИВАЕМ размер rectBitmapVisible
     *  ScaleUp - визуально увеличиваем, но при этом УМЕНЬШАЕМ размер rectBitmapVisible
     *
     * NOTE: scale_min -> на примере Squeeze: после завершения анимации rectBitmapVisible
     * уменьшается, а значит увеличивается процентное соотношение высоты bitmapVisibleHeightMin
     * в высоте rectBitmapVisible. Это нужно учесть и пересчитать scaleMin.
     */
    override fun onPostAnimate() {
        val bitmap = requireNotNull(sourceImageBitmap)

        val bitmapHeight = bitmap.height
        val heightDifference = bitmapHeight - rectBitmapVisible.height()

        scaleController.apply {
            onScaleDownAvailable(abs(heightDifference) > measurementError)
            onScaleUpAvailable(rectBitmapVisible.height() > ceil(bitmapVisibleHeightMin))
            bitmapScaleCurrent = rectBitmapVisible.height().toFloat() / bitmapHeight
        }
    }

    /**
     * Берем некую часть из битмапы (rectBitmapVisible) и переносим в указанную
     * область канвы rectVisible.
     *
     * NOTE: Нужно ограничивать область рисования (clip), чтобы в горизонтальной ориентации
     * и на планшете битмапа не вылезала по бокам.
     */
    override fun onDraw(canvas: Canvas) {

        sourceImageBitmap?.let {

            canvas.save()
            canvas.clipRect(rectViewPort)

            // TODO Вот так делать не надо. Тормозит отрисовка.
//            canvas.drawBitmap(it, rectBitmapVisible, rectDest, paintBackground)

            // TODO Вот так надо.
            rectFrom.set(rectBitmapVisible)
            rectTo.set(rectViewPort)
            scaleMatrix.reset()
            scaleMatrix.setRectToRect(rectFrom, rectTo, Matrix.ScaleToFit.FILL)
            canvas.drawBitmap(it, scaleMatrix, paintBackground)
            canvas.restore()
        }
    }
}