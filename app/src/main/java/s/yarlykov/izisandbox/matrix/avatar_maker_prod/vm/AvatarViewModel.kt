package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import s.yarlykov.izisandbox.utils.LiveDataT

class AvatarViewModel : ViewModel() {
    val permissionCamera = LiveDataT(false)
    val permissionStorage = LiveDataT(false)
    val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)

    val bitmapMutable = MutableLiveData<Bitmap>()

    /**
     * WARNING: Получатель может сделать обратный каст в mutable и пушить битмапы !
     */
    val bitmapLive: LiveData<Bitmap>
        get() = bitmapMutable
}