package s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt

/**
 * Код взят отсюда:
 * https://github.com/nemanja-kovacevic/recycler-view-swipe-to-delete
 * https://medium.com/nemanja-kovacevic/recyclerview-swipe-to-delete-no-3rd-party-lib-necessary-6bf6a6601214
 *
 */
class SwipeWithUndoActivity : AppCompatActivity() {

    companion object {
        const val PENDING_REMOVAL_TIMEOUT = 3000L
    }

    lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_with_undo)
        setSupportActionBar(findViewById(R.id.toolbar))

        recyclerView = findViewById(R.id.recycler_view)
        setUpRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_swipe_undo, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_item_undo_checkbox -> {
                item.isChecked = !item.isChecked

                (recyclerView.adapter as TestAdapter).isUndoOn = item.isChecked

            }
            R.id.menu_item_add_5_items -> {
                (recyclerView.adapter as TestAdapter).addItems(5)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpRecyclerView() {

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@SwipeWithUndoActivity)
            adapter = TestAdapter()
            setHasFixedSize(true)
        }

        setUpItemTouchHelper()
//        setUpAnimationDecoratorHelper()
    }

    /**
     * This is the standard support library way of implementing "swipe to delete" feature. You can do custom drawing in onChildDraw method
     * but whatever you draw will disappear once the swipe is over, and while the items are animating to their new position the recycler view
     * background will be visible. That is rarely an desired effect.
     */
    private fun setUpItemTouchHelper() {

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =

            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                // we want to cache these and not allocate anything repeatedly in the onChildDraw method
                private val background = ColorDrawable(Color.RED)
                private val xIcon =
                    ContextCompat.getDrawable(this@SwipeWithUndoActivity, R.drawable.ic_clear_24dp)

                var xIconMargin = 0
                var initiated = false

                init {
                    xIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
                    xIconMargin =
                        this@SwipeWithUndoActivity.resources.getDimension(R.dimen.ic_clear_margin)
                            .toInt()
                    initiated = true
                }

                /**
                 * Работает только для Drag (не этот случай)
                 */
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                /**
                 * Вызывается всякий раз когда начинаем тянуть отдельный элемент.
                 */
                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val position = viewHolder.adapterPosition
                    val testAdapter = recyclerView.adapter as TestAdapter

                    /**
                     * Если адаптер в режиме isUndoOn и для данной строки уже зашедулено
                     * удаление, то возращаем 0, показывая, что строку нельзя ни свайпить
                     * ни драгить.
                     */
                    val isPendingRemoval = testAdapter.isUndoOn && testAdapter.isPendingRemoval(position)

                    return if (isPendingRemoval) {
                        0
                    } else {
                        super.getSwipeDirs(recyclerView, viewHolder)
                    }
                }

                /**
                 * Вызывается для каждой строки после того, как она считается swiped.
                 * Так как fraction= 0.5, то метод вызывается ещё до того как завершится
                 * анимация ухода.
                 */
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    swipeDir: Int
                ) {
                    logIt("onSwiped position ${viewHolder.adapterPosition}")
                    val swipedPosition = viewHolder.adapterPosition
                    val adapter = recyclerView.adapter as TestAdapter
                    val undoOn = adapter.isUndoOn
                    if (undoOn) {
                        adapter.pendingRemoval(swipedPosition)
                    } else {
                        adapter.removeFromModel(swipedPosition)
                    }
                }

                override fun onChildDraw(
                    canvas: Canvas,
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    dX: Float,
                    dY: Float,
                    actionState: Int,
                    isCurrentlyActive: Boolean
                ) {
                    val itemView = viewHolder.itemView

                    // not sure why, but this method get's called for viewholder that are already swiped away
                    if (viewHolder.adapterPosition == -1) {
                        return
                    }

                    logIt("item right = ${itemView.right}")

                    // При перетаскивании влево значения itemView.right/itemView.left не изменяются !!!
                    // Это значения начального layout'а !!!
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    background.draw(canvas)

                    // Рисуем иконку (X)
                    val itemHeight = itemView.bottom - itemView.top

                    val intrinsicWidth = xIcon!!.intrinsicWidth
                    val intrinsicHeight = xIcon!!.intrinsicWidth

                    val xMarkLeft = itemView.right - xIconMargin - intrinsicWidth
                    val xMarkRight = itemView.right - xIconMargin
                    val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                    val xMarkBottom = xMarkTop + intrinsicHeight

                    xIcon!!.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)
                    xIcon!!.draw(canvas)

                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }

        val mItemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        mItemTouchHelper.attachToRecyclerView(recyclerView)
    }

    /**
     * We're gonna setup another ItemDecorator that will draw the red background in the empty space
     * while the items are animating to their new positions after an item is removed.
     */
    private fun setUpAnimationDecoratorHelper() {

        recyclerView!!.addItemDecoration(object : ItemDecoration() {

            // we want to cache this and not allocate anything repeatedly in the onDraw method
            var background: Drawable? = null
            var initiated = false

            private fun init() {
                background = ColorDrawable(Color.RED)
                initiated = true
            }

            override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                if (!initiated) {
                    init()
                }

                // only if animation is in progress
                if (parent.itemAnimator!!.isRunning) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    var lastViewComingDown: View? = null
                    var firstViewComingUp: View? = null

                    // this is fixed
                    val left = 0
                    val right = parent.width

                    // this we need to find out
                    var top = 0
                    var bottom = 0

                    // find relevant translating views
                    val childCount = parent.layoutManager!!.childCount
                    for (i in 0 until childCount) {
                        val child = parent.layoutManager!!.getChildAt(i)
                        if (child!!.translationY < 0) {
                            // view is coming down
                            lastViewComingDown = child
                        } else if (child.translationY > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child
                            }
                        }
                    }
                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top =
                            lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom =
                            firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top =
                            lastViewComingDown.bottom + lastViewComingDown.translationY.toInt()
                        bottom = lastViewComingDown.bottom
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.top
                        bottom =
                            firstViewComingUp.top + firstViewComingUp.translationY.toInt()
                    }
                    background!!.setBounds(left, top, right, bottom)
                    background!!.draw(canvas)
                }
                super.onDraw(canvas, parent, state)
            }
        })
    }

    /**
     * RecyclerView adapter enabling undo on a swiped away item.
     */

}