package s.yarlykov.izisandbox.recycler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class SwipeAndDropActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_and_drop)

        val model = resources.getStringArray(R.array.api_list).toMutableList()

        val adapterItems = RecyclerAdapter(model)
        val itemTouchHelper = initTouchHelper(adapterItems)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {

            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = adapterItems
            layoutManager = LinearLayoutManager(this@SwipeAndDropActivity)
        }

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun initTouchHelper(adapter: RecyclerAdapter): ItemTouchHelper {
        val callback = ItemTouchHelperCallback(this, adapter)
        return ItemTouchHelper(callback)
    }
}