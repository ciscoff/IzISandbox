package s.yarlykov.izisandbox.shimmer

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

class ShimmerLayoutV2 @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var canvasForShimmerMask: Canvas? = null
    private var maskAnimator: ValueAnimator? = null
    private var gradientTexturePaint: Paint? = null
    private var localMaskBitmap: Bitmap? = null

    private var isAnimationStarted = false
    private var autoStart = true

    private var maskOffsetX = 0f

    private val maskRect: Rect
        get() = calculateBitmapMaskRect()

    private var maskBitmap: Bitmap? = null
        get() {
            if (field == null) {
                field = createBitmap(maskRect.width(), height)
            }
            return field
        }

    private var maskWidthRatio: Float = 0.5f
        set(value) {

            if (value <= MIN_MASK_WIDTH_VALUE || value > MAX_MASK_WIDTH_VALUE) {
                throw IllegalArgumentException("maskWidth value must be higher than $MIN_MASK_WIDTH_VALUE and less or equal to $MAX_MASK_WIDTH_VALUE")
            }
            field = value
        }

    private val maskWidth: Float
        get() = (width / 2) * maskWidthRatio

    private val shimmerColor: Int
        get() = ContextCompat.getColor(context, R.color.default_shimmer_color)

    private var shimmerAnimationDuration: Long = DEFAULT_ANIMATION_DURATION
        set(value) {
            field = value
            resetIfStarted()
        }

    private var isAnimationReversed: Boolean = false
        set(value) {
            field = value
            resetIfStarted()
        }

    private var shimmerAngle: Int = DEFAULT_ANGLE
        set(value) {
            if (value < MIN_ANGLE_VALUE || MAX_ANGLE_VALUE < value) {
                throw IllegalArgumentException("shimmerAngle value must be between $MIN_ANGLE_VALUE and $MAX_ANGLE_VALUE")
            }

            field = value
            resetIfStarted()
        }

    private var gradientCenterColorWidth: Float = 0.1f
        set(value) {
            if (value <= MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE ||
                MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE < value
            ) {
                throw IllegalArgumentException("gradientCenterColorWidth value must be higher than $MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE and less than $MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE")
            }
            field = value
            resetIfStarted()
        }

    private var startAnimationPreDrawListener: ViewTreeObserver.OnPreDrawListener? = null

    private val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            logIt("${object {}.javaClass.enclosingMethod?.name}")
            viewTreeObserver.removeOnPreDrawListener(this)
            startShimmerAnimation()
            return true
        }
    }

    init {
        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)

        maskWidthRatio = 0.5f
        gradientCenterColorWidth = 0.1f
        shimmerAngle = 0

        if (autoStart && visibility == View.VISIBLE) {
            startShimmerAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        resetShimmering()
        super.onDetachedFromWindow()
    }

    /**
     * Вызывается из view.draw, чтобы отрисовать дочерние элементы ПОСЛЕ того как сама
     * эта ViewGroup была отрисована.
     */
    override fun dispatchDraw(canvas: Canvas) {
        logIt("${object {}.javaClass.enclosingMethod?.name}")
        if (isAnimationStarted.not() || width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
        } else {
            dispatchDrawShimmer(canvas)
        }
    }

    private fun dispatchDrawShimmer(canvas: Canvas) {
        logIt("${object {}.javaClass.enclosingMethod?.name}")
        super.dispatchDraw(canvas)

        localMaskBitmap = maskBitmap ?: return

        canvasForShimmerMask = (canvasForShimmerMask ?: Canvas(localMaskBitmap as Bitmap)).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            save()
            translate(-maskOffsetX, 0f)
        }

        // TODO ??? Передаем детям другую Canvas
        super.dispatchDraw(canvasForShimmerMask)
        canvasForShimmerMask?.restore()

        // TODO Shimmer рисуем на оригинальной Canvas
        drawShimmer(canvas)
        localMaskBitmap = null
    }

    private fun drawShimmer(destinationCanvas: Canvas) {
        logIt("${object {}.javaClass.enclosingMethod?.name}")

        createShimmerPaint()

        gradientTexturePaint?.let { paint ->

            destinationCanvas.save()
            destinationCanvas.apply {

                translate(maskOffsetX, 0f)
                drawRect(
                    maskRect.left.toFloat(),
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
        logIt("${object {}.javaClass.enclosingMethod?.name}")
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
            maskOffsetX = animationFromX + animatedValue

            if (maskOffsetX + shimmerBitmapWidth >= 0) {
                invalidate()
            }
        }

        maskAnimator = if (isAnimationReversed) {
            ValueAnimator.ofInt(shimmerAnimationFullLength, 0)
        } else {
            ValueAnimator.ofInt(0, shimmerAnimationFullLength)
        }.apply {
            duration = shimmerAnimationDuration
            repeatCount = ObjectAnimator.INFINITE
            addUpdateListener(animatorUpdateListener)
        }

        return maskAnimator
    }

    private fun createShimmerPaint() {

        if (gradientTexturePaint != null) return

        val edgeColor = reduceColorAlphaValueToZero(shimmerColor)
        val yPosition = 0/*if (0 <= shimmerAngle) height else 0*/

//        val x0 = 0
//        val y0 = yPosition

        logIt("${object {}.javaClass.enclosingMethod?.name}")

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
            BitmapShader(localMaskBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val composeShader = ComposeShader(gradient, maskBitmapShader, PorterDuff.Mode.DST_IN)

        gradientTexturePaint = Paint().apply {
            isAntiAlias = true
            isDither = true
            isFilterBitmap = true
            shader = composeShader
        }
    }

    private fun getGradientColorDistribution(): FloatArray {
        return floatArrayOf(
            0f,
            0.5f - gradientCenterColorWidth / 2f,
            0.5f + gradientCenterColorWidth / 2f,
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

    private fun calculateBitmapMaskRect(): Rect {
        return Rect(0, 0, calculateMaskWidth(), height)
    }

    /**
     * Учитывается поворот на угол shimmerAngle относительно точки (0, 0), то есть точки начала
     * системы координат этой view.
     */
    private fun calculateMaskWidth(): Int {
        // maskWidth работает как КАТЕТ и мы "проецируем" её на горизонталь
        val shimmerWidthPlane = maskWidth / cos(Math.toRadians(abs(shimmerAngle).toDouble()))
        // "повернутая" view.height работает как гипотенуза и мы её тоже проецируем на горизонталь
        val shimmerHeightPlane = height * tan(Math.toRadians(abs(shimmerAngle).toDouble()))

        return (shimmerWidthPlane + shimmerHeightPlane).toInt()
    }

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
            startShimmerAnimation();
        }
    }

    private fun resetShimmering() {

        maskAnimator?.apply {
            end()
            removeAllUpdateListeners()
        }

        maskAnimator = null
        gradientTexturePaint = null
        isAnimationStarted = false
        releaseBitMaps()
    }

    private fun releaseBitMaps() {
        canvasForShimmerMask = null
        maskBitmap?.recycle()
        maskBitmap = null
    }

    // TODO В оригинале использвается при чтении кастомныз атрибутов
    private fun getColor(@ColorRes id: Int): Int {
        return context.getColor(id)
    }

    companion object {
        const val DEFAULT_ANIMATION_DURATION = 2500L
        const val DEFAULT_ANGLE = 30
        const val MIN_ANGLE_VALUE = -45
        const val MAX_ANGLE_VALUE = 45
        const val MIN_MASK_WIDTH_VALUE = 0
        const val MAX_MASK_WIDTH_VALUE = 1
        const val MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 0
        const val MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 1
    }

}