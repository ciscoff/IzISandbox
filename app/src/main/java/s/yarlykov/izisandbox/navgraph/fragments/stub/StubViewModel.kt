package s.yarlykov.izisandbox.navgraph.fragments.stub

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StubViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is stub Fragment"
    }
    val text: LiveData<String> = _text
}