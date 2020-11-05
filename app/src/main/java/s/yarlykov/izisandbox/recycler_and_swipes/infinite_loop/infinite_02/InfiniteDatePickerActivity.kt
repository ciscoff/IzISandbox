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
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02.InfiniteModel.Companion.MODEL_SIZE
import java.util.*

class InfiniteDatePickerActivity : AppCompatActivity() {

    private lateinit var model: InfiniteModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_date_picker)

        generateModel()

        infinitePicker.apply {
            layoutManager = LoopRecyclerManagerV02(model)
            adapter = AdapterDates(model)
        }

        infinitePicker.adapter?.notifyItemRemoved(0)
    }

    private fun generateModel() {
        model = InfiniteModel()
    }

}