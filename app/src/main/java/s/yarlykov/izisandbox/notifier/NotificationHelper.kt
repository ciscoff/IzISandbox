package s.yarlykov.izisandbox.notifier

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import s.yarlykov.izisandbox.R

object NotificationHelper {

    /**
     * Определить действие для клика по сообщению:
     * - запуск активити, которое заточено только для реакции на notification.
     */
    private fun pendingIntentActivity(
        context: Context,
        clazz: Class<out AppCompatActivity>
    ): PendingIntent {

        val intent = Intent(context, clazz).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    /**
     * Создать builder для запуска активти clazz.
     *
     * @param smallIconRes - маленькая иконка, которая появляется в status bar'е
     * @param mainIconRes - картинка, которая видна внутри всплывающего HeadsUp и
     * внутри сообщения в открытом notification drawer.
     * @param titleId - заголовок
     * @param contentId - текст
     */
    fun builderToStartActivity(
        context: Context,
        @DrawableRes smallIconRes: Int,
        @DrawableRes mainIconRes: Int,
        @StringRes titleId: Int,
        @StringRes contentId: Int,
        @StringRes channelId: Int = R.string.channel_notify_1_id,
        clazz: Class<out AppCompatActivity> = PopUpActivity::class.java
    ): NotificationCompat.Builder {

        val channel = context.getString(channelId)
        val title = context.getString(titleId)
        val content = context.getString(contentId)

        val packageName = context.applicationContext.packageName
        val notificationLayout = RemoteViews(packageName, R.layout.layout_notification)

        // Заголовок сообщения
        notificationLayout.setTextViewText(R.id.notificationTitle, title)

        // Текст сообщения
        notificationLayout.setTextViewText(R.id.notificationBody, content)

        // Иконка сообщения
        ContextCompat.getDrawable(context, mainIconRes)?.run {
            val bitmap = toBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            notificationLayout.setImageViewBitmap(R.id.iconLogo, bitmap)
        }

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setColor(ContextCompat.getColor(context, R.color.colorAccentUi))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntentActivity(context, clazz))
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .apply {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setVibrate(LongArray(0))
            }
    }

    /**
     * Создать builder для запуска активти clazz.
     *
     * @action (NotificationCompat.Action) - это действие в notification. Фактически это
     * упаковка чего-то в Intent, далее в PendingIntent и отправка последнего системе.
     *
     * Мы будем запрашивать ввод текста пользователя и отправлять его в новое активити.
     * Весь пользовательский ввод инкапсулируется в RemoteInput.
     *
     * @remoteInput (RemoteInput) - это как бы контейнер для пользовательского ввода.
     */
    fun builderToSendUserInputFromNotification(
        context: Context,
        @DrawableRes smallIconRes: Int,
        @DrawableRes mainIconRes: Int,
        @StringRes titleId: Int,
        @StringRes contentId: Int,
        @StringRes channelId: Int = R.string.channel_notify_1_id,
        clazz: Class<out AppCompatActivity> = PopUpActivity::class.java
    ): NotificationCompat.Builder {

        // Канал, заголовок и текст notification
        val channel = context.getString(channelId)
        val title = context.getString(titleId)
        val content = context.getString(contentId)

        val packageName = context.applicationContext.packageName
        val notificationLayout = RemoteViews(packageName, R.layout.layout_notification)

        // Заголовок сообщения
        notificationLayout.setTextViewText(R.id.notificationTitle, title)

        // Текст сообщения
        notificationLayout.setTextViewText(R.id.notificationBody, content)

        // Иконка сообщения
        ContextCompat.getDrawable(context, mainIconRes)?.run {
            val bitmap = toBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            notificationLayout.setImageViewBitmap(R.id.iconLogo, bitmap)
        }

        // Label для кнопки action и key для упаковки пользовательского ввода в Intent.
        val replyKey = context.getString(R.string.key_text_reply)
        val replyLabel = context.getString(R.string.reply_label)

        val remoteInput: RemoteInput = RemoteInput.Builder(replyKey).build()

        val action = NotificationCompat.Action.Builder(
            R.drawable.ic_reply_action,     // Не отображается в UI (используется системная)
            replyLabel,                     // Отображается в UI
            pendingIntentActivity(context, clazz)
        ).addRemoteInput(remoteInput).build()

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setColor(ContextCompat.getColor(context, R.color.colorAccentUi))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntentActivity(context, clazz))
            .setDefaults(Notification.DEFAULT_ALL)
            .addAction(action)
            .setAutoCancel(true)
    }

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