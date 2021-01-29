package s.yarlykov.izisandbox.matrix.avatar_maker.v3

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import s.yarlykov.izisandbox.R

class AvatarCompoundViewV3 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ScaleController {

    private val avatarBack: AvatarBaseViewV3
    private val avatarFront: AvatarBaseViewV3

    private val animDuration = context.resources.getInteger(R.integer.anim_duration_avatar).toLong()
    private val scaleConsumers = ArrayList<ScaleConsumer>()

    /**
     * Исходная Bitmap
     */
    private var sourceImageBitmap: Bitmap? = null

    init {
        View.inflate(context, R.layout.layout_avatar_components_v3, this).also { view ->
            avatarBack = view.findViewById(R.id.avatarBack)
            avatarFront = view.findViewById(R.id.avatarFront)

            scaleConsumers.add(avatarBack)
            scaleConsumers.add(avatarFront)

            avatarFront.scaleController = this
            avatarBack.scaleController = this
        }
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

    /**
     * Дочерний элемент просит запустить анимацию
     * @param factor - scale factor
     * @param pivot - фокус скалирования в координатах view
     */
    override fun onScaleRequired(factor: Float, pivot: PointF) {

        // Подготовиться к началу анимации в дочерних Views
        scaleConsumers.forEach { it.onPreScale(factor, pivot) }

        // Запустить анимацию
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animDuration

            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                scaleConsumers.forEach { it.onScale(fraction) }
                scaleConsumers.forEach { (it as View).invalidate() }
            }
        }.start()
    }

    /**
     * Дочерний элемент сообщает разрешает/запрещает анимацию.
     */
    override fun onScaleAvailable(isAvailable: Boolean) {
        scaleConsumers.forEach { it.onScaleAvailable(isAvailable) }
    }

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