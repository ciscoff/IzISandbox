package s.yarlykov.izisandbox.transitions.using_scenes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.card.MaterialCardView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.dp

class ScenesInsideActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scenes_inside2)
    }

    override fun onResume() {
        super.onResume()

        val sceneRoot = findViewById<FrameLayout>(R.id.scene_root)
        val card = findViewById<MaterialCardView>(R.id.card)
        animateCard(sceneRoot, card)
    }

    private fun animateCard(sceneRoot : ViewGroup, card : View) {

        TransitionManager.beginDelayedTransition(sceneRoot)

        val params = card.layoutParams as ViewGroup.LayoutParams
        params.width = 300.dp
        card.layoutParams = params

    }
}