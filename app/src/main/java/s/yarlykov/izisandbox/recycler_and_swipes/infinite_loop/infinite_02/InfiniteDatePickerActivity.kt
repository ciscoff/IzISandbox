package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_infinite_date_picker.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.extensions.toReadable
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01.LoopRecyclerManagerV01
import java.util.*

class InfiniteDatePickerActivity : AppCompatActivity() {

    companion object {
        const val MODEL_SIZE = 4
    }

    private lateinit var model: InfiniteModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_date_picker)

        generateModel()

        infinitePicker.apply {
            adapter = AdapterDates(model)
            layoutManager = LoopRecyclerManagerV02(model)
        }

        infinitePicker.adapter?.notifyItemRemoved(0)
    }

    private fun generateModel() {
        val now = ZDate.now()

        model = InfiniteModel(
            (0 until MODEL_SIZE).map { i -> now.plusDays(i.toLong()) }.toMutableList()
        )
    }

     fun onTopOverScroll() {
        val first = model.first()

        Collections.rotate(model, 1)
        model[0] = first.minusDays(1)

//        model.clear()
//        logIt("onTopOverScroll first=${first.toReadable(this)}", "PLPLPL")
//        (0 until MODEL_SIZE).forEach {i ->
//            model.add(i, first.minusDays((MODEL_SIZE - i).toLong()))
//        }
    }

     fun onBottomOverScroll() {
        val last = model.last()
        Collections.rotate(model, -1)
        model[model.lastIndex] = last.plusDays(1)

//        model.clear()
//        logIt("onBottomOverScroll last=${last.toReadable(this)}", "PLPLPL")
//        (0 until MODEL_SIZE).forEach { i ->
//            model.add(i, last.plusDays((i + 1).toLong()))
//        }
    }
}