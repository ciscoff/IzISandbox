package s.yarlykov.izisandbox.matrix.avatar_maker.v5

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.notZero
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleConsumerV5
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleControllerV5

class AvatarCompoundViewV5 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ScaleControllerV5 {

    private val avatarBack: AvatarBaseViewV5
    private val avatarFront: AvatarBaseViewV5

    private val animDuration = context.resources.getInteger(R.integer.anim_duration_avatar).toLong()
    private val scaleConsumers = ArrayList<ScaleConsumerV5>()

    private val onSizeAvatarBack = MutableStateFlow(0 to 0)
    private val onSizeAvatarFront = MutableStateFlow(0 to 0)

    /**
     * Исходная Bitmap
     */
    private var sourceImageBitmap: Bitmap? = null

    /**
     * @scaleMin - изменяемая величина. После каждой анимации зума она показывает текущее
     * отношение rectBitmapVisibleHeightMin / rectBitmapVisible.height(). То есть это значение для
     * скалирования, при котором будет достугнут нижний предел, то есть rectBitmapVisible.height
     * станет равна rectBitmapVisibleHeightMin.
     */
    override var scaleShrink: Float = 1f
//    override var scaleSqueeze: Float = 1f

    override var bitmapScaleCurrent: Float = 1f
    override var bitmapScaleMin: Float = 0f

    init {
        View.inflate(context, R.layout.layout_avatar_components_v3, this).also { view ->
            avatarBack = view.findViewById(R.id.avatarBack)
            avatarFront = view.findViewById(R.id.avatarFront)

            scaleConsumers.add(avatarBack)
            scaleConsumers.add(avatarFront)

            scaleConsumers.forEach {
                it.onScaleUpAvailable(false)
                it.onScaleDownAvailable(false)
            }

            avatarFront.scaleController = this
            avatarBack.scaleController = this
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        /**
         * Подписка на извещения от дочерних элементов об окончании обработки onSizeChanged.
         * После этого им можно отдать bitmap'у.
         */
        (context as AppCompatActivity).lifecycleScope.launch {
            combine(onSizeAvatarBack, onSizeAvatarFront) { backSize, frontSize ->
                listOf(backSize, frontSize)
            }.filter { list ->
                list.all { it.notZero }
            }.collect {
                sourceImageBitmap?.let { bitmap ->
                    avatarBack.onBitmapReady(bitmap)
                    avatarFront.onBitmapReady(bitmap)
                    bitmapScaleMin = avatarBack.bitmapVisibleHeightMin / bitmap.height
                }
            }
        }
    }

    /**
     * Исходную битмапу загружаем с понижением её resolution.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sourceImageBitmap = loadSampledBitmapFromResource(R.drawable.m_4, w, h)
    }

    @ExperimentalCoroutinesApi
    override fun onFrontSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarFront.value = size
    }

    @ExperimentalCoroutinesApi
    override fun onBackSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarBack.value = size
    }

    /**
     * Дочерний элемент просит запустить анимацию
     * @param factor - scale factor
     * @param pivot - фокус скалирования в координатах view
     */
    override fun onScaleRequired(factor: Float, pivot: PointF) {

        // Подготовиться к началу анимации в дочерних Views
        scaleConsumers.forEach { it.onPreAnimate(factor, pivot) }

        // Запустить анимацию
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = animDuration

            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                scaleConsumers.forEach { it.onAnimate(fraction) }
                scaleConsumers.forEach { (it as View).invalidate() }
            }

            // После завершения анимации вызывать у всех onPostScale()
            addListener(onEnd = {
                scaleConsumers.forEach { it.onPostAnimate() }
            })

        }.start()
    }

    /**
     * Дочерний элемент сообщает разрешает/запрещает анимацию.
     */
    override fun onScaleDownAvailable(isAvailable: Boolean) {
        scaleConsumers.forEach { it.onScaleDownAvailable(isAvailable) }
    }

    override fun onScaleUpAvailable(isAvailable: Boolean) {
        scaleConsumers.forEach { it.onScaleUpAvailable(isAvailable) }
    }

    /**
     * Загрузка большой bitmap'ы с понижением resolution до размеров View, в которой она должна
     * отображаться.
     * https://stackoverflow.com/questions/32121058/most-memory-efficient-way-to-resize-bitmaps-on-android
     * https://stackoverflow.com/questions/33714992/how-is-bitmap-options-insamplesize-supposed-to-work
     *
     * @param reqWidth - требуемая ширина. Это ширина данного View.
     * @param reqHeight - требуемая высота. Это высота данного View
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

    /**
     * inSampleSize - это количество пикселей исходного изображения, которое соответствует
     * одному пикселю создаваемого (декодированного) изображения. Например, если
     * inSampleSize = 2, то декодированная картинка будет иметь размеры W/2 x H/2 и количество
     * пикселей W/2 * H/2 = (W*H)/4, то есть в 4 раза меньше, чем в исходном.
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {

        val (rawHeight: Int, rawWidth: Int) = options.run { outHeight to outWidth }

        // Если картинка достаточно маленькая, то декодируем без изменений.
        if (rawHeight < reqHeight / 2 && rawWidth < reqWidth / 2) return 1

        // Для остальных случаев сразу уменьшаем размеры в 2 раза.
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