package s.yarlykov.izisandbox.extensions

import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import s.yarlykov.izisandbox.R

/**
 * Показать SnackBar
 */
fun View.showSnackBarNotification(message: String, callback : Snackbar.Callback? = null) {
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).apply {
        setActionTextColor(ContextCompat.getColor(view.context, R.color.colorAccent))

        callback?.let {block ->
            addCallback(block)
        }

    }.show()
}