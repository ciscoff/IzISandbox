package s.yarlykov.izisandbox.recycler.swipe_1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class SwipeActivityFirst : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_first)

        val model = resources.getStringArray(R.array.api_list).toMutableList()

        val adapterItems =
            RecyclerAdapter1(model)
        val itemTouchHelper = initTouchHelper(adapterItems)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {

            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = adapterItems
            layoutManager = LinearLayoutManager(this@SwipeActivityFirst)
        }

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun initTouchHelper(adapter: RecyclerAdapter1): ItemTouchHelper {
        val callback =
            ItemTouchHelperCallback(
                this,
                adapter
            )
        return ItemTouchHelper(callback)
    }
}