package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_infinite_date_picker.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.extensions.toReadable
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01.AdapterInfinite
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01.LoopRecyclerManager

class InfiniteDatePickerActivity : AppCompatActivity(), OverScrollListener {

    companion object {
        const val MODEL_SIZE = 20
    }

    private val model = mutableListOf<ZDate>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_date_picker)

        generateModel()

        infinitePicker.apply {
            adapter = AdapterDates(model)
            layoutManager = LoopRecyclerManager()
        }

    }

    private fun generateModel() {
        val now = ZDate.now()
        model.clear()

        (0 until MODEL_SIZE).forEach { i ->
            model.add(i, now.plusDays(i.toLong()))
        }
    }

    override fun onTopOverScroll() {
    }

    override fun onBottomOverScroll() {
    }
}