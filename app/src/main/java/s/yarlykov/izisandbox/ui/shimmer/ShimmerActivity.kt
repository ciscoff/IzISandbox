package s.yarlykov.izisandbox.ui.shimmer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.databinding.ActivityShimmerBinding
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.adapter.SmartAdapterV2
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v2.model.SmartList

class ShimmerActivity : AppCompatActivity() {

    private val smartAdapter = SmartAdapterV2()
    private val stubViewController = StubController(R.layout.layout_item_stub)

    lateinit var binding: ActivityShimmerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShimmerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewBinding {
            recyclerViewShimmer.setup()
        }
    }

    private fun viewBinding(op: ActivityShimmerBinding.() -> Unit) {
        binding.op()
    }

    /**
     * Пока нет реальных данных, то показываем анимированную заглушку.
     */
    private fun RecyclerView.setup() {

        // При показе анимированных заглушек отключаем скроллинг.
        layoutManager = object : LinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        adapter = smartAdapter

        SmartList.create().apply {
            repeat(15) {
                addItem(stubViewController)
            }
        }.also(smartAdapter::updateModel)
    }
}