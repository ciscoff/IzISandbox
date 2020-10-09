package s.yarlykov.izisandbox.telegram.v2

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SmartRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var lastRawY = 0f

    private var isPullingDown = false
    private var isPullingUp = false

    private var offsetListener: ((Int) -> Unit)? = null

    private fun onTouchBegin(view: View, event: MotionEvent): Boolean {
        val rect = Rect()
        view.getGlobalVisibleRect(rect)

        return if (rect.contains(event.rawX.toInt(), event.rawY.toInt())) {
            lastRawY = event.rawY
            isPullingDown = false
            isPullingUp = false
            true
        } else {
            false
        }
    }

    fun setOnOffsetListener(op: (Int) -> Unit) {
        offsetListener = op
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val layoutManager = layoutManager as CustomLayoutManager

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchBegin(this, event)
            }
            /**
             * Основная задача обработки ACTION_MOVE - выбрать правильный момент, чтобы известить
             * offsetListener о смещении пальца. Такие сообщения нужно отправлять только когда
             * "тянем" AppBar вниз или "толкаем" вверх. Далее AppBar будет решать, нужно ли ему
             * менять высоту и в какую сторону. Кроме этого AppBar устанавливает флаг разрешающий
             * скроллинг элементов списка. Собственно сама прокрутка работает только когда
             * AppBar в свернутом состоянии. В этом случае можно прокручивать контент вверх (как бы
             * пихать "под" AppBar) или вниз (вытягивать из под AppBar'а).
             */
            MotionEvent.ACTION_MOVE -> {
                // Дистанция от места предыдущего тача до текущего положения пальца
                val rawOffset = event.rawY - lastRawY
                lastRawY = event.rawY

                // Палец тянет вниз
                if (rawOffset > 0) {

                    if (isPullingDown) {
                        isPullingUp = false
                        offsetListener?.invoke(rawOffset.toInt())
                    }
                    // "Зацепились" за нижнюю границу AppBar'а. Начинаем тянуть AppBar за собой вниз.
                    else if (layoutManager.firstVisiblePosition == 0 && layoutManager.firstVisibleTop == 0) {
                        isPullingDown = true
                        isPullingUp = false
                        offsetListener?.invoke(rawOffset.toInt())
                    }
                }
                // Палец тянет вверх
                else if (rawOffset < 0) {

                    if (isPullingUp) {
                        isPullingDown = false
                        offsetListener?.invoke(rawOffset.toInt())
                    }
                    // "Уперлись" в нижнюю границу AppBar'а. Начинаем толкать AppBar перед собой вверх.
                    else if (layoutManager.firstVisiblePosition == 0 && layoutManager.firstVisibleTop == 0) {
                        isPullingUp = true
                        isPullingDown = false
                        offsetListener?.invoke(rawOffset.toInt())
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                isPullingDown = false
                isPullingUp = false
            }
        }
        return super.onTouchEvent(event)
    }
}