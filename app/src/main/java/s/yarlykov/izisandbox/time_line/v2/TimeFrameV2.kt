package s.yarlykov.izisandbox.time_line.v2

import android.content.Context
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R

class TimeFrameV2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        isClickable = false
        isFocusable = false

        setBackgroundResource(R.drawable.background_time_frame)

        setOnTouchListener(null)
    }
}

