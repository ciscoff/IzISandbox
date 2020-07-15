package s.yarlykov.izisandbox.transitions.shared_with_activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.transitions.shared_with_activities.ActivitySharedTo.Companion.KEY_BACKGROUND

class ActivitySharedFrom : AppCompatActivity() {

    private val child = listOf(
        R.id.view_colored_blue,
        R.id.view_colored_green,
        R.id.view_colored_red,
        R.id.view_colored_orange
    )

    private val colors = listOf(
        R.drawable.shape_oval_blue,
        R.drawable.shape_oval_green,
        R.drawable.shape_oval_red,
        R.drawable.shape_oval_orange
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_from)

        val transitionName = getString(R.string.shared_transition_colored)

        for(i in child.indices) {

            findViewById<View>(child[i]).apply {
                isClickable = true
                isFocusable = true
                setOnClickListener(
                    clickListenerFabric(
                        transitionName,
                        colors[i]
                    )
                )
            }
        }
    }

    private fun clickListenerFabric(transitionName: String, backgroundColorId: Int): (View) -> Unit {

        val activity = this@ActivitySharedFrom

        return { sharedView ->

            val intent = Intent(
                activity,
                ActivitySharedTo::class.java
            ).apply {
                putExtra(KEY_BACKGROUND, backgroundColorId)
            }

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity,
                sharedView,
                transitionName
            )
            startActivity(intent, options.toBundle())
        }
    }

    /**
     * Базовый способ переключения активити
     */
    private val onClickListener: (View) -> Unit = { sharedView ->

        val intent = Intent(this@ActivitySharedFrom, ActivitySharedTo::class.java)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this@ActivitySharedFrom,
            sharedView,
            getString(R.string.shared_transition_colored)
        )
        startActivity(intent, options.toBundle())
    }
}