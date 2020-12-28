package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_smart_adapter_v2.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.SmartAdapter
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.model.ItemList

class SmartAdapterActivityV2 : AppCompatActivity() {

    val smartAdapter = SmartAdapter()

    val oneRowController = ControllerOneRow(R.layout.item_text_one_row)
    val twoRowsController = ControllerTwoRows(R.layout.item_text_two_rows)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter_v2)
    }


    private fun init() {

        recyclerView.apply {

        }

        SmartList.create().apply {
            repeat(3) {
            }
        }

    }
}