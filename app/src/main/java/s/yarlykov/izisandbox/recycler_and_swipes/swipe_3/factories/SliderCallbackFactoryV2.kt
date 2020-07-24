package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories

import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerDatum
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerRole
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.EditorAction
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.role

object SliderCallbackFactoryV1 {

    /**
     * Выполняет работу с элементами UI одинаковую для доера и для ресурса.
     * Отличия только в коде, который нужно вызвать по окончании анимации
     * ухода слайдера. Код для доера и для ресурса передается через
     * swipeToLeftHandler и swipeToRightHandler.
     */
    private fun createFromTemplate(
        slider: View,
        underLayer: View,
        state: UnderLayerState,
        swipeToLeftHandler: () -> Unit,
        swipeToRightHandler: () -> Unit
    ): (EditorAction) -> Unit {

        val iconLeft = underLayer.findViewById<ImageView>(R.id.under_layer_icon_left_1)
        val iconRight = underLayer.findViewById<ImageView>(R.id.under_layer_icon_right_1)
        val textLeft = underLayer.findViewById<TextView>(R.id.under_layer_text_left_1)
        val textRight = underLayer.findViewById<TextView>(R.id.under_layer_text_right_1)
        val pbLeft = underLayer.findViewById<ProgressBar>(R.id.pb_left_1)
        val pbRight = underLayer.findViewById<ProgressBar>(R.id.pb_right_1)
        val context = underLayer.context

        val whiteWithUnderline = ContextCompat.getDrawable(context, R.drawable.background_slider_round_white_underline)
        val whiteRounded = ContextCompat.getDrawable(context, R.drawable.background_slider_round_white)

        return { action ->
            when (action) {
                // Вызывается один раз при старте движения из исходного положения
                // или когда слайдер пересек левую границу своей области, то есть
                // значение его X сменило знак на (-)
                EditorAction.DragToLeft -> {
                    underLayer.setBackgroundResource(state.rightDrawableId)
                    state.rightTextId?.let {
                        textRight.text = context.getString(it)
                        textRight.visibility = View.VISIBLE
                    } ?: run { textRight.visibility = View.INVISIBLE }
                    state.rightIconId?.let {
                        iconRight.setImageResource(it)
                        iconRight.visibility = View.VISIBLE
                    } ?: run { iconRight.visibility = View.INVISIBLE }

                    textLeft.visibility = View.INVISIBLE
                    iconLeft.visibility = View.INVISIBLE

                    slider.background = whiteWithUnderline
                }
                // Вызывается один раз при старте движения из исходного положения
                // или когда слайдер пересек левую границу своей области, то есть
                // значение его X сменило знак на (+)
                EditorAction.DragToRight -> {
                    underLayer.setBackgroundResource(state.leftDrawableId)
                    state.leftTextId?.let {
                        textLeft.text = context.getString(it)
                        textLeft.visibility = View.VISIBLE
                    } ?: run { textLeft.visibility = View.INVISIBLE }
                    state.leftIconId?.let {
                        iconLeft.setImageResource(it)
                        iconLeft.visibility = View.VISIBLE
                    } ?: run { iconLeft.visibility = View.INVISIBLE }

                    textRight.visibility = View.INVISIBLE
                    iconRight.visibility = View.INVISIBLE

                    slider.background = whiteWithUnderline
                }
                // Началась анимация "уход" влево
                EditorAction.SwipeToLeftStart -> {
                    textLeft.visibility = View.INVISIBLE
                    iconLeft.visibility = View.INVISIBLE
                    pbLeft.visibility = View.INVISIBLE
                    pbRight.visibility = View.VISIBLE
                }
                // Началась анимация "уход" вправо
                EditorAction.SwipeToRightStart -> {
                    textRight.visibility = View.INVISIBLE
                    iconRight.visibility = View.INVISIBLE
                    pbRight.visibility = View.INVISIBLE
                    pbLeft.visibility = View.VISIBLE
                }
                // Завершение анимации
                EditorAction.SwipeToLeftEnd -> {
                    swipeToLeftHandler()
                }
                // Завершение анимации
                EditorAction.SwipeToRightEnd -> {
                    swipeToRightHandler()
                }
                EditorAction.SwipeToCenterEnd -> {
                    underLayer.setBackgroundResource(R.drawable.background_slider_round_white)
                    slider.background = whiteRounded
                }
                else -> {
                }
            }
        }
    }

    /**
     * Нужно инициализировать обработчики окончания анимации и передать их в createFromTemplate
     */
    fun createForDoer(
        slider: View,
        underLayer: View,
        state: UnderLayerState,
        adapterPosition: Int,
        user: DoerDatum,
        doerId: Int,
        isConfirmed: Boolean,
        clickHandler: (EditorAction, Int, Int) -> Unit
    ): (EditorAction) -> Unit {

        // Слайдер ушел влево (анимация завершилась)
        val swipeToLeftHandler = {
            when (user.role()) {
                DoerRole.Admin, DoerRole.Manager -> {
                    // У админа анимацией вправо завершается удаление исполнителя,
                    // поэтому можно сразу "выключить "весь underLayer
                    (underLayer.parent as View).visibility = View.GONE

                    clickHandler(EditorAction.DeleteDoer, adapterPosition, doerId)
                }
                else -> {
                    val editorAction = if (isConfirmed) EditorAction.RefuseAsDoer else EditorAction.ConfirmAsDoer
                    clickHandler(editorAction, adapterPosition, doerId)

                    // У исполнителя анимацией вправо (также как и влево) запускается процесс смены
                    // состояния (подтвердить/отказаться) поэтому не нужно скрывать underLayer,
                    // чтобы показать ProgressBar
//                    (underLayer.parent as View).visibility = View.GONE
                }
            }
        }

        // Слайдер ушел вправо (анимация завершилась)
        val swipeToRightHandler = {
            val whatToDo = when (user.role()) {
                DoerRole.Admin, DoerRole.Manager -> {
                    if (isConfirmed) EditorAction.WaitForDoer else EditorAction.ConfirmForDoer
                }
                else -> {
                    if (isConfirmed) EditorAction.RefuseAsDoer else EditorAction.ConfirmAsDoer
                }
            }
            clickHandler(whatToDo, adapterPosition, doerId)
//            (underLayer.parent as View).visibility = View.GONE
        }

        return createFromTemplate(
            slider,
            underLayer,
            state,
            swipeToLeftHandler,
            swipeToRightHandler
        )
    }

    /**
     * Нужно инициализировать обработчики окончания анимации и передать их в createFromTemplate
     */
    fun createForResource(
        slider: View,
        underLayer: View,
        state: UnderLayerState,
        resourceId: Int,
        adapterPosition: Int,
        user: DoerDatum,
        clickHandler: (EditorAction, Int, Int) -> Unit
    ): (EditorAction) -> Unit {

        // Слайдер ушел влево (анимация завершилась)
        val swipeToLeftHandler = {
            clickHandler(EditorAction.DeleteResource, adapterPosition, resourceId)
        }
        // Слайдер ушел вправо (анимация завершилась)
        val swipeToRightHandler = {
            clickHandler(EditorAction.DeleteResource, adapterPosition, resourceId)
        }

        return createFromTemplate(
            slider,
            underLayer,
            state,
            swipeToLeftHandler,
            swipeToRightHandler
        )
    }
}