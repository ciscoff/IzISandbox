package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_smart_adapter.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.adapter.SmartAdapter
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.base.viewType
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.model.ItemList
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.offset.OffsetDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator.round.RoundDecorator

class SmartAdapterActivityV1 : AppCompatActivity() {

    private val simpleAdapter = SmartAdapter()

    // Декоратор отступов 1
    private val horizontalOffsetsDecor1 by lazy {
        OffsetDecorator(
            left = 46.px,
            right = 16.px
        )
    }

    // Декоратор отступов 2
    private val horizontalOffsetsDecor2 by lazy {
        OffsetDecorator(
            left = 16.px,
            right = 46.px
        )
    }

    // Декоратор, который закругляет углы (TODO: нужно добавить политику)
    private val roundDecor by lazy {
        RoundDecorator(12.px.toFloat())
    }

    // Генерим инстанс RecyclerView.ItemDecoration с помощью билдера.
    // Билдеру сообщаем обо всех декораторах, которые хотим применить.
    private val decorator by lazy {
        Decorator.Builder()
            .underlay(OneRowDataItem::class.java.viewType to roundDecor)
            .offset(OneRowDataItem::class.java.viewType to horizontalOffsetsDecor1)
            .offset(TwoRowsDataItem::class.java.viewType to horizontalOffsetsDecor2)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter)
        init()
    }

    private fun init() {

        recyclerView.apply {
            adapter = simpleAdapter
            addItemDecoration(/*MainDecorator(DecorController())*/decorator)
            layoutManager = LinearLayoutManager(this@SmartAdapterActivityV1)
            setPadding(8.px, 0, 8.px, 0)
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