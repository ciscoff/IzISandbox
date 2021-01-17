package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker.MediaDataConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker.v1.AvatarBackViewV1
import s.yarlykov.izisandbox.matrix.avatar_maker.v1.AvatarFrontViewV1

class AvatarCompoundViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val avatarBack: AvatarBaseViewV3
    private val avatarFront: AvatarBaseViewV3

    /**
     * Исходная Bitmap и её размеры
     */
    private var sourceImageBitmap: Bitmap? = null
    private var rectSourceImage = Rect()

    init {
        View.inflate(context, R.layout.layout_avatar_components_v3, this).also { view ->
            avatarBack = view.findViewById(R.id.avatarBack)
            avatarFront = view.findViewById(R.id.avatarFront)

            avatarFront.onScaleChangeListener = ::onViewPortScaled
        }
    }

    private fun onViewPortScaled(scale: Float) {
        avatarBack.onScaleChanged(scale)
    }

    /**
     * Сразу загружаем полную bitmap без скалирования. Далее она будет использоваться как
     * исходный материал для скалированных картинок.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        sourceImageBitmap =
            BitmapFactory.decodeResource(context.resources, R.drawable.nature)
                ?.also { bitmap ->
                    avatarBack.onBitmapReady(bitmap)
                    avatarFront.onBitmapReady(bitmap)
                }
    }

//    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
//        super.onSizeChanged(w, h, oldw, oldh)
//
//        sourceImageBitmap = loadSampledBitmapFromResource(R.drawable.nature, w, h).also { bitmap ->
//            avatarBack.onBitmapReady(bitmap)
//            avatarFront.onBitmapReady(bitmap)
//        }
//    }

    /**
     * Загрузка большой bitmap'ы с понижением resolution до размеров View, в которой она должна
     * отображаться.
     *
     * + https://stackoverflow.com/questions/32121058/most-memory-efficient-way-to-resize-bitmaps-on-android
     */
    private fun loadSampledBitmapFromResource(
        @DrawableRes resourceId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {

        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, this)

            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory.decodeResource(context.resources, resourceId, this)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {

        val (rawHeight: Int, rawWidth: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (rawHeight > reqHeight || rawWidth > reqWidth) {

            val halfHeight: Int = rawHeight / 2
            val halfWidth: Int = rawWidth / 2

            while (
                halfHeight / inSampleSize >= reqHeight &&
                halfWidth / inSampleSize >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}