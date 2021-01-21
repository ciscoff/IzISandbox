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
 * + Часовая шкала в левой панели
 * + Боковые подсказки о диапазоне выбранного времени
 */
class HolderAnyOverlayDecor(context: Context) : Decorator.ViewHolderDecorator {

    private val radHintRect = context.resources.getDimension(R.dimen.hint_rect_corner_radius)
    private val hintMargin = context.resources.getDimensionPixelOffset(R.dimen.hint_margin)
    private val hintPaddingHor = context.resources.getDimensionPixelOffset(R.dimen.hint_padding_h)
    private val hintPaddingVer = context.resources.getDimensionPixelOffset(R.dimen.hint_padding_v)
    private val borderStrokeWidth =
        context.resources.getDimension(R.dimen.blue_rect_border_stroke_width)

    private val paintWhiteRect = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val paintGrayBorder = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
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

    private val rectHint = RectF()
    private val blueRect = RectF()
    private val rectTextBounds = Rect()

    override fun draw(
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val viewHolder = recyclerView.getChildViewHolder(view)
        (viewHolder as? ColumnViewController.TicketViewHolder)?.ticket?.let { ticket ->
            val dayRange = ticket.dayRange

            // ppm - pixels per minute
            val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

            blueRect.set(
                view.left.toFloat(),
                view.top + (ticket.start - dayRange.first) * ppm,
                view.right.toFloat(),
                view.top + (ticket.end - dayRange.first) * ppm
            )

            // Левая вертикальная панель с часами (отрисовку делает первый видимый ViewHolder)
            if (recyclerView.getChildAt(0)?.id == view.id) {
                drawHours(canvas, recyclerView, dayRange)
            }

            // Боковые подсказки (у элемента списка, который выбран кликом)
            if (view.isSelected) {
                drawBubbleHints(ticket.start, ticket.end, canvas, view, recyclerView)
            }
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

        try {
            canvas.save()
            canvas.clipRect(
                0,
                recyclerView.paddingTop,
                recyclerView.paddingStart,
                recyclerView.height
            )

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

        } finally {
            canvas.restore()
        }
    }

    /**
     * Рисование "всплывающих" подсказок границ выделенного региона
     */
    private fun drawBubbleHints(
        start: Int,
        end: Int,
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView
    ) {
        val startSz = start.hhMm()
        val endSz = end.hhMm()

        try {
            canvas.save()

            canvas.clipRect(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.width,
                recyclerView.height
            )

            paintHintText.getTextBounds(startSz, 0, startSz.length, rectTextBounds)

            // У первого элемента списка подсказки справа, у остальных слева от blueRect
            val startX = if (recyclerView.getChildAt(0)?.id == view.id) {
                blueRect.right + hintMargin + hintPaddingHor
            } else {
                blueRect.left - hintMargin - hintPaddingHor - rectTextBounds.width()
            }
            val startY = blueRect.top + rectTextBounds.height() + hintPaddingVer

            rectHint.set(
                startX - hintPaddingHor,
                blueRect.top,

                startX + rectTextBounds.width() + hintPaddingHor,
                startY + hintPaddingVer
            )
            canvas.drawRoundRect(rectHint, radHintRect, radHintRect, paintWhiteRect)
            canvas.drawRoundRect(rectHint, radHintRect, radHintRect, paintGrayBorder)

            rectHint.offset(0f, blueRect.bottom - hintMargin - startY)
            canvas.drawRoundRect(rectHint, radHintRect, radHintRect, paintWhiteRect)
            canvas.drawRoundRect(rectHint, radHintRect, radHintRect, paintGrayBorder)

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