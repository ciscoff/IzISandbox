package s.yarlykov.izisandbox.recycler_and_swipes.grid

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.min
import s.yarlykov.izisandbox.utils.logIt
import java.lang.IllegalArgumentException
import kotlin.math.ceil
import kotlin.math.min

/**
 * Данный CellLayoutManager использует свой собственный декоратор CellDecorator
 * для определения отступов.
 */
class CellLayoutManager(
    val context: Context,
    val colSpan: Int,
    @DimenRes divider: Int = R.dimen.cell_spacer
) : RecyclerView.LayoutManager() {

    init {
        if (colSpan < 0) throw IllegalArgumentException("Invalid colSpan argument")
    }

    /**
     * Декоратор
     */
    val decorator: CellDecorator by lazy { CellDecorator(context, divider) }

    /**
     * Размер декоратора между ячейками.
     * Его должен сообщить ItemDecoration. Из первого сообщения выбирается наименьшее
     * значение и применяется ко всем children.
     */
    private var spacer: Int = -1

    /**
     * Переменная для layout'а наборов строк.
     */
    private var summaryHeight = 0

    /**
     * Ширина отдельного элемента
     */
    private val spanSize: Int by lazy {
        val spacersQty = colSpan + 1
        val spacersSize = spacersQty * spacer
        val remain = width - spacersSize
        ceil(remain.toFloat() / colSpan).toInt()
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

        var nextItem = 0
        summaryHeight = spacer

        while (nextItem != itemCount && summaryHeight < height) {
            nextItem = fillRow(nextItem, summaryHeight, recycler)
        }
    }

    /**
     * Сделать layout элементов одной строки
     */
    private fun fillRow(positionFrom: Int, topFrom: Int, recycler: RecyclerView.Recycler): Int {
        val positionTo = min(itemCount, positionFrom + colSpan)

        val widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY)

        var leftFrom = spacer

        (positionFrom until positionTo).forEach { i ->
            val child = recycler.getViewForPosition(i)

            addView(child)
            measureChildWithoutInsets(child, widthSpec, heightSpec)
            child.layout(leftFrom, topFrom, leftFrom + spanSize, topFrom + spanSize)
            leftFrom += (spanSize + spacer)
        }

        summaryHeight += (spanSize + spacer)

        return positionTo
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
     * Установить размер в MeasureSpec
     */
    private fun updateMeasureSpecs(spec: Int, size: Int): Int {

        val mode = View.MeasureSpec.getMode(spec)

        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(size, mode)
        }
        return spec
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
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

    /**
     * Класс декоратора для работы с данным LayoutManager'ом.
     *
     * У базового декоратора 3 метода: один (getItemOffsets) нужен чтобы добавить
     * отступы. Два других для рисования на канве RecyclerView. Отступы учитываются при
     * layout'е. Все эти методы между собой НЕ СВЯЗАНЫ. Рисовать можно что угодно и где угодно
     * независимо от того добавлялись ли отступы для этого или нет.
     *
     */
    inner class CellDecorator(
        val context: Context,
        @DimenRes divider: Int
    ) : RecyclerView.ItemDecoration() {

        private var spacer = context.resources.getDimension(divider).toInt()

        override fun getItemOffsets(
            rect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view).let {
                if (it == RecyclerView.NO_POSITION) return else it
            }

            rect.set(spacer, spacer, spacer, spacer)
        }

        /**
         * Установить разные отступы в зависимости от номера столбца.
         */
        private fun setByColumn(rect: Rect, position: Int) {

            val halfSpacer = spacer / 2

            when {
                // Первый столбец
                (position % colSpan == 0) -> {
                    rect.left = spacer
                    rect.right = halfSpacer
                }
                // Последний столбец
                ((position + 1 % colSpan) == 0) -> {
                    rect.left = halfSpacer
                    rect.right = spacer

                }
                // Серединка
                else -> {
                    rect.left = halfSpacer
                    rect.right = halfSpacer
                }
            }
            rect.top = spacer
            rect.bottom = 0
        }
    }
}