package s.yarlykov.izisandbox.recycler_and_swipes.swipe_4

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class SwipeAnyWhereActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_any_where)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        findViewById<RecyclerView>(R.id.recycler_any_where).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = AdapterItem()
            layoutManager = LinearLayoutManager(this@SwipeAnyWhereActivity)
            addOnScrollListener(RecyclerScrollListener)
        }
    }
}