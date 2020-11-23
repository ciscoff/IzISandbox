package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.toReadable

class InfiniteCalendarActivity : AppCompatActivity() {

    lateinit var calendar: ScrollingCalendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_calendar)

        calendar = findViewById<ScrollingCalendar>(R.id.calendarCompound).apply {
            // DEBUG
//            initialDateTime = LocalDateTime.of(2020, Month.NOVEMBER, 19, 15, 30, 0)
        }

        val textView = findViewById<TextView>(R.id.textResult)

        // Кнопка для теста
        findViewById<Button>(R.id.buttonTest).apply {
            setOnClickListener {
                textView.text = calendar.selectedDateTime.toReadable(this@InfiniteCalendarActivity)
            }
        }
    }
}