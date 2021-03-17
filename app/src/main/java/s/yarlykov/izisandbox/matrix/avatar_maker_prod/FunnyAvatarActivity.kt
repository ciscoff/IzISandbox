package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.LiveDataT

class FunnyAvatarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_funny_avatar)
    }

    class LocalModel : ViewModel() {
        val permissionCamera = LiveDataT(false)
        val permissionStorage = LiveDataT(false)
        val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)
    }
}