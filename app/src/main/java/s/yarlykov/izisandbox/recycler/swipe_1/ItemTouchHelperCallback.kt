package s.yarlykov.izisandbox.recycler.swipe

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

    /**
     * Переменная замораживает элемент в промежуточном положении
     */
    private var isFrozen = false

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
        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    /**
     * Это основная фича !
     * Короче, метод вызывается каждый раз, когда я собираюсь потянуть пальцем
     * на очередном элементе. Фактически это запрос свайпа для конкретного элемента.
     * И вот здесь можно временно приостановить свайп для всех остальных элементов
     * кроме того, кто поменял значение isFrozen на true. Для него свайп сохранится,
     * так как он ранее уже ответил true, а остальные подождут.
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return !isFrozen
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
     *
     * NOTE: Дефолтовая версия этого метода выполняет translate элемента
     * на указанные dX/dY. Вот здесь: https://bit.ly/2WAxrNR
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

        /**
         * Этот метод (как ни странно) вызывается для элементов, которые уже swiped.
         * Поэтому игнорим такие вызовы.
         */
        if (viewHolder.adapterPosition == -1) return

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

                    isFrozen = false
                    return super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                } else if (!isFrozen) {
                    isFrozen = true
                    viewHolder.itemView.apply {
                        logIt("Turning item ${viewHolder.adapterPosition}")

                        setOnTouchListener{ v, event ->
                            logIt("Touched item ${viewHolder.adapterPosition}")
                            parent.requestDisallowInterceptTouchEvent(true)
                            isClickable = true
                            isFocusable = true
                            isEnabled = true
                            true

                        }

                        isClickable = true
                        isFocusable = true
                        isEnabled = true
                    }
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

    /**
     * Метод вызывается перед тем как нужно включить анимацию, которая либо вернет
     * элемент в исходное состояние, либо просвайпит до конца.
     * Устанавливая animation duration в Long.MAX_VALUE мы замораживаем положение элемента
     * в его текущем промежуточном состоянии.
     */
    override fun getAnimationDuration(
        recyclerView: RecyclerView,
        animationType: Int,
        animateDx: Float,
        animateDy: Float
    ): Long {

        return if (isFrozen) {
            Long.MAX_VALUE
        } else {
            super.getAnimationDuration(recyclerView, animationType, animateDx, animateDy)
        }
    }
}