package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import s.yarlykov.izisandbox.utils.LiveDataT

/**
 * WARNING: Получатель может сделать обратный каст в mutable и пушить в Flow !
 *
 * SharedFlow и StateFlow являются thread-safe, то есть можно выполнять любые операции
 * из разных корутин.
 */
class AvatarViewModel : ViewModel() {
    val permissionCamera = MutableSharedFlow<Boolean>()
    val permissionStorage = MutableSharedFlow<Boolean>()
    val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)

    val bitmapFlow = MutableSharedFlow<Bitmap>()

    val avatarClipFlow = MutableSharedFlow<RectF>()

}