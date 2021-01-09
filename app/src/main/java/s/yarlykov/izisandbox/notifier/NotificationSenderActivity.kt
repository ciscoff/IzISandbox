package s.yarlykov.izisandbox.notifier

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.android.synthetic.main.activity_notification_sender.*
import s.yarlykov.izisandbox.R

class NotificationSenderActivity : AppCompatActivity() {

    private var notificationId: Int = 1001
        get() = field++

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_sender)
        init()
    }

    private fun init() {
        NotificationHelper.createNotificationChannel(applicationContext)

        val builder1 = NotificationHelper.builderToStartActivity(
            this,
            R.drawable.ic_izi_pizi,
            R.string.notification_1_title,
            R.string.notification_1_text
        )

        val builder2 = NotificationHelper.builderToSendUserInputFromNotification(
            this,
            R.drawable.ic_beer,
            R.string.notification_2_title,
            R.string.notification_2_text
        )

        buttonActivity.setOnClickListener(Clicker(builder1))

        // При каждой отправке Reply будет пересоздаваться (onCreate) активити PopUpActivity
        buttonReply.setOnClickListener(Clicker(builder2))
    }

    inner class Clicker(val builder: NotificationCompat.Builder) : View.OnClickListener {

        override fun onClick(v: View) {
            with(NotificationManagerCompat.from(v.context)) {
                notify(notificationId, builder.build())
            }
        }
    }
}