package s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.toReadable

class InfiniteCalendarActivity : AppCompatActivity() {

    lateinit var calendar: ScrollingCalendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infinite_calendar)

        calendar = findViewById(R.id.calendarCompound)

        // Кнопка для теста
        findViewById<Button>(R.id.buttonTest).apply {
            setOnClickListener {
                logIt(
                    "result = ${calendar.selectedDateTime.toReadable(this@InfiniteCalendarActivity)}",
                    "PLPLPL"
                )
            }
        }
    }
}