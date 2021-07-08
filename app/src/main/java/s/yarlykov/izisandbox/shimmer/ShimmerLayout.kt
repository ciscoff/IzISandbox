package s.yarlykov.izisandbox.shimmer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout

class ShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var canvasForShimmerMask: Canvas? = null
    private var maskAnimator: ValueAnimator? = null
    private var gradientTexturePaint: Paint? = null
    private var localMaskBitmap: Bitmap? = null

    private var maskRect: Rect? = null

    private var isAnimationStarted = false
    private var autoStart = false

    private var maskOffsetX = 0f

    private var maskBitmap: Bitmap? = null
        get() {
            if (field == null) {
                field = createBitmap(maskRect!!.width(), height)
            }
            return field
        }

    private var maskWidth: Float = 0f
        set(value) {

            if (value <= MIN_MASK_WIDTH_VALUE || value > MAX_MASK_WIDTH_VALUE) {
                throw IllegalArgumentException("maskWidth value must be higher than $MIN_MASK_WIDTH_VALUE and less or equal to $MAX_MASK_WIDTH_VALUE")
            }
            field = value
        }

    private var shimmerColor: Int = 0
        set(value) {
            field = value
            resetIfStarted()
        }

    private var shimmerAnimationDuration: Int = 0
        set(value) {
            field = value
            resetIfStarted()
        }

    private var isAnimationReversed: Boolean = false
        set(value) {
            field = value
            resetIfStarted()
        }

    private var shimmerAngle: Int = 0
        set(value) {
            if (value < MIN_ANGLE_VALUE || MAX_ANGLE_VALUE < value) {
                throw IllegalArgumentException("shimmerAngle value must be between $MIN_ANGLE_VALUE and $MAX_ANGLE_VALUE")
            }

            field = value
            resetIfStarted()
        }

    private var gradientCenterColorWidth: Float = 0f
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
            viewTreeObserver.removeOnPreDrawListener(this)
            startShimmerAnimation()
            return true
        }
    }

    init {
        // Разрешить рисование для ViewGroup
        setWillNotDraw(false)

        maskWidth = 0.5f
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

    override fun dispatchDraw(canvas: Canvas?) {
        if (isAnimationStarted.not() || width <= 0 || height <= 0) {
            super.dispatchDraw(canvas)
        } else {
            dispatchDrawShimmer(canvas)
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
        }
    }

    private fun stopShimmerAnimation() {
        startAnimationPreDrawListener?.let(viewTreeObserver::removeOnPreDrawListener)
        resetShimmering()
    }

    private fun dispatchDrawShimmer(canvas: Canvas) {
        super.dispatchDraw(canvas)

        localMaskBitmap = maskBitmap ?: return

        canvasForShimmerMask = (canvasForShimmerMask ?: Canvas(localMaskBitmap as Bitmap)).apply {
            drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            save()
            translate(-maskOffsetX, 0)
        }

        super.dispatchDraw(canvasForShimmerMask)
        canvasForShimmerMask?.restore()

        drawShimmer(canvas)
        localMaskBitmap = null
    }

    private fun drawShimmer(canvas: Canvas) {

    }

    private fun createShimmerPaint() {

        if(gradientTexturePaint != null) return



    }

    /**
     * Reset alpha-компонент цвета (сброс в 0 - полностью прозрачный)
     */
    private fun reduceColorAlphaValueToZero(actualColor : Int): Int {
        return Color.argb(0, Color.red(actualColor), Color.green(actualColor), Color.blue(actualColor))
    }

    private fun calculateBitmapMaskRect() : Rect {

    }

    private fun calculateMaskWidth() : Int {

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

    companion object {
        const val DEFAULT_ANIMATION_DURATION = 1500
        const val DEFAULT_ANGLE = 20
        const val MIN_ANGLE_VALUE = -45
        const val MAX_ANGLE_VALUE = -45
        const val MIN_MASK_WIDTH_VALUE = 0
        const val MAX_MASK_WIDTH_VALUE = 1
        const val MIN_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 0
        const val MAX_GRADIENT_CENTER_COLOR_WIDTH_VALUE = 1
    }

}