package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.minutes
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.ColumnViewController
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_END
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity.Companion.DAY_START
import s.yarlykov.izisandbox.utils.logIt

class DutyTimeDecor(context: Context) : Decorator.ViewHolderDecorator {

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
        strokeWidth = borderStrokeWidth * 5
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
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val touchPoints = FloatArray(4) { 0f }

    private val frameRect = RectF()
    private val borderRect = RectF()

    override fun draw(
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {
        val viewHolder = recyclerView.getChildViewHolder(view)
        if (viewHolder !is ColumnViewController.TicketViewHolder) return

        viewHolder.ticket?.let { ticket ->

            // Поправка на величину верхнего padding'а у RecyclerView
            val baseY = recyclerView.paddingTop

            // ppm - pixels per minute
            val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

            frameRect.set(
                view.left.toFloat(),
                baseY + (ticket.start.minutes - dayRange.first) * ppm,
                view.right.toFloat(),
                baseY + (ticket.end.minutes - dayRange.first) * ppm
            )

            borderRect.set(
                frameRect.left + borderStrokeWidth / 2,
                frameRect.top + borderStrokeWidth / 2,
                frameRect.right - borderStrokeWidth / 2,
                frameRect.bottom - borderStrokeWidth / 2
            )

            drawBlueRect(canvas, frameRect)
            drawBorder(canvas, borderRect)
            drawTouchPoints(canvas, borderRect)

            // Заголовки столбцов
            canvas.drawText(ticket.title, view.left.toFloat() + 20f, view.top - 50f, paintText)
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
}