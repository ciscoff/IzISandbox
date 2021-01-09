package s.yarlykov.izisandbox.notifier

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import s.yarlykov.izisandbox.R

object NotificationHelper {

    /**
     * Создать channel
     */
    fun createNotificationChannel(
        context: Context,
        @StringRes channelId: Int = R.string.channel_notify_1_id,
        @StringRes nameId: Int = R.string.channel_notify_1_name,
        @StringRes descriptionId: Int = R.string.channel_notify_1_description
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(nameId)
            val descriptionText = context.getString(descriptionId)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                context.getString(channelId),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}