package s.yarlykov.izisandbox.telegram.v2

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomLayoutManager(context: Context, private val appBar: ActionBarLayout) :
    LinearLayoutManager(context) {

    lateinit var recyclerView: SmartRecyclerView

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        recyclerView = view as SmartRecyclerView
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return true
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
//        logIt("onLayoutChildren", TAG_DEBUG)
    }

    /**
     * Позиция в адаптере первого видимого элемента
     */
    val firstVisiblePosition: Int
        get() {
            return getChildAt(0)?.let { child ->
                getPosition(child)
            } ?: NOT_FIRST_ON_TOP
        }

    /**
     * Координата верхней границы первого видимого элемента
     */
    val firstVisibleTop: Int
        get() {
            return getChildAt(0)?.let { child ->
                getDecoratedTop(child)
            } ?: Int.MIN_VALUE
        }

    /**
     * Делаем прокрутку, чтолько если AppBar это разрешает.
     */
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return if (appBar.scrollingAllowed)
            super.scrollVerticallyBy(dy, recycler, state)
        else
            0
    }
}