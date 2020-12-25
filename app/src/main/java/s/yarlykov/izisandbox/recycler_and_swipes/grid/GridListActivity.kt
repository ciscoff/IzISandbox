package s.yarlykov.izisandbox.recycler_and_swipes.grid

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_grid_list.*
import s.yarlykov.izisandbox.R

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

        recyclerView.apply {
            adapter = AdapterCell(3)
            layoutManager = CellLayoutManager(this@GridListActivity, COLUMNS)
            addItemDecoration(CellDecorator(this@GridListActivity, 1))
        }
    }
}