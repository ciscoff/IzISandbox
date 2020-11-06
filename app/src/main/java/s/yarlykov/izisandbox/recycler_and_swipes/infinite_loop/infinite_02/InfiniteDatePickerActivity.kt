package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearSnapHelper
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

        /**
         * Данный класс центрирует child'а RecyclerView, который ПОСЛЕ окончания пользовательской
         * прокрутки ближе всего к центру. Остальные child'ы также смещаются.
         *
         * Примерный смысл его действий следующий: установить в RecyclerView свой обработчик
         * OnScrollListener. По окончании пользовательской прокрутки, используя LayoutManager
         * пройтись по child'ам, в поисках того, который ближе к центру (это называется snap view).
         * Затем с помощью своего SmoothScroller'а и вызова LayoutManager.startSmoothScroll
         * анимированно дотянуть контент таким образом, чтобы snap view встал четко по центру.
         *
         * NOTE: Этим классом пользуется ViewPager2, чтобы устанавливать контент очередной страницы
         * внутри своего viewPort.
         */
        LinearSnapHelper().apply {
            attachToRecyclerView(infinitePicker)
        }
    }

    private fun generateModel() {
        model = InfiniteModel()
    }
}