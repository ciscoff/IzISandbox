package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.hhMm
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.ColumnViewController

/**
 * Отрисовка:
 * + Голубые прямоугольники выбранного времени
 * + Рамки вокруг голубых прямоугольников
 * + Точки тача на рамках
 * - Боковые подсказки о диапазоне выбранного времени
 */
class HolderOverlayDecor(context: Context) : Decorator.ViewHolderDecorator {

    private val borderStrokeWidth =
        context.resources.getDimension(R.dimen.blue_rect_border_stroke_width)

    private val cornerRadius = context.resources.getDimension(R.dimen.blue_rect_corner_radius)

    private val hintMargin = context.resources.getDimensionPixelOffset(R.dimen.hint_margin)

    private val paintBlueRect = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorDecor16)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintTouchPoint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorDecor4)
        strokeWidth = borderStrokeWidth * 7
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val paintBorder = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorDecor4)
        strokeWidth = borderStrokeWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val paintText = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = context.resources.getDimension(R.dimen.title_stroke_width)
        textSize = context.resources.getDimension(R.dimen.title_text_size)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintHintText = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
        strokeWidth = context.resources.getDimension(R.dimen.hint_stroke_width)
        textSize = context.resources.getDimension(R.dimen.hint_text_size)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        style = Paint.Style.FILL
        isAntiAlias = true
    }



    private val touchPoints = FloatArray(4) { 0f }

    private val blueRect = RectF()
    private val borderRect = RectF()
    private val rectTextBounds = Rect()

    override fun draw(
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val viewHolder = recyclerView.getChildViewHolder(view)
        if (viewHolder !is ColumnViewController.TicketViewHolder) return

        viewHolder.ticket?.let { ticket ->

            val dayRange = ticket.dayRange

            // ppm - pixels per minute
            val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

            blueRect.set(
                view.left.toFloat(),
                view.top + (ticket.start - dayRange.first) * ppm,
                view.right.toFloat(),
                view.top + (ticket.end - dayRange.first) * ppm
            )

            borderRect.set(
                blueRect.left + borderStrokeWidth / 2,
                blueRect.top + borderStrokeWidth / 2,
                blueRect.right - borderStrokeWidth / 2,
                blueRect.bottom - borderStrokeWidth / 2
            )

            canvas.save()

            // Заголовки столбцов
            drawTitle(ticket.title, canvas, view, recyclerView)


            // Для того, чтобы не залезать в верхнее padding-поле с заголовками
            // обрезаем область рисования.
            canvas.clipRect(
                recyclerView.left,
                recyclerView.paddingTop,
                recyclerView.right,
                recyclerView.bottom
            )

            drawBlueRect(canvas, blueRect)
            drawBorder(canvas, borderRect)
            drawTouchPoints(canvas, borderRect)

            // Левая вертикальная панель с часами
            if(recyclerView.getChildAt(0)?.id == view.id) {

//                try {
//                    canvas.save()
//                    canvas.clipRect(
//                        recyclerView.left,
//                        recyclerView.paddingTop,
//                        recyclerView.left + recyclerView.paddingStart,
//                        recyclerView.bottom
//                    )
//                    drawHours(canvas, recyclerView, dayRange)
//                } finally {
//                    canvas.restore()
//                }
            }

            // Боковые подсказки
            drawBubbleHints(ticket.start, ticket.end, canvas, view)

            canvas.restore()
        }
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
     * Отрисовка голубого прямоугольника
     */
    private fun drawBlueRect(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paintBlueRect)
    }

    /**
     * Отрисовка рамки вокруг голубого прямоугольника
     */
    private fun drawBorder(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paintBorder)
    }

    /**
     * Отрисовка точек тача на рамке вокруг голубого прямоугольника
     */
    private fun drawTouchPoints(
        canvas: Canvas,
        rect: RectF
    ) {

        val offset = rect.width() / 5

        touchPoints[0] = rect.left + offset
        touchPoints[1] = rect.top
        touchPoints[2] = rect.right - offset
        touchPoints[3] = rect.bottom

        canvas.drawPoints(touchPoints, paintTouchPoint)
    }

    private fun drawTitle(
        title: String,
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView
    ) {
        try {
            canvas.save()
            paintText.getTextBounds(title, 0, title.length, rectTextBounds)

            val startX = view.left + (view.width - rectTextBounds.width()) / 2
            val startY =
                (recyclerView.paddingTop - rectTextBounds.height()) / 2 + rectTextBounds.height()

            canvas.translate(startX.toFloat(), startY.toFloat())
            canvas.drawText(title, 0f, 0f, paintText)

        } finally {
            canvas.restore()
        }
    }


    private fun drawBubbleHints(start: Int, end: Int, canvas: Canvas, view: View) {

        val startSz = start.hhMm()
        val endSz = end.hhMm()

        try {
            canvas.save()

            paintHintText.getTextBounds(startSz, 0, startSz.length, rectTextBounds)

            val startX = blueRect.left - rectTextBounds.width() - hintMargin
            val startY = blueRect.top + rectTextBounds.height() + hintMargin

            canvas.translate(startX, startY)
            canvas.drawText(startSz, 0f, 0f, paintHintText)

            paintHintText.getTextBounds(endSz, 0, endSz.length, rectTextBounds)

            canvas.translate(0f, blueRect.bottom - hintMargin - startY)
            canvas.drawText(endSz, 0f, 0f, paintHintText)

        } finally {
            canvas.restore()
        }
    }
}