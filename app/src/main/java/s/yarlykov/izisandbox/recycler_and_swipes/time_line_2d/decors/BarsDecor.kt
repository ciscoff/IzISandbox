package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_END
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_START
import kotlin.math.floor

class BarsDecor(context: Context) : Decorator.RecyclerViewDecorator {

    private val guideLineWidth = context.resources.getDimension(R.dimen.bar_separator_width)
    private val gridStrokeWidth = context.resources.getDimension(R.dimen.grid_stroke_width)
    private val gridStrokeGap = context.resources.getDimension(R.dimen.grid_stroke_gap)
    private val rectLeft = Rect()

    private val dayRange = (DAY_START.minutes..DAY_END.minutes)

    private val paintWhiteRect = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintGuideLine = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = guideLineWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintGrid = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = gridStrokeWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
        pathEffect = DashPathEffect(floatArrayOf(gridStrokeGap, gridStrokeGap), 0f)
    }

    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = context.resources.getDimension(R.dimen.title_stroke_width)
        textSize = context.resources.getDimension(R.dimen.title_text_size)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        // Левая панель с часами: 10:00...21:00
        rectLeft.set(
            recyclerView.left,
            recyclerView.top,
            recyclerView.left + recyclerView.paddingStart,
            recyclerView.bottom
        )

        // Закрасить цветом левую панель
        canvas.drawRect(rectLeft, paintWhiteRect)

        // Верхняя горизонтальная линия
        canvas.drawLine(
            recyclerView.left.toFloat(),
            recyclerView.top + recyclerView.paddingTop - guideLineWidth / 2,
            recyclerView.right.toFloat(),
            recyclerView.top + recyclerView.paddingTop - guideLineWidth / 2,
            paintGuideLine
        )

        // Левая боковая вертикальная линия
        canvas.drawLine(
            recyclerView.left + recyclerView.paddingStart.toFloat(),
            recyclerView.top.toFloat(),
            recyclerView.left + recyclerView.paddingStart.toFloat(),
            recyclerView.bottom.toFloat(),
            paintGuideLine
        )

        // Линейка
        drawGrid(canvas, recyclerView)

    }

    private fun drawGrid(canvas: Canvas, recyclerView: RecyclerView) {
        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        val startX = recyclerView.paddingLeft.toFloat()
        val endX = recyclerView.right.toFloat()

        // Количество горизонтальных линий
        val lines = hours - 2
        // Один промежуток (gap) - 1 час.
        // Высота промежутка между целыми часами, например между 10:00 и 11:00
        val gap =
            (recyclerView.bottom - recyclerView.top - recyclerView.paddingTop) / hours.toFloat()

        for (i in 1..lines) {
            val y = floor(i * gap) + recyclerView.paddingTop
            canvas.drawLine(startX, y, endX, y, paintGrid)
        }
    }

    /**
     * Рисуем числа. Они центрируются относительно отсечек благодаря настройке текстовой Paint.
     */
//    private fun drawHours() {
//        val hours = (dayRange.last - dayRange.first) / 60
//        if (hours == 0) return
//
//        val steps = hours - 1
//        val gap = width.toFloat() / hours
//
//        /**
//         * Позиционируем канву для отрисовки текста. Позиция канвы станет левым-НИЖНИМ углом
//         * области текста. Не левым-ВЕРХНИМ, а левым-НИЖНИМ, то есть все через жопу. Поэтому
//         * ставим канву на нижнюю границу View, а текст отрусуется поверх этой границы.
//         *
//         * Полезная заметка тут:
//         * https://stackoverflow.com/questions/3654321/measuring-text-height-to-be-drawn-on-canvas-android
//         */
//        cacheCanvas.save()
//        cacheCanvas.translate(0f, height.toFloat())
//
//        for (i in 1..steps) {
//            cacheCanvas.translate(gap, 0f)
//            cacheCanvas.drawText("${(dayRange.first + i * 60) / 60}", 0f, 0f, paintText)
//        }
//
//        cacheCanvas.restore()
//    }

    /**
     * Функция выполняет расчет высоты для основных компонентов.
     * - высота текста определяется через FontMetrics
     * - высота цветового фона с помощью ratio
     * - оставшееся вертикальное пространство остается для высоты шкаликов метрической линейки
     */

    /**
     * Величины высот основных компонентов
     */
    private var backgroundHeight = 0f
    private var metricsLineHeight = 0f
    private var metricsTextHeight = 0f

//    private fun calculateDimensions() {
//        metricsTextHeight = paintText.fontMetrics.let { it.descent - it.ascent }
//        backgroundHeight = height * TimeSurfaceV4.BACKGROUND_HEIGHT_RATIO
//        metricsLineHeight = height - backgroundHeight - metricsTextHeight
//    }
}