package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.arch.v3

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.utils.logIt

/**
 * Горизонтальная/Вертикальная прокрутки должны работать при:
 * - Касании на дочернем элементе ВНЕ синей зоны.
 * - Касании в пространстве МЕЖДУ дочерними элементами.
 *
 * Горизонтальная/Вертикальная прокрутки НЕ должны работать при:
 * - Касании на дочернем элементе ВНУТРИ синей зоны (включая перетаскивание).
 * - ZOOM'е синей зоны дочернего элемета.
 *
 * Для скрола необходимо наличие следующих событий: ACTION_DOWN/ACTION_MOVE. Для перетаскивания
 * синей зоны и zoom'а они также необходимы. Поэтому должны обрабатываться и в родителе и в
 * детях. Ребенок будет запрещать родителю пользоваться ACTION_MOVE при перетаскивании
 * синей зоны и её zoom'а.
 */
class ContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        private const val INVALID_ID = Int.MAX_VALUE
    }

    private var touchListener: OnTouchListener? = null
    private var touchOwnerId: Int = INVALID_ID
    private var allowIntercept = true
    private var childScaling = false

    override fun setOnTouchListener(l: OnTouchListener?) {
        touchListener = l
    }

    /**
     * 1. MotionEvent.ACTION_DOWN необходимо всегда отдавать дочерним элементам потому что
     * они перемещают и масштабируют ползунок и им для этого требуется ловить начало тача.
     * 2. MotionEvent.ACTION_DOWN нужно и здесь обрабатывать, чтобы поймать начало тача для
     * горизонтального скрола.
     * 3. OnTouchListener должен всегда вызываться, а его результат возвращаться из
     * dispatchTouchEvent (в данном случае используется результат, который возвращается самой
     * dispatchTouchEvent). OnTouchListener живет в активити и выполняет "косметику".
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        // 1. Предварительная обработка
        when (event.actionMasked) {
            // Зафиксировать у себя начало тача через onTouchEvent().
            MotionEvent.ACTION_DOWN -> {
                touchOwnerId = event.ownerId
                onTouchEvent(event)
            }
            // Тач закончился, зафиксировать это. Разрешить intercept, сбросить child ID.
            // NOTE: без вызова onTouchEvent() не работает анимированный скролинг.
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                touchOwnerId = INVALID_ID
                allowIntercept = true
                childScaling = false
                onTouchEvent(event)
            }
        }

        // 2. Вызвать OnTouchListener (косметика в активити)
        touchListener?.onTouch(this, event)

        // 3.1 Если дети разрешают intercept, то пробуем захватить ACTION_MOVE
        // для горизонтального скрола.
        return if (allowIntercept && onInterceptTouchEvent(event)) {
            onTouchEvent(event)
        }
        // 3.2 Передать событие дочернему элементу.
        else {
            event.owner?.dispatchTouchEvent(event) ?: true
        }
    }

    /**
     * Запретить/Разрешить перехват событий
     */
    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        allowIntercept = !disallowIntercept
    }

    /**
     * NOTE: ScaleDetector может зафиксировать scaleBegin не после того как поставили на экран
     * второй палец, а только после первого move двумя пальцами. И только тогда он сможет вызвать
     * requestDisallowInterceptTouchEvent. Но до этого момента мы перехватываем все move что
     * не даст детектору начать работу. Поэтому мы самостоятельно устанавливаем флаг childScaling
     * после касания вторым пальцем. Тогда перехват ACTION_MOVE проходит с проверкой и все ОК.
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        return when (event.actionMasked) {

            // Можем перехватить, только если тапнули МЕЖДУ дочерними элементами (иначе скрол
            // не начнется, если тапнули в разделитель)
            MotionEvent.ACTION_DOWN -> {
                logIt("onIntercept:ACTION_DOWN")
                touchOwnerId == INVALID_ID
            }

            // Можем перехватить, только если "внизу" не выполняют scale (childScaling == false)
            MotionEvent.ACTION_MOVE -> {
                logIt("onIntercept:ACTION_MOVE")
                !childScaling
            }

            // Передать "вниз" для работы ScaleGestureDetector'a и установить флаг, который
            // запретит перехват ACTION_MOVE.
            MotionEvent.ACTION_POINTER_DOWN -> {
                logIt("onIntercept:ACTION_POINTER_DOWN")
                childScaling = true
                false
            }

            // Передать "вниз" для работы ScaleGestureDetector'a и сбросить флаг, что позволит
            // далее перехватывать ACTION_MOVE.
            // NOTE: Во время своей работы ScaleDetector блокирует работу с Intercept'ом поэтому
            // эта проверка не будет выполняться.
            MotionEvent.ACTION_POINTER_UP -> {
                logIt("onIntercept:ACTION_POINTER_UP")
                childScaling = false
                false
            }

            // Передать "вниз"
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                logIt("onIntercept:ACTION_CANCEL|ACTION_UP")
                false
            }

            else -> false
        }
    }

    /**
     * Используется для отладки
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {

            MotionEvent.ACTION_DOWN -> {
                logIt("onTouchEvent:ACTION_DOWN")
            }

            MotionEvent.ACTION_MOVE -> {
                logIt("onTouchEvent:ACTION_MOVE")
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                logIt("onTouchEvent:ACTION_POINTER_UP")
            }

            MotionEvent.ACTION_POINTER_UP -> {
                logIt("onTouchEvent:ACTION_POINTER_UP")
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                logIt("onTouchEvent:ACTION_CANCEL|ACTION_UP")
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * Найти child'а, которому принадлежит event
     */
    private val MotionEvent.owner: View?
        get() {
            val viewRect = Rect()
            children.forEach {
                viewRect.set(it.left, it.top, it.right, it.bottom)
                if (viewRect.contains(x.toInt(), y.toInt())) return it
            }
            return null
        }

    /**
     * Найти id child'а, которому принадлежит event
     */
    private val MotionEvent.ownerId: Int
        get() = owner?.id ?: INVALID_ID
}