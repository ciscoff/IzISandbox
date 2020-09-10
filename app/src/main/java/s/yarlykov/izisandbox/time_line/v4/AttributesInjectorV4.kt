package s.yarlykov.izisandbox.time_line.v4

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import s.yarlykov.izisandbox.R

class AttributesInjectorV4(val context: Context, val attrs: AttributeSet?) : AttributesAccessorV4 {

    /**
     * Читаем кастомный enum-атрибут из XML
     */
    private inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
        getInt(index, -1).let {
            if (it >= 0) enumValues<T>()[it] else default
        }

    override val mode: SeverityMode
        get() {
            val mode: SeverityMode

            context.obtainStyledAttributes(attrs, R.styleable.TimeSurfaceV4)
                .apply {
                    mode = getEnum(R.styleable.TimeSurfaceV4_mode, SeverityMode.Master)
                }.recycle()

            return mode
        }
}