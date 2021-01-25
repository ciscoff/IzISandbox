package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import kotlin.math.max
import kotlin.math.min

class TimeLineLayoutManager(val context: Context) : RecyclerView.LayoutManager(), ZoomConsumer {

    /**
     * Масштабирование элементов списка по высоте
     */
    private var scaleHeight = 1f

    /**
     * Ширина отдельного элемента
     */
    private val spanSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.column_width)
    }

    private val viewCache = SparseArray<View>()

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State?) {

        // Адаптер пустой или стал пустым после обновления модели
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }

        if (state?.isPreLayout == true) return

        detachAndScrapAttachedViews(recycler)

        fill(recycler)
    }

    /**
     * Сюда приходим при начальном Layout'е и после каждого scroll'a
     *
     * NOTE: Реальная прокрутка происходит в горизонтальном направлении. В этом случае
     * нужно контролировать появление новых элементов и утилизацию старых. Сложность
     * появляется при реализации вертикальной "прокрутки". Например, сначала сдвинули
     * вверх видимые столбики, а затем проскролили в сторону и вызвали появление нового столбика.
     * Он ничего не знает о том, что его собраться смещены вертикально и выводится на экран
     * в дефолтовом положении. Необходимо явным образом установить ему offset ориентируясь
     * на текущий вертикальный offset якорной anchorView (см в методах fillLeft/fillRight вызовы
     * child.offsetTopAndBottom). То есть anchorView является не только опорной для горизонтальной
     * прокрутки, но и для вертикальной.
     */
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

        // Удалить элементы, которые вышли из области видимости.
        for (i in 0 until viewCache.size()) {
            recycler.recycleView(viewCache.valueAt(i))
        }
    }

    /**
     * Расчитать "чистый" размер child'а, без inset'ов, а именно:
     * - без отступов, которые насчитал декоратор
     * - без margins child'а
     */
    private fun measureChildWithoutInsets(child: View, widthSpec: Int, heightSpec: Int) {

        // Посчитать декор (вызвать обязательно, иначе offset-декоратор не отработает)
        val decorRect = Rect()
        calculateItemDecorationsForChild(child, decorRect)

        resetMargins(child)

        val widthSpecUpdated = updateMeasureSpecs(widthSpec, spanSize)
        val heightSpecUpdated = updateMeasureSpecs(heightSpec, ((height - paddingTop) * scaleHeight).toInt())
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

    /**
     * Вертикальный скролл начинает работать если scaleHeight > 1, то есть при увеличенном
     * масштабе. В этом случае "столбики" ходят вверх/вниз ограниченные областью видимости
     * RecyclerView. То есть прокрутки нет, а есть смещения.
     */
    override fun scrollVerticallyBy(
        dY: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State?
    ): Int {


        val delta = when {
            // Палец идет вверх. Контролируем появление View снизу.
            (dY > 0) -> deltaUp(dY, recycler)
            // Палец идет вниз. Контролируем появление View сверху.
            (dY < 0) -> deltaDown(dY, recycler)
            else -> {
                0
            }
        }

        if (delta != 0) {
            offsetChildrenVertical(-delta)
            fill(recycler)
        }

        return delta
    }

    /**
     * dY > 0
     *
     * Расчитать смещение вверх.
     */
    private fun deltaUp(dY: Int, recycler: RecyclerView.Recycler): Int {
        return getChildAt(0)?.let { firstChild ->
            val bottomView = getDecoratedBottom(firstChild)
            if (bottomView == height) return 0
            min(dY, bottomView - height)
        } ?: 0
    }

    /**
     * dY < 0
     *
     * Расчитать смещение вниз.
     */
    private fun deltaDown(dY: Int, recycler: RecyclerView.Recycler): Int {
        return getChildAt(0)?.let { firstChild ->
            val topView = getDecoratedTop(firstChild)
            if (topView >= paddingTop) return 0
            max(dY, topView - paddingTop)
        } ?: 0
    }

    /**
     * Отрисовать элементы левее опорного anchorView
     */
    private fun fillLeft(anchorView: View?, recycler: RecyclerView.Recycler) {

        val (anchorPos, anchorLeft) =
            if (anchorView != null) {
                getPosition(anchorView) to getDecoratedLeft(anchorView)
            } else 0 to paddingLeft

        var viewRight = anchorLeft
        val viewHeight = (height - paddingTop - paddingBottom) * scaleHeight

        // Верхняя позиция с поправкой на вертикальный сдвиг
        val viewTop = paddingTop

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight.toInt(), View.MeasureSpec.EXACTLY)

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

                /**
                 * см. коммент для fill()
                 */
                anchorView?.let { av ->
                    val d = getDecoratedTop(av) - getDecoratedTop(child)
                    child.offsetTopAndBottom(d)
                }

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
     * Отрисовать опорный anchorView и элементы правее него
     */
    private fun fillRight(anchorView: View?, recycler: RecyclerView.Recycler) {

        val (anchorPos, anchorRight) = if (anchorView != null) {
            getPosition(anchorView) to getDecoratedRight(anchorView)
        } else 0 to paddingLeft

        var viewLeft = anchorRight
        val viewTop = paddingTop
        val viewHeight = (height - paddingTop) * scaleHeight

        val widthSpec = View.MeasureSpec.makeMeasureSpec(spanSize, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(viewHeight.toInt(), View.MeasureSpec.EXACTLY)

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

                /**
                 * см. коммент для fill()
                 */
                anchorView?.let { av ->
                    val d = getDecoratedTop(av) - getDecoratedTop(child)
                    child.offsetTopAndBottom(d)
                }

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
        val mainRect = Rect(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )

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

    /**
     * Установить размер в MeasureSpec.
     */
    private fun updateMeasureSpecs(spec: Int, size: Int): Int {
        val mode = View.MeasureSpec.getMode(spec)
        if (mode == View.MeasureSpec.AT_MOST || mode == View.MeasureSpec.EXACTLY) {
            return View.MeasureSpec.makeMeasureSpec(size, mode)
        }
        return spec
    }

    /**
     * Посчитать горизонтальное смещение.
     */
    private fun scrollHorizontallyInternal(dX: Int): Int {

        if (childCount == 0) {
            return 0
        }

        var delta = 0

        // Палец идет вправо. Контролируем появление элементов слева.
        if (dX < 0) {

            val firstVisibleView = getChildAt(0)!!
            val firstVisibleViewAdapterPos = getPosition(firstVisibleView)

            delta = if (firstVisibleViewAdapterPos > 0) {
                dX
            } else {
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

    override fun onZoomChanged(zoom: Float) {
        scaleHeight = zoom
    }
}