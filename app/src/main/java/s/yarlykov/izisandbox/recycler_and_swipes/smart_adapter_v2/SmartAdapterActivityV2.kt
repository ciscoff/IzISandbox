package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_smart_adapter_v2.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.model.ItemList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.adapter.SmartAdapter

class SmartAdapterActivityV2 : AppCompatActivity() {

    private val simpleAdapter = SmartAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter_v2)

        init()
    }

    private fun init() {

        recyclerView.apply {
            adapter = simpleAdapter
            setPadding(0, 16.px, 0, 16.px)
            layoutManager = LinearLayoutManager(this@SmartAdapterActivityV2)
        }

        ItemList.create().apply {
            repeat(2) {
                addItem(OneRowDataItem())
            }
            repeat(3) {
                addItem(TwoRowsDataItem())
            }
            repeat(1) {
                addItem(OneRowDataItem())
            }
            repeat(4) {
                addItem(TwoRowsDataItem())
            }
            repeat(1) {
                addItem(OneRowDataItem())
            }
        }.also(simpleAdapter::updateModel)
    }
}