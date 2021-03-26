package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import s.yarlykov.izisandbox.utils.LiveDataT

/**
 * WARNING: Получатель может сделать обратный каст в mutable и пушить в Flow !
 */
class AvatarViewModel : ViewModel() {
    val permissionCamera = MutableSharedFlow<Boolean>()
    val permissionStorage = MutableSharedFlow<Boolean>()
    val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)

    val bitmapFlow = MutableSharedFlow<Bitmap>()
}