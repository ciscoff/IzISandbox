package s.yarlykov.izisandbox.time_line

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R


/**
 * Думаю можно обойтись контейнером FrameLayout и двумя дочерними View:
 * - одна для background (свободное/занятое время двумя цветами, шкала и раочие часы)
 * - вторая для ползунка (просто View и background как рамка)
 */

class TimeLineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_time_line)
    }
}