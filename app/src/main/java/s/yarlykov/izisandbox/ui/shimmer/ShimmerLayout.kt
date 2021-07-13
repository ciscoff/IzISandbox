package s.yarlykov.izisandbox.ui.shimmer

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan


/**
 * Agenda:
 * -- Mask - эффект Shimmer, который мы "накладываем" поверх уже отрисованных дочерних Views.
 * -- MaskRect - область, которая определяет размеры маски, размеры рисования эффекта.
 * -- MaskBitmap - битмапа размером MaskRect. Собственно это пиксели с отрисовкой эффекта шиммера.
 * -- MaskCanvas - канва для рисования по MaskBitmap.
 * -- MaskPaint - кисть с шейдером для рисования с MaskCanvas.
 */

class ShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var maskAnimator: ValueAnimator? = null
    private var maskCanvas: Canvas? = null
    private var maskPaint: Paint? = null

    private var isAnimationStarted = false
    private var autoStart = true

    private var maskLeftX = 0f

    private var localMaskBitmap: Bitmap? = null
    private var maskBitmap: Bitmap? = null
        get() {
            if (field == null) {
                field = createBitmap(maskRect.width(), height)
            }
            return field
        }

    private val maskRect: Rect
        get() = Rect(0, 0, calculateMaskPlaneWidth(), height)

    private val maskWidth: Float
        get() = (width / 2) * maskWidthRatio

    private var shimmerColor: Int = Color.TRANSPARENT

    private var animationDuration: Long = DEFAULT_ANIMATION_DURATION.toLong()
        set(value) {
            field = value
            resetIfStarted()
        }

    private var isAnimationReversed: Boolean = false
        set(value) {
            field = value
            resetIfStarted()
        }

    private var maskWidthRatio: Float = DEFAULT_MASK_WIDTH_RATIO
        set(value) {
            if (value <= MIN_MASK_WIDTH_VALUE || value > MAX_MASK_WIDTH_VALUE) {
                throw IllegalArgumentException(
                    String.format(
                        "maskWidth value must be higher than %d and less or equal to %d",
                        MIN_MASK_WIDTH_VALUE,
                        MAX_MASK_WIDTH_VALUE
                    )
                )
            }
            field = value
        }

    private var shimmerAngle: Int = DEFAULT_ANGLE
        set(value) {
            if (value < MIN_ANGLE_VALUE || MAX_ANGLE_VALUE < value) {
                throw IllegalArgumentException(
                    String.format(
                        "shimmerAngle value must be between %d and %d",
                        MIN_ANGLE_VALUE,
                        MAX_ANGLE_VALUE
                    )
                )
            }

            field = value
            resetIfStarted()
        }

    private var gradientWidthRatio: Float = DEFAULT_GRADIENT_WIDTH_RATIO
        set(value) {
            if (value <= MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE ||
                MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE < value
            ) {
                throw IllegalArgumentException(
                    String.format(
                        "gradientCenterColorWidth value must be higher than %d and less than %d",
                        MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE,
                        MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE
                    )
                )
            }
            field = value
            resetIfStarted()
        }

    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            viewTreeObserver.removeOnPreDrawListener(this)
            startShimmerAnimation()
            return true
        }
    }

    init {
        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)

        with(context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout)) {
            maskWidthRatio =
                getFloat(
                    R.styleable.ShimmerLayout_shimmer_width_ratio,
                    DEFAULT_MASK_WIDTH_RATIO
                )
            gradientWidthRatio =
                getFloat(
                    R.styleable.ShimmerLayout_shimmer_gradient_width_ratio,
                    DEFAULT_GRADIENT_WIDTH_RATIO
                )

            shimmerAngle = getInt(
                R.styleable.ShimmerLayout_shimmer_angle,
                DEFAULT_ANGLE
            )

            animationDuration = getInt(
                R.styleable.ShimmerLayout_shimmer_duration,
                DEFAULT_ANIMATION_DURATION
            ).toLong()

            isAnimationReversed =
                getBoolean(R.styleable.ShimmerLayout_shimmer_reverse_animation, false)

            shimmerColor = getColor(
                R.styleable.ShimmerLayout_shimmer_color,
                getColor(R.color.shimmer_color_default)
            )

            recycle()
        }

        // Старт
        if (autoStart && visibility == View.VISIBLE) {
            startShimmerAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        resetShimmering()
        super.onDetachedFromWindow()
    }

    /**
     * Вызов из view.draw, чтобы отрисовать детей уже ПОСЛЕ того как сама эта ViewGroup
     * была отрисована. Если анимации нет, то просто рисуем детей. Если анимация идет,
     * то поверх детей рисуем эффект шимера.
     */
    override fun dispatchDraw(canvas: Canvas) {

        if (isAnimationStarted.not() || width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
        } else {
            dispatchDrawShimmer(canvas)
        }
    }

    /**
     * Пояснения по поводу всех этих манипуляций смотри в
     * [s.yarlykov.izisandbox.ui.shimmer.arch.ShimmerLayoutV2.dispatchDrawShimmer]
     */
    private fun dispatchDrawShimmer(canvas: Canvas) {
        // Сначала отрисовать детей
        super.dispatchDraw(canvas)

        localMaskBitmap = maskBitmap ?: return

        maskCanvas = (maskCanvas ?: Canvas(maskBitmap/*localMaskBitmap*/ as Bitmap)).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            save()
            translate(-maskLeftX, 0f)
        }
        logIt("maskOffsetX=$maskLeftX")

        // Теперь на той части битмапы шиммера, которая пересекается с битмапой view,
        // нарисовать детей. По факту мы сформировали битмапу для BimapShader и будем использовать
        // этот шейдер в drawShimmer
        super.dispatchDraw(maskCanvas)
        maskCanvas?.restore()

        drawShimmer(canvas)
        localMaskBitmap = null
    }

    /**
     * Рисование по "системной" Canvas. Это отрисовка ПОВЕРХ УЖЕ отрисованных детей.
     */
    private fun drawShimmer(canvas: Canvas) {
        logIt("${object {}.javaClass.enclosingMethod?.name}")

        createShimmerPaint()

        maskPaint?.let { paint ->

            canvas.save()
            canvas.apply {

                /*TODO translate(maskLeftX, 0f)*/
                translate(maskLeftX, 0f)
                drawRect(
                    0f/*maskRect.left.toFloat()*/,
                    0f,
                    maskRect.width().toFloat(),
                    maskRect.height().toFloat(),
                    paint
                )
                restore()
            }
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            if (autoStart) {
                startShimmerAnimation()
            }
        } else {
            stopShimmerAnimation()
        }
    }

    private fun startShimmerAnimation() {
        if (isAnimationStarted) return

        if (width == 0) {
            startAnimationPreDrawListener = preDrawListener
            viewTreeObserver.addOnPreDrawListener(startAnimationPreDrawListener)
            return
        }

        getShimmerAnimation()?.start()
        isAnimationStarted = true
    }

    private fun stopShimmerAnimation() {
        startAnimationPreDrawListener?.let(viewTreeObserver::removeOnPreDrawListener)
        resetShimmering()
    }

    private fun getShimmerAnimation(): Animator? {
        if (maskAnimator != null) return maskAnimator

        val shimmerBitmapWidth = maskRect.width()

        val animationToX = width

        val animationFromX = if (width > maskRect.width()) {
            -animationToX
        } else {
            -maskRect.width()
        }

        val shimmerAnimationFullLength = animationToX - animationFromX

        val animatorUpdateListener = ValueAnimator.AnimatorUpdateListener { animator ->
            val animatedValue = try {
                (animator.animatedValue as Int).toFloat()
            } catch (e: Exception) {
                0f
            }
            maskLeftX = animationFromX + animatedValue

            if (maskLeftX + shimmerBitmapWidth >= 0) {
                invalidate()
            }
        }

        maskAnimator = if (isAnimationReversed) {
            ValueAnimator.ofInt(shimmerAnimationFullLength, 0)
        } else {
            ValueAnimator.ofInt(0, shimmerAnimationFullLength)
        }.apply {
            duration = animationDuration
            repeatCount = ObjectAnimator.INFINITE
            addUpdateListener(animatorUpdateListener)
        }

        return maskAnimator
    }

    /**
     * По поводу всех манипуляций смотри комменты в
     * [s.yarlykov.izisandbox.ui.shimmer.arch.ShimmerLayoutV2.createShimmerPaint]
     *
     * TODO нужно разобраться почему градиент не работает правильно если в коде ниже
     * TODO явно указать ' val yPosition = 0f'. По факту получаем ту же линию градиента,
     * TODO но ниже на высоту View.
     *
     * Log: shimmerAngle=30, shimmerAngle.rad=0.5235987755982988,
     * Log: linear gradient ok RectF(0.0, 210.0, 146.7913, 294.75), wrong RectF(0.0, 0.0, 146.7913, 84.75)
     */
    private fun createShimmerPaint() {

        if (maskPaint != null) return

        val edgeColor = reduceColorAlphaValueToZero(shimmerColor)

        val yPosition = if (0 <= shimmerAngle) height else 0

        val gradient = LinearGradient(
            0f,
            yPosition.toFloat(),
            cos(Math.toRadians(shimmerAngle.toDouble())).toFloat() * maskWidth,
            yPosition + sin(Math.toRadians(shimmerAngle.toDouble())).toFloat() * maskWidth,
            intArrayOf(edgeColor, shimmerColor, shimmerColor, edgeColor),
            getGradientColorDistribution(),
            Shader.TileMode.CLAMP
        )

        val maskBitmapShader =
            BitmapShader(maskBitmap!!/*localMaskBitmap!!*/, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val composeShader = ComposeShader(gradient, maskBitmapShader, PorterDuff.Mode.DST_IN)

        maskPaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
            shader = composeShader
        }
    }

    private fun getGradientColorDistribution(): FloatArray {
        return floatArrayOf(
            0f,
            0.5f - gradientWidthRatio / 2f,
            0.5f + gradientWidthRatio / 2f,
            1f
        )
    }

    /**
     * Reset alpha-компонент цвета (сброс в 0 - полностью прозрачный)
     */
    private fun reduceColorAlphaValueToZero(actualColor: Int): Int {
        return Color.argb(
            0,
            Color.red(actualColor),
            Color.green(actualColor),
            Color.blue(actualColor)
        )
    }

    /**
     * Вычисляем полную "проекцию" маски на горизонтальную плоскость. Здесь учитывается поворот
     * на угол shimmerAngle относительно точки (0, 0), то есть точки начала системы координат
     * этой view.
     */
    private fun calculateMaskPlaneWidth(): Int {
        // maskWidth работает как КАТЕТ и мы "проецируем" её на горизонталь.
        val maskWidthPlane = maskWidth / cos(Math.toRadians(abs(shimmerAngle).toDouble()))
        // "повернутая" maskHeight (она же view.height) работает как КАТЕТ и мы её тоже
        // "проецируем" на горизонталь.
        val maskHeightPlane = height * tan(Math.toRadians(abs(shimmerAngle).toDouble()))

        logIt("gradient, maskWidth=$maskWidth, maskWidthPlane=$maskWidthPlane, maskHeightPlane=$maskHeightPlane")

        return (maskWidthPlane + maskHeightPlane).toInt()
    }

    /**
     * При нехватке памяти ничего не создается.
     */
    private fun createBitmap(width: Int, height: Int): Bitmap? {
        return try {
            Bitmap.createBitmap(width, height, Bitmap.Config.ALPHA_8)
        } catch (e: OutOfMemoryError) {
            System.gc()
            null
        }
    }

    private fun resetIfStarted() {
        if (isAnimationStarted) {
            resetShimmering()
            startShimmerAnimation()
        }
    }

    private fun resetShimmering() {

        maskAnimator?.apply {
            end()
            removeAllUpdateListeners()
        }

        maskAnimator = null
        maskPaint = null
        isAnimationStarted = false
        releaseBitMaps()
    }

    private fun releaseBitMaps() {
        maskCanvas = null
        maskBitmap?.recycle()
        maskBitmap = null
    }

    private fun getColor(@ColorRes id: Int): Int {
        return ContextCompat.getColor(context, id)
    }

    companion object {
        const val DEFAULT_GRADIENT_WIDTH_RATIO = 0.1f
        const val DEFAULT_MASK_WIDTH_RATIO = 0.1f
        const val DEFAULT_ANIMATION_DURATION = 1500
        const val DEFAULT_ANGLE = 30
        const val MIN_ANGLE_VALUE = -45
        const val MAX_ANGLE_VALUE = 45
        const val MIN_MASK_WIDTH_VALUE = 0
        const val MAX_MASK_WIDTH_VALUE = 1
        const val MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 0
        const val MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 1
    }

}