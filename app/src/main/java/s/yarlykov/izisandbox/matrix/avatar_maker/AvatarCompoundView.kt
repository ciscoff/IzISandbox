package s.yarlykov.izisandbox.matrix.avatar_maker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.PhotoHelper.reduce

class AvatarCompoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val avatarBack: MediaDataConsumer
    private val avatarFront: MediaDataConsumer

    /**
     * Исходная Bitmap и её размеры
     */
    private var sourceImageBitmap: Bitmap? = null
    private var rectSourceImage = Rect()

    init {
        View.inflate(context, R.layout.layout_avatar_components, this).also { view ->
            avatarBack = view.findViewById<ViewGroup>(R.id.avatarBack) as MediaDataConsumer
            avatarFront = view.findViewById<ViewGroup>(R.id.avatarFront) as MediaDataConsumer
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        sourceImageBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.nature)
                ?.also { bitmap ->
                    avatarBack.onBitmapReady(bitmap)
                    avatarFront.onBitmapReady(bitmap)
                }?.reduce()
    }
}