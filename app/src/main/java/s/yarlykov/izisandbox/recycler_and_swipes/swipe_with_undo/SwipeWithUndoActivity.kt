package s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo

import android.graphics.*
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

                (recyclerView.adapter as TestAdapterV2).isUndoOn = item.isChecked

            }
            R.id.menu_item_add_5_items -> {
                (recyclerView.adapter as TestAdapterV2).addItems(5)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setUpRecyclerView() {

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@SwipeWithUndoActivity)
            adapter = TestAdapterV2()
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

        val context = this

        val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =

            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

                private val background = ColorDrawable(Color.RED)
                private val xIcon = ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_clear_24dp
                )

                var xIconMargin = context.resources.getDimension(R.dimen.ic_clear_margin).toInt()
                var initiated = false

                init {
                    xIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
//                    xIcon?.colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_ATOP)
                    initiated = true
                }

                /**
                 * Работает только для Drag (не наш случай)
                 */
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                /**
                 * Вызывается ВСЯКИЙ РАЗ когда начинаем тянуть отдельный элемент.
                 */
                override fun getSwipeDirs(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    val position = viewHolder.adapterPosition
                    val testAdapter = recyclerView.adapter as TestAdapterV2

                    /**
                     * Если адаптер в режиме isUndoOn и для данной строки уже зашедулено
                     * удаление, то возвращаем 0, показывая, что строку нельзя ни свайпить
                     * ни драгить.
                     */
                    val isPendingRemoval =
                        testAdapter.isUndoOn && testAdapter.isPendingRemoval(position)

                    return if (isPendingRemoval) {
                        0
                    } else {
                        super.getSwipeDirs(recyclerView, viewHolder)
                    }
                }

                /**
                 * Вызывается для каждой строки после того, как она считается swiped.
                 *
                 * NOTE: Основной момент - подразумевается, что я должен удалить
                 * свайпнутый элемент из модели и обновить адаптер. То есть я свайпнул элемент,
                 * отработала анимация ухода влево, затем удаляю элемент из модели и извещаю адаптер.
                 * Список перерисовывается уже без элемента. Однако, если элемент не удалять,
                 * то при последующем onBindViewHolder он автоматически вернется назад, да еще с
                 * reverse анимацией !!!
                 */
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    swipeDir: Int
                ) {
                    val swipedPosition = viewHolder.adapterPosition
                    val adapter = recyclerView.adapter as TestAdapterV2
                    val undoOn = adapter.isUndoOn
                    if (undoOn) {
                        adapter.putInRemoval(swipedPosition)
                    } else {
                        adapter.removeInstantly(swipedPosition)
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

                    // Метод onChildDraw почему-то вызывается для viewHolder'ов, которые уже swiped
                    if (viewHolder.adapterPosition == -1) {
                        return
                    }

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
                    xIcon?.let { icon ->
                        val itemHeight = itemView.bottom - itemView.top

                        val intrinsicWidth = icon.intrinsicWidth
                        val intrinsicHeight = icon.intrinsicWidth

                        val xMarkLeft = itemView.right - xIconMargin - intrinsicWidth
                        val xMarkRight = itemView.right - xIconMargin
                        val xMarkTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                        val xMarkBottom = xMarkTop + intrinsicHeight

                        icon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom)
                        icon.draw(canvas)
                    }

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