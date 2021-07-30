package s.yarlykov.izisandbox.recycler_and_swipes.decorator.app

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_decorated_list.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.px
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.StubViewController
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.TimeStampController
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.offset.OffsetDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.round.RoundDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.prod.StickyItemDecorator
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList
import java.util.*

class DecoratedListActivity : AppCompatActivity() {

    companion object {
        const val patternDateTime = "dd MMMM yyyy HH:mm"
    }

    private val smartAdapter = SmartAdapterV2()

    private val timeStampController = TimeStampController(R.layout.item_time_stamp)
    private val stubViewController = StubViewController(R.layout.item_controller_short_card)
    private val stickyDecorator = StickyItemDecorator()

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
            .overlay(stickyDecorator)
            .offset(stubViewController.viewType() to offsetsDecor)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_decorated_list)
        init()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_sticky_decor_mode, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_highlight -> {
                item.isChecked = !item.isChecked
                stickyDecorator.highlightMode(item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun init() {

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@DecoratedListActivity)
            adapter = smartAdapter
            setPadding(0, 0, 0, 0)
            addItemDecoration(decorator)
        }

        val formatter = DateTimeFormatter.ofPattern(patternDateTime)
        val now = LocalDateTime.now()

        SmartList.create().apply {
            repeat(20) { i ->

                // Добавить sticky элемент (генерим строку с датой и временем)
                if (i.rem(2) == 0) {
                    addItem(
                        now.plusDays(i.toLong()).minusMinutes((0..179).random().toLong())
                            .format(formatter), timeStampController
                    )
                }

                // Добавить обычный элемент
                addItem(stubViewController)
            }
        }.also(smartAdapter::updateModel)
    }
}