package s.yarlykov.izisandbox.recycler_and_swipes.swipe_3

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.DoerDatum
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.domain.EditorAction
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.SliderCallbackFactory
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.factories.UnderLayerStateFactory

class SmartSwipeActivity : AppCompatActivity() {

    private lateinit var ivRowIcon : ImageView
    private lateinit var upperLayer : LinearLayout
    private lateinit var underLayer : FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_smart_swipe)

        findViews()

        // Просто заглушки
        val user = DoerDatum()
        val isConfirmed = true
        val adapterPosition = 0
        val doerId = 0
        val underLayerState = UnderLayerStateFactory.createForDoer(user, isConfirmed)

        upperLayer.setOnTouchListener(
            StuffDragHandler(
                upperLayer,
                SliderCallbackFactory.createForDoer(
                    upperLayer,
                    underLayer,
                    underLayerState,
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
        ivRowIcon = findViewById<ImageView>(R.id.iv_row_icon)
        upperLayer = findViewById<LinearLayout>(R.id.detail_row)
        underLayer = findViewById<FrameLayout>(R.id.under_layer)

        ivRowIcon.setImageResource(R.drawable.vd_doer_state_confirmed)
    }

    private fun clickHandler(action : EditorAction, param1 : Int, param2 : Int) {

    }
}