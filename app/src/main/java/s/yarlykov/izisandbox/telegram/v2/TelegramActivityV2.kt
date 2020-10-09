package s.yarlykov.izisandbox.telegram.v2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_telegram_v2.*
import s.yarlykov.izisandbox.R

class TelegramActivityV2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_v2)
        initList()
    }

    private fun initList() {

        listView.apply {
            layoutManager = CustomLayoutManager(this@TelegramActivityV2, actionBarLayout)
            setHasFixedSize(true)
            itemAnimator = null
            adapter = AdapterLinear()
        }

        (listView as SmartRecyclerView).setOnOffsetListener(::dataExchanger)
    }

    /**
     * Рестранслятор сообщения между дочерними View. Надо бы заменить на что-то более путевое.
     */
    private fun dataExchanger(offset: Int) {
        actionBarLayout.onOffsetChanged(offset)
    }
}