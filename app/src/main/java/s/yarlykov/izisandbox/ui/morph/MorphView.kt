package s.yarlykov.izisandbox.ui.morph

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.util.AttributeSet
import android.util.StateSet
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R

class MorphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var viewPadding: ViewPadding
    private var cornerRadius: Float = 0f
    private var strokeWidth: Int = 0
    private var strokeColor: Int = 0
    private var viewHeight: Int = 0
    private var viewWidth: Int = 0
    private var viewColor: Int = 0

    private var isAnimationInProgress = false

    private lateinit var normalStateDrawable: StrokeGradientDrawable
    private lateinit var pressedStateDrawable: StrokeGradientDrawable

    init {
        initView()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (viewWidth == 0 && viewHeight == 0 && w != 0 && h != 0) {
            viewWidth = w
            viewHeight = h
        }
    }

    /**
     * Метод изменяет нормальный вид элемента согласно значениям в параметре params
     */
    fun morph(params: Params) {
        if (isAnimationInProgress) {
            return
        }

        // Модифицируем GradientDrawable, который работает фоном в состоянии pressed
        pressedStateDrawable.apply {
            color = params.colorPressed
            cornerRadius = params.cornerRadius
            strokeColor = params.strokeColor
            strokeWidth = params.strokeWidth
        }

        viewColor = params.colorPressed
        cornerRadius = params.cornerRadius
        strokeColor = params.strokeColor
        strokeWidth = params.strokeWidth

        morphWithoutAnimation(params)
    }

    private fun morphWithoutAnimation(params: Params) {

        // Модифицируем GradientDrawable, который работает фоном в состоянии normal
        normalStateDrawable.apply {
            color = params.colorPressed
            cornerRadius = params.cornerRadius
            strokeColor = params.strokeColor
            strokeWidth = params.strokeWidth
        }

        if (params.width != 0 && params.height != 0) {

            layoutParams = layoutParams.apply {
                width = params.width
                height = params.height
            }
        }
    }

    /**
     * View инициализирует свой внешний вид, а именно:
     * - читает из ресурсов два цвета для состояний normal/pressed
     * - читает из ресурсов радиус углов
     * - создает два Drawable для своего фона в состояниях normal/pressed и назначает им цветаа
     *   и радиус углов (см выше)
     * - создает StateListDrawable и делает его своим background'ом
     *
     * После этого при клике на View мы получим смену цветов: синий-темносиний-синий.
     * Система сама отслеживает смену состояний normal/pressed и рисует нужный Drawable.
     */
    private fun initView() {
        viewPadding = ViewPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

        val radius = resources.getDimension(R.dimen.morph_corner_radius_2dp)
        val colorBlue = ContextCompat.getColor(context, R.color.morph_blue)
        val colorBlueDark = ContextCompat.getColor(context, R.color.morph_blue_dark)

        normalStateDrawable = createDrawable(colorBlue, radius, 0)
        pressedStateDrawable = createDrawable(colorBlueDark, radius, 0)

        viewColor = colorBlue
        strokeColor = colorBlue
        cornerRadius = radius

        val background = StateListDrawable().apply {
            addState(
                IntArray(1) { android.R.attr.state_pressed },
                pressedStateDrawable.gradientDrawable
            )
            addState(StateSet.WILD_CARD, normalStateDrawable.gradientDrawable)
        }

        setBackground(background)
    }

    private fun createDrawable(
        _color: Int,
        _cornerRadius: Float,
        _strokeWidth: Int = 0
    ): StrokeGradientDrawable = StrokeGradientDrawable(GradientDrawable()).apply {
        gradientDrawable.shape = GradientDrawable.RECTANGLE
        color = _color
        cornerRadius = _cornerRadius
        strokeColor = _color
        strokeWidth = _strokeWidth
    }

    class Params {
        internal var cornerRadius: Float = 0f
        internal var colorPressed: Int = 0
        internal var colorNormal: Int = 0
        internal var duration: Int = 0
        internal var iconId: Int = 0
        internal var width: Int = 0
        internal var height: Int = 0
        internal var strokeWidth: Int = 0
        internal var strokeColor: Int = 0
        internal var text: String? = null

        fun text(text: String): Params {
            this.text = text
            return this
        }

        fun icon(@DrawableRes iconId: Int): Params {
            this.iconId = iconId
            return this
        }

        fun cornerRadius(cornerRadius: Float): Params {
            this.cornerRadius = cornerRadius
            return this
        }

        fun width(width: Int): Params {
            this.width = width
            return this
        }

        fun height(height: Int): Params {
            this.height = height
            return this
        }

        fun colorNormal(color: Int): Params {
            this.colorNormal = color
            return this
        }

        fun colorPressed(colorPressed: Int): Params {
            this.colorPressed = colorPressed
            return this
        }

        fun duration(duration: Int): Params {
            this.duration = duration
            return this
        }

        fun strokeWidth(strokeWidth: Int): Params {
            this.strokeWidth = strokeWidth
            return this
        }

        fun strokeColor(strokeColor: Int): Params {
            this.strokeColor = strokeColor
            return this
        }

        companion object {
            fun create() = Params()
        }
    }

}