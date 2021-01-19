package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import kotlin.math.max

class TimeLineLayoutManager(val context: Context) : RecyclerView.LayoutManager() {

    /**
     * Переменная для layout'а наборов строк.
     */
    private var summaryWidth = 0

    /**
     * Ширина отдельного элемента
     */
    private val spanSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.column_width)
    }

    private var scaleHeight = 1

    private val viewCache = SparseArray<View>()

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        fill(recycler)
    }

    private fun fill(recycler: RecyclerView.Recycler) {

        val anchorView = getAnchorView()
        viewCache.clear()

        //Помещаем вьюшки в кэш и...
        for (i in 0 until childCount) {
            getChildAt(i)?.let { view ->
                viewCache.put(getPosition(view), view)
            }
        }

        //... и удалям из лэйаута
        for (i in 0 until viewCache.size()) {
            detachView(viewCache.valueAt(i))
        }

        fillLeft(anchorView, recycler)
        fillRight(anchorView, recycler)

        for (i in 0 until viewCache.size()) {
            recycler.recycleView(viewCache.valueAt(i))
        }

//        fillRow(recycler)
    }

    /**
     * Сделать layout элементов одной строки
     */
    private fun fillRow(recycler: RecyclerView.Recycler) {

        var nextItem = 0
        summaryWidth = 0

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(height - paddingTop, View.MeasureSpec.EXACTLY)

        var viewLeft = paddingLeft
        val viewTop = paddingTop

        while (nextItem != itemCount && viewLeft < width) {

            val child = recycler.getViewForPosition(nextItem)
            addView(child)

            measureChildWithoutInsets(child, widthSpec, heightSpec)

            val decoratedMeasuredWidth = getDecoratedMeasuredWidth(child)
            val decoratedMeasuredHeight = getDecoratedMeasuredHeight(child)

            layoutDecorated(
                child,
                viewLeft,
                viewTop,
                viewLeft + decoratedMeasuredWidth,
                viewTop + decoratedMeasuredHeight
            )
            viewLeft = getDecoratedRight(child)
            nextItem++
        }
    }


    /**
     * Установить "чистый" размер view - без inset'ов, а именно:
     * - отступов, которые насчитал декоратор
     * - margins нашей view
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        // Посчитать декор (вызвать обязательно, иначе offset-декоратор не отработает)
        val decorRect = Rect()
        calculateItemDecorationsForChild(child, decorRect)

        resetMargins(child)

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, height - paddingTop)
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

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return super.scrollHorizontallyBy(dx, recycler, state)
    }


    private fun fillLeft(anchorView: View?, recycler: RecyclerView.Recycler) {

        val (anchorPos, anchorLeft) = if (anchorView != null) {
            getPosition(anchorView) to getDecoratedLeft(anchorView)
        } else 0 to paddingLeft

        var fillLeft = true
        var pos = anchorPos - 1

        var viewRight = anchorLeft
        val viewHeight = height - paddingTop
        val viewTop = paddingTop

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        while (fillLeft && pos >= 0) {
            var child = viewCache.get(pos) //проверяем кэш

            if (child == null) {
                //если вьюшки нет в кэше - просим у recycler новую, измеряем и лэйаутим её
                child = recycler.getViewForPosition(pos)
                addView(child, 0)
                measureChildWithoutInsets(child, widthSpec, heightSpec)

                val decoratedMeasuredWidth = getDecoratedMeasuredWidth(child)
                val decoratedMeasuredHeight = getDecoratedMeasuredHeight(child)

                layoutDecorated(
                    child,
                    viewRight - decoratedMeasuredWidth,
                    viewTop,
                    viewRight,
                    viewTop + decoratedMeasuredHeight
                )
            } else {
                //если вьюшка есть в кэше - просто аттачим её обратно
                //нет необходимости проводить measure/layout цикл.
                attachView(child)
                viewCache.remove(pos)
            }
            viewRight = getDecoratedLeft(child)
            fillLeft = viewRight > paddingLeft
            pos--
        }
    }

    /**
     * Заполнить экран (RecyclerView) элементами, взятыми у Recycler
     */
    private fun fillRight(anchorView: View?, recycler: RecyclerView.Recycler) {

        val (anchorPos, anchorRight) = if (anchorView != null) {
            getPosition(anchorView) to getDecoratedRight(anchorView)
        } else 0 to 0

        var pos = anchorPos
        var fillRight = true
        val height = height - paddingTop
        var viewLeft = anchorRight
        val viewTop = paddingTop

        val itemCount = itemCount
        val viewHeight = height - paddingTop

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        while (fillRight && pos < itemCount) {

            var child = viewCache.get(pos)

            if (child == null) {
                child = recycler.getViewForPosition(pos)
                addView(child)
                measureChildWithoutInsets(child, widthSpec, heightSpec)

                val decoratedMeasuredWidth = getDecoratedMeasuredWidth(child)
                val decoratedMeasuredHeight = getDecoratedMeasuredHeight(child)

                layoutDecorated(
                    child,
                    viewLeft,
                    viewTop,
                    viewLeft + decoratedMeasuredWidth,
                    viewTop + decoratedMeasuredHeight
                )
            } else {
                attachView(child)
                viewCache.remove(pos)
            }
            viewLeft = getDecoratedRight(child)
            fillRight = viewLeft <= width
            pos++
        }
    }

    /**
     * Найти "опорную" view
     */
    private fun getAnchorView(): View? {

        val viewsOnScreen = mutableMapOf<Int, View>()
        val mainRect = Rect(paddingLeft, paddingTop, width, height)

        for (i in 0 until childCount) {

            getChildAt(i)?.let { child ->
                val viewRect = Rect(
                    getDecoratedLeft(child),
                    getDecoratedTop(child),
                    getDecoratedRight(child),
                    getDecoratedBottom(child)
                )
                val intersect = viewRect.intersect(mainRect)
                if (intersect) {
                    val square = viewRect.width() * viewRect.height()
                    viewsOnScreen[square] = child
                }
            }
        }
        if (viewsOnScreen.isEmpty()) {
            return null
        }
        var maxSquare: Int? = null
        for (square in viewsOnScreen.keys) {
            maxSquare = if (maxSquare == null) square else max(maxSquare, square)
        }
        return viewsOnScreen[maxSquare]
    }

    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }
}