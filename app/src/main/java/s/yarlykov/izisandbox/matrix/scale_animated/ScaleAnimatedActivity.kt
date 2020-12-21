package s.yarlykov.izisandbox.matrix.scale_animated

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_matrix_v1.*
import s.yarlykov.izisandbox.R

class ScaleAnimatedActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit var sourceBitmap : Bitmap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scale_animated)

        seekBar.setOnSeekBarChangeListener(this)
        seekBar.progress = 100

        loadBitmap()

    }

    private fun loadBitmap() {

        sourceBitmap = BitmapFactory.decodeResource(resources, R.drawable.batumi)
        val bmpWidth = pictureFrame.drawable.intrinsicWidth
        val bmpHeight = pictureFrame.drawable.intrinsicHeight

    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {




        // Ползунок идет влево (стремится к 0)
//        if (progress < prevProgress && progress <= 50 && prevProgress > 50) {
//            pictureFrame.setImageDrawable(drawables[nextIndex])
//        }
//        // Ползунок идет вправо (стремится к 100)
//        else if (progress > prevProgress && progress >= 50 && prevProgress < 50) {
//            pictureFrame.setImageDrawable(drawables[nextIndex])
//        }
//
//        prevProgress = progress
//        scalingPicture(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }
}