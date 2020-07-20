package s.yarlykov.izisandbox.recycler

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.dimensionPix

/**
 * Основная задача: свайп влево обработать как обычно - удаляем элемент. Свайт вправо должен
 * остановиться на половине экрана, чтобы предложить ползователю подтвердить действие.
 *
 * Есть важный момент: значение возвращаемое из getSwipeThreshold() должно быть больше
 * RecyclerView.width/2. Дело в том, что мы тормозим свайп в центре экрана. И в этот момент
 * свайп не должен считаться завершенным. То есть он не должен дотягивать до критического значения.
 *
 * В результате проведенного тюнинга с помощью методов getSwipeThreshold(),
 * getSwipeEscapeVelocity(), getAnimationDuration() удалось добится предсказуемого поведения.
 *
 */

class ItemTouchHelperCallback(
    private val context: Context,
    private val adapter: ItemTouchHelperAdapter
) : ItemTouchHelper.Callback() {

    private val background = ColorDrawable()

    private val iconWidth = context.dimensionPix(R.dimen.touch_helper_bucket_icon_size)
    private val iconHeight = context.dimensionPix(R.dimen.touch_helper_bucket_icon_size)
    private val iconMarginX = context.dimensionPix(R.dimen.touch_helper_bucket_icon_margin_x_axis)
    private val iconDelete = ContextCompat.getDrawable(context, R.drawable.vd_delete_icon)

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        val dragFlags = 0
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        logIt("onSwiped")
        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//        when(actionState) {
//            ItemTouchHelper.ACTION_STATE_SWIPE -> {
//
//                (viewHolder as RecyclerViewHolder).itemView.animate().translationX(300f).apply {
//                    duration = 500L
//                }.start()
//            }
//
//        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    /**
     * Минимальная скорость, при достижении которой считается, что swipe имеет место.
     * Измеряется в px/sec. Видимо зависит от железа (разрешения экрана),
     * На моем Asus defaultValue = 360.0, в эмуляторе 315.0
     *
     * Я увеличил скорость, чтобы было сложно смахнуть неумышленно.
     */
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return defaultValue * 9f
    }

    /**
     * Определить fraction при достижении которой item считается swiped.
     *
     * Эта величина - процент от ширины RecyclerView. Если элемент сдвинулся вправо/влево
     * на эту величину, то он считается swiped и генерится onSwiped().
     *
     */
    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.75f
    }

    /**
     * Рисуем фон на освобождающемся месте и поверх него иконку корзинки удаления
     */
    override fun onChildDraw(
        canvas: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val dbgPrefix =
            "${this::class.java.simpleName}::${object {}.javaClass.enclosingMethod?.name}"

        val itemView = viewHolder.itemView

        val iconMarginY = (itemView.height - iconHeight) / 2
        val iconTop = itemView.top + iconMarginY
        val iconBottom = iconTop + iconHeight

        val (iconLeft, iconRight) = getIconPositionHorizontal(itemView, iconMarginX, dX)

        when {
            // Swipe Left -> Right
            (dX > 0) -> {

                background.apply {
                    setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                    color = ContextCompat.getColor(context, R.color.colorAccent)
                    draw(canvas)
                }

                iconDelete!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                iconDelete.draw(canvas)

            }
            // Swipe Right -> Left
            (dX < 0) -> {
                background.apply {
                    setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                    color = ContextCompat.getColor(context, R.color.colorAccent)
                    draw(canvas)
                }

                iconDelete!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                iconDelete.draw(canvas)
            }
            else -> {
            }
        }

        /**
         * Вот тут самый цимес. По положению сдвинутого item определяем значение для isFinite,
         * которая управляет продолжительностью анимации. Если не дотянули до (itemView.width / 2),
         * то анимация дефолтовая, если дотянули, то вообще никакой. Продолжительность анимации
         * сообщяет метод getAnimationDuration() см. ниже.
         */
        when (actionState) {
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                if (dX < itemView.width / 3) {
                    logIt("dX < itemView.width / 3, dX=$dX, isCurrentlyActive=$isCurrentlyActive")

                    isFinite = false
                    return super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                } else {
                    itemView.animation?.let {
                        it.cancel()
                    }
                    itemView.clearAnimation()
                    logIt("dX > itemView.width / 3, dX=$dX, isCurrentlyActive=$isCurrentlyActive")
                    isFinite = true
                }
            }
        }
    }

    /**
     * Расчитать позицию иконки (left, top)
     */
    private fun getIconPositionHorizontal(
        itemView: View,
        iconMargin: Int,
        dX: Float
    ): Pair<Int, Int> {
        val iconLeft: Int
        val iconRight: Int

        // Swipe LEFT->RIGHT
        if (dX > 0) {
            iconLeft = itemView.left + iconMargin
            iconRight = iconLeft + iconWidth
        }
        // Swipe RIGHT->LEFT
        else {
            iconRight = itemView.right - iconMargin
            iconLeft = iconRight - iconWidth
        }

        return Pair(iconLeft, iconRight)
    }

    private var isFinite = false

    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {

        return if (isFinite) {
            Long.MAX_VALUE
        } else {
            super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }
    }
}