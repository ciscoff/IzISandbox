package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.sand_box

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_smart_adapter.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter_v2.model.SmartList

class SmartAdapterActivityV2 : AppCompatActivity() {

    private val smartAdapter = SmartAdapterV2()

    val model = listOf(
        TextModel("It's a header 1", "Description 1"),
        TextModel("It's a header 2", "Description 2")
    )

    private val controller1 = Controller1(R.layout.item_text_one_row)
    private val controller2 = Controller1(R.layout.item_text_two_rows)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter)
        init()
    }

    fun init() {

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SmartAdapterActivityV2)
            adapter = smartAdapter
            setPadding(0, 16.px, 0, 16.px)
        }

        SmartList.create().apply {
            repeat(4) {
                addBindable(model[0], controller1)
            }
        }.also {
            smartAdapter.updateModel(it)
        }
    }
}