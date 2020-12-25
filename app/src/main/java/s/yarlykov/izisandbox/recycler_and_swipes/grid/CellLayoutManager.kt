package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.extensions.min
import s.yarlykov.izisandbox.utils.logIt
import java.lang.IllegalArgumentException
import kotlin.math.ceil
import kotlin.math.min

class CellLayoutManager(val context: Context, val colSpan: Int) : RecyclerView.LayoutManager() {

    init {
        if (colSpan < 0) throw IllegalArgumentException("Invalid colSpan argument")
    }

    /**
     * Размер декоратора между ячейками.
     * Его должен сообщить ItemDecoration. Из первого сообщения выбирается наименьшее
     * значение и применяется ко всем children.
     */
    private var spacer: Int = -1

    /**
     * Ширина отдельного элемента
     */
    private val spanSize: Int by lazy {
        val spacersQty = colSpan + 1
        val spacersSize = spacersQty * spacer
        val remain = width - spacersSize
        ceil(remain.toFloat() / colSpan).toInt()
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * Хотим скролиться по вертикали
     */
    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount <= 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        fillGrid(recycler)
    }

    /**
     * Ну нужно пользоваться методами layoutDecorated/layoutDecoratedWithMargins потому что
     * они используют данные декоратора, хранящиеся в недоступной layoutParams.mDecorInsets.
     * Нужно просто вызывать child.layout(...)
     */
    private fun fillGrid(recycler: RecyclerView.Recycler) {
        var summaryHeight = 0

        run loop@{

            var lastItem = 0

            while (lastItem != itemCount) {
                lastItem = fillRow(lastItem, spacer, recycler)
            }
        }
    }

    /**
     * Сделать layout элементов одной строки
     */
    private fun fillRow(indexFrom: Int, topFrom: Int, recycler: RecyclerView.Recycler): Int {
        val indexTo = min(itemCount, indexFrom + colSpan)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)

        var leftFrom = spacer

        (indexFrom until indexTo).forEach { i ->
            val child = recycler.getViewForPosition(i)

            addView(child)
            measureChildWithoutInsets(child, widthSpec, heightSpec)
            child.layout(leftFrom, topFrom, leftFrom + spanSize, topFrom + spanSize)
            leftFrom += (spanSize + spacer)
        }

        return indexTo
    }

    /**
     * Измерить view с учетом имеющихся insets, а именно:
     * - отступов, которые насчитал декоратор
     * - margins нашей view
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        val decorRect = Rect()

        // Получить декор и установить spacer
        calculateItemDecorationsForChild(child, decorRect)
        if (spacer == -1) {
            spacer = decorRect.min
        }

        resetMargins(child)

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, spanSize)
        child.measure(widthSpecUpdated, heightSpecUpdated)
    }

    /**
     * Обнуляем все маргины. Все отступы будут согласно декору.
     */
    private fun resetMargins(child: View) {
        (child.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = 0
            rightMargin = 0
            bottomMargin = 0
            topMargin = 0
        }
    }

    /**
     * Корректируем отдельную размерность (ширина/высота) view с учетом имеющихся insets.
     */
    private fun updateMeasureSpecs(spec: Int, size: Int): Int {

        val mode = View.MeasureSpec.getMode(spec)

        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(size, mode)
        }
        return spec
    }

    /**
     * Смотрим что насчитал декоратов
     */
    private fun printDecorValues(child: View) {
        /**
         * Одни и теже значения в результатах
         */
        val l = getDecoratedLeft(child)
        val r = getDecoratedRight(child)
        val b = getDecoratedBottom(child)
        val t = getDecoratedTop(child)

        val rect = Rect()
        getDecoratedBoundsWithMargins(child, rect)

        val leftDecW = getLeftDecorationWidth(child)
        val rightDecW = getRightDecorationWidth(child)

        val (w, h) = getDecoratedMeasuredWidth(child) to getDecoratedMeasuredHeight(child)

        logIt(
            "1. decorated: l,t,r,b=$l,$t,$r,$b. bounds=$rect, leftDecW=$leftDecW, rightDecW=$rightDecW, fullW=$w, fullH=$h",
            true,
            "PLPL"
        )
    }

}