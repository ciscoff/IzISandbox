package s.yarlykov.izisandbox.matrix.avatar_maker

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.disposables.Disposable

class Removable<T>(
    private val liveData: LiveData<T>,
    val observer: Observer<T>
) : Disposable {

    private var isDisposed = false

    override fun isDisposed(): Boolean = isDisposed

    override fun dispose() {
        isDisposed = true
        liveData.removeObserver(observer)
    }
}