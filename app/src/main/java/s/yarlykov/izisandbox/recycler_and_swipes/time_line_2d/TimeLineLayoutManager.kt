package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.max
import kotlin.math.min

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

    private var scaleHeight = 2

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

        // Помещаем вьюшки в cache и...
        for (i in 0 until childCount) {
            getChildAt(i)?.let { view ->
                viewCache.put(getPosition(view), view)
            }
        }

        //... и удалям из layout'a
        for (i in 0 until viewCache.size()) {
            detachView(viewCache.valueAt(i))
        }

        // Заполнить список элементами
        fillLeft(anchorView, recycler)
        fillRight(anchorView, recycler)

        // Удалить элементы, который вышли из области видимости.
        for (i in 0 until viewCache.size()) {
            recycler.recycleView(viewCache.valueAt(i))
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
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, (height - paddingTop) * scaleHeight)
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
        dX: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {
        val delta = scrollHorizontallyInternal(dX)

        if (delta != 0) {
            offsetChildrenHorizontal(-delta)
            fill(recycler)
        }

        return delta
    }

    override fun scrollVerticallyBy(
        dY: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {

        val delta =  when {
            // Палец идет вверх. Контролируем появление View снизу.
            (dY > 0) -> translateUp(dY, recycler)
            // Палец идет вниз. Контролируем появление View сверху.
            (dY < 0) -> translateDown(dY, recycler)
            else -> {
                0
            }
        }

        if(delta != 0) offsetChildrenVertical(-delta)

        logIt("Translate vert $delta")

        return delta
    }

    private fun translateUp(dY: Int, recycler: RecyclerView.Recycler): Int {

        return getChildAt(0)?.let { firstChild ->

            val bottomView = getDecoratedBottom(firstChild)
            if (bottomView == height) return 0

            min(dY, bottomView - height)

        } ?: 0
    }

    private fun translateDown(dY: Int, recycler: RecyclerView.Recycler): Int {

        return getChildAt(0)?.let { firstChild ->

            val topView = getDecoratedTop(firstChild)
            if (topView == paddingTop) return 0
            max(dY, topView - height)

        } ?: 0

    }


    private fun fillLeft(anchorView: View?, recycler: RecyclerView.Recycler) {

        val (anchorPos, anchorLeft) =
            if (anchorView != null) {
                getPosition(anchorView) to getDecoratedLeft(anchorView)
            } else 0 to paddingLeft

        var viewRight = anchorLeft
        val viewHeight = (height - paddingTop) * scaleHeight
        val viewTop = paddingTop

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        var pos = anchorPos - 1
        var fillLeft = viewRight > paddingLeft

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
        } else 0 to paddingLeft

        var viewLeft = anchorRight
        val viewTop = paddingTop
        val viewHeight = (height - paddingTop) * scaleHeight

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight, View.MeasureSpec.EXACTLY)

        var pos = anchorPos
        var fillRight = viewLeft < width

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
     * Найти "опорную" view. Поиск по наибольшей площади child'а. Если несколько child'ов
     * имеют одинаковую максимальную площадь, то по умолчанию в hashMap'е останется последний
     * добавленный. Однако я изменил условия добавления. Остается первый добавленный. Это помогло
     * решить проблему с горизонтальным скролом слева направо.
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
                    if (!viewsOnScreen.containsKey(square)) {
                        viewsOnScreen[square] = child
                    }
                }
            }
        }
        if (viewsOnScreen.isEmpty()) {
            return null
        }
        var maxSquare = 0
        for (square in viewsOnScreen.keys) {
            maxSquare = max(maxSquare, square)
        }
        return viewsOnScreen[maxSquare]
    }

    private fun scrollHorizontallyInternal(dX: Int): Int {

        if (childCount == 0) {
            return 0
        }

//        val viewPortWidth = width - paddingLeft
//        val rightView = getChildAt(childCount - 1)!!
//        val leftView = getChildAt(0)!!
//        val viewSpan = getDecoratedRight(rightView) - getDecoratedLeft(leftView)
//        if (viewSpan <= viewPortWidth && getPosition(rightView) == (itemCount - 1)) {
//            return 0
//        }
//        logIt("scrollHorizontallyInternal childCount=$childCount, viewPortWidth=$viewPortWidth")

        var delta = 0

        // Палец идет вправо. Контролируем появление элементов слева.
        if (dX < 0) {

            val firstVisibleView = getChildAt(0)!!
            val firstVisibleViewAdapterPos = getPosition(firstVisibleView)

            val d = getDecoratedLeft(firstVisibleView)
            val d1 = paddingLeft

            delta = if (firstVisibleViewAdapterPos > 0) {
//                logIt("firstVisibleViewAdapterPos =$firstVisibleViewAdapterPos, dX=$dX")
                dX
            } else {
//                logIt("firstVisibleViewAdapterPos =$firstVisibleViewAdapterPos, getDecoratedLeft=$d, paddingLeft=$d1, dX=$dX")
                max(getDecoratedLeft(firstVisibleView) - paddingLeft, dX)
            }
        }
        // Палец идет влево. Контролируем появление элементов справа.
        else if (dX > 0) {

            val lastView = getChildAt(childCount - 1)!!
            val lastViewAdapterPos = getPosition(lastView)

            delta =
                if (lastViewAdapterPos < itemCount - 1) {
                    dX
                } else {
                    min(getDecoratedRight(lastView) - width, dX)
                }
        }
        return delta
    }

    override fun canScrollHorizontally(): Boolean = true
    override fun canScrollVertically(): Boolean = true

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
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
}