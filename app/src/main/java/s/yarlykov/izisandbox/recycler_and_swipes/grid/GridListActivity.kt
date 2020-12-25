package s.yarlykov.izisandbox.recycler_and_swipes.grid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_grid_list.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt

class GridListActivity : AppCompatActivity() {

    companion object {
        const val COLUMNS = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_list)
        initRecyclerView()
    }

    private fun initRecyclerView() {

        val manager = CellLayoutManager(this, COLUMNS)
        val decorator = manager.decorator

        recyclerView.apply {
            adapter = AdapterCell(11)
            layoutManager = manager
            addItemDecoration(decorator)
        }
    }
}