package s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm

import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.gesture.OverHead
import s.yarlykov.izisandbox.utils.LiveDataT

/**
 * WARNING: Получатель может сделать обратный каст в mutable и пушить в Flow !
 *
 * SharedFlow и StateFlow являются thread-safe, то есть можно выполнять любые операции
 * из разных корутин.
 *
 */

class AvatarViewModel : ViewModel() {
    val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)

    val permissionCamera = MutableSharedFlow<Boolean>()
    val permissionStorage = MutableSharedFlow<Boolean>()

    /**
     * Рамка видоискателя переместилась.
     */
    private val _rectClipState = MutableStateFlow(RectF())
    fun onRectClip(rect: RectF) {
        _rectClipState.value = rect
    }

    /**
     * Нажата кнопка Ready
     */
    private val _readyState = MutableSharedFlow<Long>()

    /**
     * Мапируем flow нажатия кнопки в flow rectClip
     */
    @ExperimentalCoroutinesApi
    val readyState = _readyState.flatMapLatest { _rectClipState }

    fun onReady() {
        viewModelScope.launch {
            _readyState.emit(System.currentTimeMillis())
        }
    }

    /**
     * Нажата кнопка Cancel
     */
    private val _cancelState = MutableSharedFlow<Long>()

    val cancelState = _cancelState.asSharedFlow()

    fun onCancel() {
        viewModelScope.launch {
            _cancelState.emit(System.currentTimeMillis())
        }
    }

    /**
     * Битмапа готова
     */
    fun onBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _bitmapFlow.emit(bitmap)
        }
    }

    /**
     * bitmapFlow используется фрагментом FragmentAvatar, который пересоздает свою View
     * (onCreateView/onViewCreated) при возврате в него из FragmentMaker. Соответственно
     * он и переподписывается на bitmapFlow, в которую FragmentMaker положил битмапу.
     * Чтобы её не профачить делаем кэш размером в 1 элемент. Это позволит при переподписке
     * получить битмапу.
     */
    private val _bitmapFlow = MutableSharedFlow<Bitmap>(replay = 1)

    val bitmapFlow = _bitmapFlow.asSharedFlow()

    /**
     * При перетаскивании рамки фиксируем overHead
     */
    fun onOverHead(overHead: OverHead) {
        viewModelScope.launch {
            _overHeadState.emit(overHead)
        }
    }

    private val _overHeadState = MutableSharedFlow<OverHead>()
    val overHeadState = _overHeadState.asSharedFlow()
}