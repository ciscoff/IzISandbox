package s.yarlykov.izisandbox.telegram.v2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_telegram_v2.*
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.recycler_and_swipes.items_animation.ItemOffsetDecoration

class TelegramActivityV2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram_v2)

        initWindowDecor()
        initList()
    }

    /**
     * Инициализация RecyclerView
     */
    private fun initList() {

        listView.apply {
            addItemDecoration(ListItemDecorator(this@TelegramActivityV2))
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

    /**
     * Внешний вид окна активити:
     * - Полупрозрачный statusBar
     * - Непрозрачный navigationBar
     *
     * + см. тему @style/TelegramTheme
     */
    private fun initWindowDecor() {
        window.apply {

            val color = ContextCompat.getColor(applicationContext, R.color.color_dark_1)

            statusBarColor = color
            navigationBarColor = color
        }
    }
}