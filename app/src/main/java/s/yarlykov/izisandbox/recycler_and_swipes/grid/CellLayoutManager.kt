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

        fillDown(recycler)
    }

    /**
     * Ну нужно пользоваться методами layoutDecorated/layoutDecoratedWithMargins потому что
     * они используют данные декоратора, хранящиеся в недоступной layoutParams.mDecorInsets.
     * Нужно просто вызывать child.layout(...)
     */
    private fun fillDown(recycler: RecyclerView.Recycler) {

        var summaryHeight = 0

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)

        run loop@{
            (0 until itemCount).forEach { i ->
                val child = recycler.getViewForPosition(i)

                addView(child)
                measureChildWithoutInsets(child, widthSpec, heightSpec)
//                measureChildWithMargins(child, 0, 0)

                printDecorValues(child)
//                val (w, h) = getDecoratedMeasuredWidth(child) to getDecoratedMeasuredHeight(child)

                summaryHeight += spacer
                child.layout(spacer, summaryHeight, spacer + spanSize, summaryHeight + spanSize)
//                layoutDecoratedWithMargins(child, spacer, summaryHeight, spanSize, h + summaryHeight)
                summaryHeight += spanSize

                if (summaryHeight > height) return@loop
            }
        }
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

        // Обнуляем все маргины. Все отступы будут согласно декору.
        (child.layoutParams as RecyclerView.LayoutParams).apply {
            leftMargin = 0
            rightMargin = 0
            bottomMargin = 0
            topMargin = 0
        }

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, spanSize)
        child.measure(widthSpecUpdated, heightSpecUpdated)
    }

    private fun resetDecor(child: View) {
        (child.layoutParams as ViewGroup.MarginLayoutParams).apply {
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

        logIt(
            "1. decorated: l,t,r,b=$l,$t,$r,$b. bounds=$rect, leftDecW=$leftDecW, rightDecW=$rightDecW",
            true,
            "PLPL"
        )
    }

}