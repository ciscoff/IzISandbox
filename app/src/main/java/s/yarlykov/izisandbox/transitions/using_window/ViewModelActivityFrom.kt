package s.yarlykov.izisandbox.transitions.using_window

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelActivityFrom : ViewModel() {
    private val _isReentrant = MutableLiveData<Boolean>().apply {
        value = false
    }

    val isReentrant : LiveData<Boolean> = _isReentrant

    fun hasEntered() {
        _isReentrant.value = true
    }
}