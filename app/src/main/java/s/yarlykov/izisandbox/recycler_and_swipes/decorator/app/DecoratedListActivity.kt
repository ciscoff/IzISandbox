package s.yarlykov.izisandbox.recycler_and_swipes.decorator.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_smart_adapter.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.StubViewController
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.TimeStampController
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.offset.OffsetDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.round.RoundDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.StickyItemDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.controller.BindableItemController
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.holder.BindableViewHolder
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList

class DecoratedListActivity : AppCompatActivity() {

    private val smartAdapter = SmartAdapterV2()

    private val timeStampController = TimeStampController(R.layout.item_time_stamp)
    private val stubViewController = StubViewController(R.layout.item_controller_short_card)

    // Декоратор отступов
    private val offsetsDecor by lazy {
        OffsetDecorator(
            left = 12.px,
            top = 4.px,
            right = 12.px,
            bottom = 4.px
        )
    }

    // Закругляем углы у элементов StubView
    private val roundDecor by lazy {
        RoundDecorator(12.px.toFloat())
    }

    val decorator by lazy {
        Decorator.Builder()
            .underlay(stubViewController.viewType() to roundDecor)
            .overlay(StickyItemDecorator())
            .offset(stubViewController.viewType() to offsetsDecor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decorated_list)

        init()
    }

    fun init() {

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DecoratedListActivity)
            adapter = smartAdapter
            setPadding(0, 0, 0, 0)
            addItemDecoration(decorator)
        }

        SmartList.create().apply {
            repeat(20) { i ->

                // Добавить sticky элемент
                if(i.rem(4) == 0) {
                    val date = DateFormat.format("dd MMMM yyyy HH:mm:ss", System.currentTimeMillis() + i * 1000)
                    addItem(date.toString(), timeStampController as BindableItemController<String, BindableViewHolder<String>>)
                }

                // Добавить обычный элемент
                addItem(stubViewController)
            }
        }.also(smartAdapter::updateModel)
    }
}