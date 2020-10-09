package s.yarlykov.izisandbox.recycler_and_swipes.items_animation.item_animators

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_recycler_view_item_animation.*
import kotlinx.android.synthetic.main.activity_recycler_view_layout_animation.toolbar
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.items_animation.ItemOffsetDecoration

class RecyclerViewItemAnimationActivity : AppCompatActivity() {

    lateinit var itemDecorator: ItemOffsetDecoration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_item_animation)
        setSupportActionBar(toolbar)

        initRecyclerView()
    }

    private fun initRecyclerView() {

        val columns = 3
        val rows = 5

        itemDecorator = ItemOffsetDecoration(this)

        rvList.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, columns)
            itemAnimator = CustomItemAnimatorV1()
            addItemDecoration(itemDecorator)
            adapter = AdapterDoodle(columns * rows) {position ->
                this.adapter?.notifyItemChanged(position)
            }
        }
    }


}

