package s.yarlykov.izisandbox.recycler_and_swipes.layout_animation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recycler_view_layout_animation.*
import s.yarlykov.izisandbox.R

class RecyclerViewLayoutAnimationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_layout_animation)
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_layout_animation, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_linear -> animateLinear()
            R.id.menu_grid -> animateGrid()
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun animateLinear(): Boolean {

        val animationController =
            AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)

        rvAnimated.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@RecyclerViewLayoutAnimationActivity)
            layoutAnimation = animationController
            itemAnimator = DefaultItemAnimator()

            adapter = AdapterLinear()
            scheduleLayoutAnimation()
        }

        return true
    }

    private fun animateGrid(): Boolean {
        // TODO

        return false
    }
}