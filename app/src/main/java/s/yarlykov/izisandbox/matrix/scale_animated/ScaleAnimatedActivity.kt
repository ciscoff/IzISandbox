package s.yarlykov.izisandbox.matrix.scale_animated

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_matrix_v1.*
import s.yarlykov.izisandbox.R

class ScaleAnimatedActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    companion object {
        const val progressMin = 0
        const val progressMax = 100
    }

    private lateinit var sourceBitmap: Bitmap
    private val matrix = Matrix()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scale_animated)

        seekBar.setOnSeekBarChangeListener(this)
        seekBar.progress = progressMin

        pictureFrame.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            loadBitmap()
            renderSubSample(0)
        }
    }

    private fun loadBitmap() {

        sourceBitmap = BitmapScaleHelper.loadSampledBitmapFromResource(
            this,
            R.drawable.batumi,
            pictureFrame.width,
            pictureFrame.height
        )
    }

    /**
     * Из исходной битмапы вырезаем прямоугольник равный половине её ширины и высоты и скалируем
     * его на весь размер pictureFrame.
     */
    private fun renderSubSample(dX : Int) {

        val dY = sourceBitmap.height / 4
        val srcW = sourceBitmap.width / 2.0f
        val srcH = sourceBitmap.height / 2.0f
        val scaleX = pictureFrame.width / srcW
        val scaleY = pictureFrame.height / srcH

        matrix.apply {
            reset()
            setScale(scaleX, scaleY )
        }


        val b = Bitmap.createBitmap(sourceBitmap, dX, dY, srcW.toInt(), srcH.toInt(), matrix, false)
        pictureFrame.setImageBitmap(b)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val pathLength = sourceBitmap.width / 2
        val dX = pathLength * progress / progressMax
        renderSubSample(dX)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }
}