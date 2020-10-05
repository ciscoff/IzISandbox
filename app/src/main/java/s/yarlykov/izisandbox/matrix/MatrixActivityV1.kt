package s.yarlykov.izisandbox.matrix

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_matrix_v1.*
import s.yarlykov.izisandbox.R
import kotlin.math.sign

class MatrixActivityV1 : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private lateinit var drawables: List<Drawable>
    private var prevProgress = 100
    private var i = 0

    // Управление индексом массива картинок
    private val nextIndex : Int
        get() {
            if(i >= drawables.lastIndex) {
                i = 0
            } else {
                i++
            }

            return i
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matrix_v1)

        seekBar.setOnSeekBarChangeListener(this)

        drawables = listOf(
            ContextCompat.getDrawable(this, R.drawable.masha)!!,
            ContextCompat.getDrawable(this, R.drawable.anna)!!,
            ContextCompat.getDrawable(this, R.drawable.gosha)!!
        )

        pictureFrame.apply {
            setImageDrawable(drawables[0])
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                scalingPicture(100)
            }
        }
    }

    private fun scalingPicture(scaleValue: Int) {

        val drawable = pictureFrame.drawable ?: return

        val viewWidth: Float = getImageViewWidth(pictureFrame).toFloat()
        val viewHeight: Float = getImageViewHeight(pictureFrame).toFloat()
        val drawableWidth = drawable.intrinsicWidth
        val drawableHeight = drawable.intrinsicHeight

        val matrix = Matrix()
        matrix.setTranslate(viewWidth / 2f, (viewHeight - drawableHeight) / 2f)
        matrix.preScale((scaleValue - 50) / 50f, 1f)
        val skewValue = (0.3f - oneZeroOne(scaleValue.toFloat()) * 0.3f)
        matrix.preSkew(0f, skewValue.toFloat())

        pictureFrame.imageMatrix = matrix
    }

    /**
     * Когда progress = 50, то делаем замену drawable в ImageView
     */

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

        // Ползунок идет влево (стремится к 0)
        if (progress < prevProgress && progress <= 50 && prevProgress > 50) {
            pictureFrame.setImageDrawable(drawables[nextIndex])
        }
        // Ползунок идет вправо (стремится к 100)
        else if (progress > prevProgress && progress >= 50 && prevProgress < 50) {
            pictureFrame.setImageDrawable(drawables[nextIndex])
        }

        prevProgress = progress
        scalingPicture(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    private fun getImageViewWidth(imageView: ImageView): Int {
        return imageView.width - imageView.paddingLeft - imageView.paddingRight
    }

    private fun getImageViewHeight(imageView: ImageView): Int {
        return imageView.height - imageView.paddingTop - imageView.paddingBottom
    }

    /**
     * @progress меняется от 100 до 0
     * @return меняется от 1 (progress = 100), до 0 (progress = 50), до 1 (progress = 0)
     */
    private fun oneZeroOne(progress: Float): Float {
        val half = 50f
        val s = progress - half
        return ((progress - half) * sign(s) / half)
    }

    /**
     * @progress меняется от 100 до 0
     * @return меняется от 0 (progress = 100), до 1 (progress = 50), до 0 (progress = 0)
     */
    private fun zeroOneZero(progress: Float): Float {
        val half = 50f
        val full = if (progress >= half) 100f else 0f
        val s = progress - full
        return ((progress - full) * sign(s) / half)
    }
}