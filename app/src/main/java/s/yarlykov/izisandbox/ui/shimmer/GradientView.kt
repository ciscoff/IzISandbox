package s.yarlykov.izisandbox.ui.shimmer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class GradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val gradientWidthRatio = 0.1f

    private val shimmerColor = Color.BLUE
    private val edgeColor = reduceColorAlphaValueToZero(shimmerColor)


    private val linearGradientShader: Shader by lazy {

        val maskWidth = 300
        val angle = 30
        val xPosition = 0f
        val yPosition = height

        val matrix = Matrix()
        matrix.postScale(1.2f, 1.2f)
        matrix.preTranslate(300f, 0f)

        LinearGradient(
            xPosition,
            yPosition.toFloat(),
            xPosition + cos(Math.toRadians(angle.toDouble())).toFloat() * maskWidth,
            yPosition + sin(Math.toRadians(angle.toDouble())).toFloat() * maskWidth,
            intArrayOf(edgeColor, shimmerColor, shimmerColor, edgeColor),
            getGradientColorDistribution(),
            Shader.TileMode.CLAMP
        ).apply {
            setLocalMatrix(matrix)
        }
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isDither = true
        isFilterBitmap = true
        shader = linearGradientShader
    }

    override fun onDraw(canvas: Canvas) {

        if (width == 0 || height == 0) {
            super.onDraw(canvas)
        } else {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
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

    private fun reduceColorAlphaValueToZero(actualColor: Int): Int {
        return Color.argb(
            0,
            Color.red(actualColor),
            Color.green(actualColor),
            Color.blue(actualColor)
        )
    }
}