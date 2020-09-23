package s.yarlykov.izisandbox.recycler_and_swipes.layout_animation

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_recycler_view_layout_animation.*
import s.yarlykov.izisandbox.R

class RecyclerViewLayoutAnimationActivity : AppCompatActivity() {

    lateinit var itemDecorator : ItemOffsetDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_layout_animation)
        setSupportActionBar(toolbar)

        rvLinear.visibility = View.INVISIBLE
        rvGrid.visibility = View.INVISIBLE

        itemDecorator = ItemOffsetDecoration(this)
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

        rvLinear.apply {

            // Нужно удалять, иначе при каждой следующей анимации суммируются отступы
            // пердыдущего декоратора.
            removeItemDecoration(itemDecorator)

            rvGrid.visibility = View.INVISIBLE
            visibility = View.VISIBLE

            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@RecyclerViewLayoutAnimationActivity)
            layoutAnimation = animationController
            itemAnimator = DefaultItemAnimator()
            addItemDecoration(itemDecorator)
            adapter = AdapterLinear()
            scheduleLayoutAnimation()
        }

        return true
    }

    private fun animateGrid(): Boolean {

        val columns = 4
        val rows = 16

        val animationController =
            AnimationUtils.loadLayoutAnimation(this, R.anim.grid_layout_animation_from_bottom)

        rvGrid.apply {

            rvLinear.visibility = View.INVISIBLE
            visibility = View.VISIBLE

            // Нужно удалять, иначе при каждой следующей анимации суммируются отступы
            // пердыдущего декоратора.
            removeItemDecoration(itemDecorator)

            layoutManager = GridLayoutManager(context, columns)
            layoutAnimation = animationController
            adapter = AdapterGrid(columns * rows)
            addItemDecoration(itemDecorator)
            scheduleLayoutAnimation()
        }

        return false
    }
}