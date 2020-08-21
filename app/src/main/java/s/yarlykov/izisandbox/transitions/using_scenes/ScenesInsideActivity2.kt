package s.yarlykov.izisandbox.transitions.using_scenes

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.dp
import s.yarlykov.izisandbox.extensions.px

class ScenesInsideActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scenes_inside2)
    }

    override fun onResume() {
        super.onResume()

        val sceneRoot = findViewById<LinearLayout>(R.id.scene_root)
        val card = findViewById<MaterialCardView>(R.id.card)

        /**
         * Анимацию запускаем с задержкой, чтобы весь макет успел полностью
         * выполнить начальный layout после показа активити. Задержка в 10 мс
         * маловата. 100 - как раз.
         */
        sceneRoot.postDelayed({
            animateCard(sceneRoot, card)
        }, 2000)
    }

    /**
     * Карточка анимированно "растет" от фиксированного положения своего top/left вправо и вниз.
     */
    private fun animateCard(sceneRoot : ViewGroup, card : View) {
        TransitionManager.beginDelayedTransition(sceneRoot)

        val params = card.layoutParams as ViewGroup.LayoutParams
        params.width = 200.px
        params.height = 56.px
        card.layoutParams = params
    }
}