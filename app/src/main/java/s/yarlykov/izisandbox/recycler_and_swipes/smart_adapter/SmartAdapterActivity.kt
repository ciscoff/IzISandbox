package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.controller.Controller

class SmartAdapterActivity : AppCompatActivity() {


    private val shortCardController = Controller(R.layout.item_controller_short_card)
    private val longCardController = Controller(R.layout.item_controller_long_card)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_adapter)
    }
}