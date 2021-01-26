package s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class ColumnView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /**
     * У всех элементов списка должны быть уникальные ID чтобы можно было однозначно
     * идентифицировать любой из них и поручать ему выполнить определенные действия.
     * В данной реализации декоратор первого видимого элемента списка рисует шкалу времени.
     */
    init {
        id = generateViewId()
    }

    /**
     * Во избежание сюрпризов это нужно переопределить и всегда возвращать false, то есть нужно
     * всю обработку отдать в наш ColumnTouchListener.
     *
     * NOTE: Если у view установлен OnTouchListener, то он вызывается первым из
     * view.dispatchTouchEvent. Если OnTouchListener вернет true, то все ОК и onTouchEvent не
     * вызываестся, но если OnTouchListener вернет false, то вызывается onTouchEvent, который
     * для ACTION_DOWN (цука) постоянно возращает true. И получается, что мой false на ACTION_DOWN
     * в OnTouchListener'е теряется и получается непонятное поведение.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return false
    }
}