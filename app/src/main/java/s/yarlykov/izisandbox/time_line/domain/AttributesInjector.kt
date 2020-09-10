package s.yarlykov.izisandbox.time_line.domain

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import s.yarlykov.izisandbox.R

class AttributesInjector(val context: Context, val attrs: AttributeSet?) :
    AttributesAccessor {

    private val _mode: SeverityMode
    private val _severityMode: SeverityMode
    private val _controlMode: SeverityMode

    /**
     * Читаем кастомный enum-атрибут из XML
     */
    private inline fun <reified T : Enum<T>> TypedArray.getEnum(index: Int, default: T) =
        getInt(index, -1).let {
            if (it >= 0) enumValues<T>()[it] else default
        }

    /**
     * Атрибуты нужно читать пока работает конструктор кастомной View и все конструкторы
     * наследуемых классов. Потом атрибуты теряют значения.
     */

    init {
        context.obtainStyledAttributes(attrs, R.styleable.TimeSurfaceV4)
            .apply {
                _mode = getEnum(R.styleable.TimeSurfaceV4_mode, SeverityMode.Master)
            }.recycle()

        context.obtainStyledAttributes(attrs, R.styleable.TimeSurface)
            .apply {
                _severityMode = getEnum(R.styleable.TimeSurface_severity_mode, SeverityMode.Master)
            }.recycle()

        context.obtainStyledAttributes(attrs, R.styleable.TimeLine)
            .apply {
                _controlMode = getEnum(R.styleable.TimeLine_control_mode, SeverityMode.Master)
            }.recycle()
    }

    override val mode: SeverityMode
        get() = _mode

    override val severityMode: SeverityMode
        get() = _severityMode

    override val controlMode: SeverityMode
        get() = _controlMode
}