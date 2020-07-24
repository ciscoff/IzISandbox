package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerDatum
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.EditorAction
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.SliderCallbackFactoryV1
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.SliderCallbackFactoryV2
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.UnderLayerStateFactoryV1
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.UnderLayerStateFactoryV2

class SmartSwipeActivity : AppCompatActivity() {

    private lateinit var ivRowIcon1 : ImageView
    private lateinit var ivRowIcon2 : ImageView
    private lateinit var upperLayer1 : LinearLayout
    private lateinit var underLayer1 : FrameLayout
    private lateinit var upperLayer2 : LinearLayout
    private lateinit var underLayer2 : FrameLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_swipe)

        findViews()

        // Просто заглушки
        val user = DoerDatum()
        val isConfirmed = true
        val adapterPosition = 0
        val doerId = 0
        val underLayerState1 = UnderLayerStateFactoryV1.createForDoer(user, isConfirmed)
        val underLayerState2 = UnderLayerStateFactoryV2.createForDoer(user, isConfirmed)

        upperLayer1.setOnTouchListener(
            StuffDragHandlerV1(
                upperLayer1,
                SliderCallbackFactoryV1.createForDoer(
                    upperLayer1,
                    underLayer1,
                    underLayerState1,
                    adapterPosition,
                    user,
                    doerId,
                    isConfirmed,
                    ::clickHandler
                )
            )
        )

        upperLayer2.setOnTouchListener(
            StuffDragHandlerV2(
                upperLayer2,
                SliderCallbackFactoryV2.createForDoer(
                    upperLayer2,
                    underLayer2,
                    underLayerState2,
                    adapterPosition,
                    user,
                    doerId,
                    isConfirmed,
                    ::clickHandler
                )
            )
        )
    }


    private fun findViews() {
        ivRowIcon1 = findViewById(R.id.iv_row_icon_1)
        upperLayer1 = findViewById(R.id.detail_row_1)
        underLayer1 = findViewById(R.id.under_layer_1)

        ivRowIcon2 = findViewById(R.id.iv_row_icon_2)
        upperLayer2 = findViewById(R.id.detail_row_2)
        underLayer2 = findViewById(R.id.under_layer_2)

        ivRowIcon1.setImageResource(R.drawable.vd_doer_state_confirmed)
        ivRowIcon2.setImageResource(R.drawable.vd_doer_state_confirmed)
    }

    private fun clickHandler(action : EditorAction, param1 : Int, param2 : Int) {

    }
}