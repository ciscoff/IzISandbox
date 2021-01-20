package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.ColumnViewController
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_END
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_START

/**
 * Отрисовка:
 * - Голубые прямоугольники выбранного времени
 * - Рамки вокруг голубых прямоугольников
 * - Точки тача на рамках
 */

class HolderOverlayDecor(context: Context) : Decorator.ViewHolderDecorator {

    private val dayRange = (DAY_START.minutes..DAY_END.minutes)

    private val borderStrokeWidth =
        context.resources.getDimension(R.dimen.blue_rect_border_stroke_width)

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

            // ppm - pixels per minute
            val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

            blueRect.set(
                view.left.toFloat(),
                view.top + (ticket.start.minutes - dayRange.first) * ppm,
                view.right.toFloat(),
                view.top + (ticket.end.minutes - dayRange.first) * ppm
            )

            borderRect.set(
                blueRect.left + borderStrokeWidth / 2,
                blueRect.top + borderStrokeWidth / 2,
                blueRect.right - borderStrokeWidth / 2,
                blueRect.bottom - borderStrokeWidth / 2
            )

            canvas.save()

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

            canvas.restore()

            // Заголовки столбцов
            drawTitle(ticket.title, canvas, view, recyclerView)
        }
    }

    /**
     * Отрисовка голубого прямоугольника
     */
    private fun drawBlueRect(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRect(rect, paintBlueRect)
    }

    /**
     * Отрисовка рамки вокруг голубого прямоугольника
     */
    private fun drawBorder(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRect(rect, paintBorder)
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
            val startY = (recyclerView.paddingTop - rectTextBounds.height()) / 2 + rectTextBounds.height()

            canvas.translate(startX.toFloat(), startY.toFloat())
            canvas.drawText(title, 0f, 0f, paintText)

        } finally {
            canvas.restore()
        }
    }
}