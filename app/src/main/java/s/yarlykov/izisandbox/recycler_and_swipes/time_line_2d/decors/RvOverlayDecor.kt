package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.ColumnViewController
import kotlin.math.floor

/**
 * Отрисовка
 * - Горизонтальная линия, выделяющая верхнюю панель
 * - Левая боковая вертикальная линия, выделяющая боковую панель
 * - Шкала с часами в левой боковой панели
 */
class RvOverlayDecor(context: Context) : Decorator.RecyclerViewDecorator {

    private val guideLineWidth = context.resources.getDimension(R.dimen.bar_separator_width)
    private val gridStrokeWidth = context.resources.getDimension(R.dimen.grid_stroke_width)
    private val gridStrokeGap = context.resources.getDimension(R.dimen.grid_stroke_gap)
    private val rectLeft = Rect()
    private val rectTextBounds = Rect()

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
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val zoomBitmap =
        ContextCompat.getDrawable(context, R.drawable.ic_zoom)?.run {
            toBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        val dayRange = recyclerView.getChildAt(0)
            ?.let { anchor ->
                (recyclerView.getChildViewHolder(anchor) as? ColumnViewController.TicketViewHolder)?.ticket?.dayRange
            } ?: return

        // Область левой вертикальной панели с часами: 10:00...21:00
        rectLeft.set(
            recyclerView.left,
            recyclerView.top,
            recyclerView.left + recyclerView.paddingStart,
            recyclerView.bottom
        )

        // Закрасить цветом левую панель
//        canvas.drawRect(rectLeft, paintWhiteRect)

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

        // Сетка горизонтальных линий
        drawGrid(canvas, recyclerView, dayRange)

        // Левая вертикальная панель с часами
//        try {
//            canvas.save()
//            canvas.clipRect(
//                recyclerView.left,
//                recyclerView.paddingTop,
//                recyclerView.left + recyclerView.paddingStart,
//                recyclerView.bottom
//            )
//            drawHours(canvas, recyclerView, dayRange)
//        } finally {
//            canvas.restore()
//        }

        // Иконка zoom'а
        drawZoomIcon(canvas, recyclerView)
    }

    /**
     * Линии сетки двигаются вертикально повторяя "вертикальную прокрутку" элементов списка.
     */
    private fun drawGrid(canvas: Canvas, recyclerView: RecyclerView, dayRange: IntRange) {

        canvas.save()
        canvas.clipRect(
            recyclerView.paddingLeft,
            recyclerView.paddingTop,
            recyclerView.right - recyclerView.paddingRight,
            recyclerView.bottom - recyclerView.paddingBottom
        )

        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        val startX = recyclerView.paddingLeft.toFloat()
        val endX = recyclerView.right.toFloat()

        // Опорная view по которой определим высоту элемента списка. Берем первую видимую.
        val anchor = recyclerView.getChildAt(0) ?: recyclerView

        // Количество горизонтальных линий
        val lines = hours - 1
        // Один промежуток (gap) - 1 час.
        // Высота промежутка между целыми часами, например между 10:00 и 11:00
        val gap =
            (anchor.bottom - anchor.top) / hours.toFloat()

        for (i in 1..lines) {
            val y = floor(i * gap) + anchor.top
            canvas.drawLine(startX, y, endX, y, paintGrid)
        }

        canvas.restore()
    }

    /**
     * Рисуем текст в левой панеле.
     *
     * Позиционируем канву для отрисовки текста. Позиция канвы будет определять координаты
     * base line текста, а не left/top прямоугольника области текста. Поэтому для вертикального
     * центрирования текста относительно какой-либо точки нужно учитывать высоту прямоугольника
     * текста.
     */
    private fun drawHours(canvas: Canvas, recyclerView: RecyclerView, dayRange: IntRange) {
        val hours = (dayRange.last - dayRange.first) / 60
        if (hours == 0) return

        // Опорная view по которой определим высоту элемента списка. Берем первую видимую.
        val anchor = recyclerView.getChildAt(0) ?: recyclerView

        // Один промежуток (gap) - 1 час.
        // Высота промежутка в px между целыми часами, например между 10:00 и 11:00
        val gap =
            (anchor.bottom - anchor.top) / hours.toFloat()

        // Измерить прямоугольник текста "00:00"
        val minutes = ":00"
        val pattern = "00$minutes"
        paintText.getTextBounds(pattern, 0, pattern.length, rectTextBounds)

        val startX = (recyclerView.paddingLeft - rectTextBounds.width()) / 2
        val startY = anchor.top + rectTextBounds.height() / 2

        canvas.translate(startX.toFloat(), startY.toFloat())

        for (i in 1 until hours) {
            val hour = (dayRange.first + i * 60) / 60
            val text = hour.toString().padStart(2, '0') + minutes

            canvas.translate(0f, gap)
            canvas.drawText(text, 0f, 0f, paintText)
        }
    }

    /**
     * Иконка zoom'а в левом верхнем углу
     */
    private fun drawZoomIcon(canvas: Canvas, recyclerView: RecyclerView) {

        zoomBitmap?.let { bitmap ->
            val left = (recyclerView.paddingLeft - bitmap.width) / 2f
            val top = (recyclerView.paddingTop - bitmap.height) / 2f
            canvas.drawBitmap(bitmap, left, top, null)
        }
    }
}