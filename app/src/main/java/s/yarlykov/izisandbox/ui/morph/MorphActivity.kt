package s.yarlykov.izisandbox.ui.morph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.IntegerRes
import androidx.core.content.ContextCompat
import s.yarlykov.izisandbox.R

class MorphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_morph)

        findViewById<MorphView>(R.id.view_morph_advanced).setOnClickListener {
            morphToCircle(it as MorphView)
        }
    }

    private fun morphToCircle(morphView: MorphView) {
        val circleParams = MorphView.Params.create()
            .cornerRadius(dimen(R.dimen.morph_corner_radius_56dp))
            .width(dimen(R.dimen.morph_dim_56dp).toInt())
            .height(dimen(R.dimen.morph_dim_56dp).toInt())
            .colorNormal(color(R.color.morph_blue))
            .colorPressed(color(R.color.morph_blue_dark))
            .strokeColor(color(R.color.morph_blue_dark))

        morphView.morph(circleParams)
    }

    private fun dimen(@DimenRes dimenId: Int): Float = resources.getDimension(dimenId)

    private fun color(@ColorRes colorId: Int): Int = ContextCompat.getColor(this, colorId)

    private fun integer(@IntegerRes intId: Int): Int = resources.getInteger(intId)
}