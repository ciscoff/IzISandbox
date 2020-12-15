package s.yarlykov.izisandbox.matrix.avatar_maker_v1

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.disposables.Disposable
import s.yarlykov.izisandbox.extensions.setRoundedDrawable

open class LiveImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var disposable: Disposable? = null

    var liveURI: LiveData<Uri>? = null
        set(value) {

            value?.let { _liveUri ->
                if (_liveUri == field) return

                field = _liveUri
                disposable?.dispose()
                disposable = liveURI?.observe(::makeRounded)

            } ?: setImageURI(Uri.EMPTY)
        }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disposable = liveURI?.observe(::makeRounded)
    }

    override fun onDetachedFromWindow() {
        disposable?.dispose()
        super.onDetachedFromWindow()
    }

    private fun makeRounded(uri: Uri?) {
        uri?.let {
            if (it != Uri.EMPTY) setRoundedDrawable(it)
        }
    }

    private inline fun <T> LiveData<T>.observe(crossinline observer: (t: T?) -> Unit): Removable<T> {
        val removable: Removable<T> = Removable(this, Observer { observer(it) })
        observeForever(removable.observer)
        return removable
    }
}