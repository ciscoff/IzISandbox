package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet

class AvatarBackViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AvatarBaseViewV3(context, attrs, defStyleAttr) {

    /**
     * Paint для заливки фона исходной Bitmap'ой с применением цветового фильтра.
     */
    private var paintBackground: Paint? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        invalidate()
    }

    /**
     * @rectBitmapVisible - задает видимую часть битмапы в координатах битмапы.
     * @rectDest - целевой прямоугольник в координатах канвы.
     * То есть берем некую часть из битмапы и переносим в указанную область канвы.
     */
    override fun onDraw(canvas: Canvas) {
        sourceImageBitmap?.let {
            canvas.drawBitmap(it, rectBitmapVisible, rectDest, paintBackground)
        }
    }

    override fun onScaleChanged(scale: Float, pivot: PointF) {
        if (scale > 1f) return

        scaleAnimated(scale, pivot)

//        sourceImageBitmap?.let { bitmap ->
//            rectBitmapVisible.right = (bitmap.width * scale).toInt()
//            rectBitmapVisible.bottom = (bitmap.height * scale).toInt()
//            invalidate()
//        }
    }

    /**
     *
     * NOTE: Надо дебажить. По моему при увеличении битмапы больше её собственного
     * разрешения начинает лагать отрисовка. До превышения этого значения все ОК, ниже линии.
     *
     * @param pivot - точка в координатах канвы. Её нужно смапить на координаты битмапы.
     */
    private fun scaleAnimated(scaleFactor: Float, pivot: PointF) {

        sourceImageBitmap?.let { bitmap ->

            val pivotBitmap = Point(pivot.x.toInt(), pivot.y.toInt()).apply {
                offset(rectBitmapVisible.left, rectBitmapVisible.top)
            }

            val pivotRatioX = (pivotBitmap.x.toFloat() - rectBitmapVisible.left) / rectBitmapVisible.width()
            val pivotRatioY = (pivotBitmap.y.toFloat() - rectBitmapVisible.top) / rectBitmapVisible.height()

            val startW = rectBitmapVisible.width()
            val startH = rectBitmapVisible.height()

            ValueAnimator.ofFloat(1f, scaleFactor).apply {
                duration = animDuration

                addUpdateListener { animator ->
                    val scale = animator.animatedValue as Float

                    val w = (startW * scale).toInt()
                    val h = (startH * scale).toInt()

                    rectBitmapVisible.apply {
                        left = (pivotBitmap.x - w * pivotRatioX).toInt()
                        top = (pivotBitmap.y - h * pivotRatioY).toInt()
                        right = left + w
                        bottom = top + h
                    }
                    invalidate()
                }
            }.start()
        }
    }
}