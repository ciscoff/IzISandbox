package s.yarlykov.izisandbox.recycler_and_swipes.helpers

import android.os.Handler

/**
 * Объеккт позволяет фиксировать long Press через состояние своства isOccurred
 *
 * Работает слебующим образом:
 *
 * MotionEvent.ACTION_DOWN ->
 *   LongPress.restart()
 *   view.parent.requestDisallowInterceptTouchEvent(true)
 *
 * MotionEvent.ACTION_MOVE ->
 *   if(LongPress.isOccurred) {
 *     view.parent.requestDisallowInterceptTouchEvent(true)
 *     .. выполняем то что нам нужно, например двигаем view вправо/влево
 *   } else {
 *     v.parent.requestDisallowInterceptTouchEvent(false)
 *   }
 *
 * MotionEvent.ACTION_UP ->
 *   view.parent.requestDisallowInterceptTouchEvent(false)
 *   if(LongPress.isOccurred) {
 *     .. выполняем то что нам нужно, например анимируем view в исходное положение
 *   } else {
 *      LongPress.reset()
 *   }
 *
 * Итак, при нажатии (ACTION_DOWN) фиксируем начало процесса и блокируем перехваты в родителе.
 * Следующие ACTION_MOVE приходят к нам, но если это происходит до isOccurred, то разрешаем
 * родителю перехваты. Однако это не мешает нам продолжать получать ACTION_MOVE и в момент
 * когда isOccurred становится true, то снова запрещаем родителю перехваты и делаем свою
 * отложенную работу.
 *
 * По поводу ACTION_UP. Сразу разрешаем родителю перехваты. Далее по ситуации: если UP случился
 * до isOccurred, то просто ресетаем LongPress, в противном случае делаем свои дела и в конце
 * также ресетаем LongPress.
 *
 */
object LongPress {

    var isOccurred = false

    private const val delay = 300L

    private val handler = Handler()

    private val callback = {
        isOccurred = true
    }

    fun set() {
        handler.postDelayed(callback, delay)
    }

    fun reset() {
        handler.removeCallbacksAndMessages(null)
        isOccurred = false
    }

    fun restart() {
        reset()
        set()
    }
}