package s.yarlykov.izisandbox.recycler.swipe_2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler.swipe_1.RecyclerAdapter1

class SwipeActivitySecond : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_swipe_second)

        val model = resources.getStringArray(R.array.api_list).toMutableList()
        val adapterItems = RecyclerAdapter2(model)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            itemAnimator = DefaultItemAnimator()
            adapter = adapterItems
            layoutManager = LinearLayoutManager(this@SwipeActivitySecond)
        }
    }
}