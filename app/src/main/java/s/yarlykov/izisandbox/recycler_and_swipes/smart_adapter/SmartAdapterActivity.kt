package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_smart_adapter.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.*
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.Controller
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.ItemList

class SmartAdapterActivity : AppCompatActivity() {

    private val smartAdapter = SmartAdapter()

    private val shortCardController = Controller(R.layout.item_controller_short_card)
    private val longCardController = Controller(R.layout.item_controller_long_card)

    private val horizontalAndVerticalOffsetDecor by lazy {
        SimpleOffsetDrawer(
            left = 16.px,
            top = 8.px,
            right = 16.px,
            bottom = 8.px
        )
    }

    private val horizontalOffsetDecor by lazy {
        SimpleOffsetDrawer(
            left = 16.px,
            right = 16.px
        )
    }

    private val dividerDrawer2Dp by lazy {
        LinearDividerDrawer(
            Gap(
                resources.getColor(R.color.gray_A150),
                2.px,
                paddingStart = 16.px,
                paddingEnd = 16.px,
                rule = Rules.MIDDLE
            )
        )
    }

    private val roundDecor by lazy {
        RoundDecor(12.px.toFloat(), roundPolitic = RoundPolitic.Group())
    }

    private val decorator by lazy {
        Decorator.Builder()
            .underlay(longCardController.viewType to roundDecor)
            .overlay(shortCardController.viewType to dividerDrawer2Dp)
            .offset(longCardController.viewType to horizontalOffsetDecor)
            .offset(shortCardController.viewType to horizontalOffsetDecor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter)
        init()
    }

    private fun init() {

        recyclerView.apply {
            adapter = smartAdapter
            addItemDecoration(decorator)
            setPadding(0, 16.px, 0, 16.px)
            layoutManager = LinearLayoutManager(this@SmartAdapterActivity)
        }

        ItemList.create()
            .apply {
                repeat(3) {
                    add(longCardController)
                }
                repeat(2) {
                    add(shortCardController)
                }

                smartAdapter.updateControllers(this)
            }.also(smartAdapter::updateItems)
    }
}