package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.decors

import android.content.Context
import android.graphics.*
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.hhMm
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.ColumnViewController
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.model.Ticket
import kotlin.math.min

/**
 * Рисуем все что касается декора самих элементов списка:
 * + Голубые прямоугольники выбранного времени.
 * + Рамки вокруг голубых прямоугольников.
 * + Точки тача на рамках.
 * + Затененные прямоугольники с недоступными интервалами.
 */
class HolderViewTypeOverlayDecor(context: Context) : Decorator.ViewHolderDecorator {

    companion object {
        const val POOL_SIZE = 20
    }

    private val borderStrokeWidth =
        context.resources.getDimension(R.dimen.blue_rect_border_stroke_width)

    private val radBlueRect = context.resources.getDimension(R.dimen.blue_rect_corner_radius)
    private val radHintRect = context.resources.getDimension(R.dimen.hint_rect_corner_radius)

    private val hintMargin = context.resources.getDimensionPixelOffset(R.dimen.hint_margin)

    private val hintPaddingHor = context.resources.getDimensionPixelOffset(R.dimen.hint_padding_h)
    private val hintPaddingVer = context.resources.getDimensionPixelOffset(R.dimen.hint_padding_v)

    /**
     * Пул прямоугольников для отрисовки недоступных для выбора областей
     */
    private val rectPool = Array(POOL_SIZE) { RectF() }

    /**
     * Фон для заливки регионов недоступного времени
     */
    private val dottedBackground =
        ContextCompat.getDrawable(context, R.drawable.ic_dots_sparse)?.run {
            toBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        }

    private val bitmapShader =
        BitmapShader(dottedBackground!!, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)

    private val paintShadedRect = Paint().apply {
        shader = bitmapShader
        isAntiAlias = true
    }

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

    private val paintBlueBorder = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorDecor4)
        strokeWidth = borderStrokeWidth
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

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

    private val touchPoints = FloatArray(4) { 0f }

    private val rectHint = RectF()
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
        (viewHolder as? ColumnViewController.TicketViewHolder)?.ticket?.let { ticket ->

            // Заголовок столбца
            drawTitle(ticket.title, canvas, view, recyclerView)

            // Выбранное время в виде голубого прямоугольника
            drawBlueRegion(ticket, canvas, view, recyclerView)

            // TODO Переехало в HolderAnyOverlayDecor
            // Левая вертикальная панель с часами (отрисовку делает первый видимый ViewHolder)
//            if (recyclerView.getChildAt(0)?.id == view.id) {
//                drawHours(canvas, recyclerView, dayRange)
//            }

            // TODO Переехало в HolderAnyOverlayDecor
            // Боковые подсказки (у элемента списка, который выбран)
//            if (view.isSelected) {
//                drawBubbleHints(ticket.start, ticket.end, canvas, view, recyclerView)
//            }

            // Прямоугольники с недоступными интервалами времени
            drawShadedRegion(ticket, canvas, view)
        }
    }

    /**
     * Отрисовать выбранное время
     */
    private fun drawBlueRegion(
        ticket: Ticket,
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView
    ) {
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

        try {
            canvas.save()
            canvas.clipRect(
                recyclerView.paddingLeft,
                recyclerView.paddingTop,
                recyclerView.width,
                recyclerView.height
            )

            drawBlueRect(canvas, blueRect)
            drawBorder(canvas, borderRect)
            drawTouchPoints(canvas, borderRect)
        } finally {
            canvas.restore()
        }
    }

    /**
     * Отрисовка голубого прямоугольника
     */
    private fun drawBlueRect(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRoundRect(rect, radBlueRect, radBlueRect, paintBlueRect)
    }

    /**
     * Отрисовка рамки вокруг голубого прямоугольника
     */
    private fun drawBorder(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRoundRect(rect, radBlueRect, radBlueRect, paintBlueBorder)
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

    /**
     * Заголовок элемента списка
     */
    private fun drawTitle(
        title: String,
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView
    ) {

        try {
            canvas.save()
            canvas.clipRect(
                recyclerView.paddingLeft,
                0,
                recyclerView.width,
                recyclerView.paddingTop
            )

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

    /**
     * Декорируем области с недоступными интервалами времени
     */
    private fun drawShadedRegion(
        ticket: Ticket,
        canvas: Canvas,
        view: View
    ) {
        val dayRange = ticket.dayRange

        // ppm - pixels per minute
        val ppm = (view.height).toFloat() / (dayRange.last - dayRange.first)

        // Чтобы не выйти за пределы массивов выбираем кол-во проходов по минимальному.
        repeat(min(ticket.busySlots.size, rectPool.size)) { i ->
            slotToRect(ticket.busySlots[i], ppm, dayRange, view, rectPool[i])
            drawShadedRect(canvas, rectPool[i])
        }
    }

    /**
     * Отдельный прямоугольник с недоступным интервалам времени
     */
    private fun drawShadedRect(
        canvas: Canvas,
        rect: RectF
    ) {
        canvas.drawRect(rect, paintShadedRect)
    }

    /**
     * Транслировать интервал в RectF
     */
    private fun slotToRect(
        slot: Pair<Int, Int>,
        ppm: Float,
        dayRange: IntRange,
        view: View,
        outRect: RectF
    ) {
        val (start, end) = slot

        outRect.set(
            view.left.toFloat(),
            view.top + (start - dayRange.first) * ppm,
            view.right.toFloat(),
            view.top + (end - dayRange.first) * ppm
        )
    }

    /**
     * TODO Переехало в HolderAnyOverlayDecor
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

    /**
     * TODO Переехало в HolderAnyOverlayDecor
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
}