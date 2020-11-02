package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_infinite_recycler.*
import s.yarlykov.izisandbox.R

class InfiniteRecyclerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_recycler)

        infiniteList.apply {
            adapter =
                AdapterInfinite()
            layoutManager =
                LoopRecyclerManager()
        }
    }
}