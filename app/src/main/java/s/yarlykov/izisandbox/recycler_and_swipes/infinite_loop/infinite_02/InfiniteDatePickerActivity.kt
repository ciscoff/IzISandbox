package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_infinite_date_picker.*
import s.yarlykov.izisandbox.R

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
    }

    private fun generateModel() {
        model = InfiniteModel()
    }
}