package s.yarlykov.izisandbox.matrix.avatar_maker.v5

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.awaitEnd
import s.yarlykov.izisandbox.extensions.notZero
import s.yarlykov.izisandbox.matrix.avatar_maker.EditorAvatarActivity.Companion.IMAGE_ID

class AvatarCompoundViewV5 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ScaleControllerV5 {

    private val avatarBack: AvatarBaseViewV5
    private val avatarFront: AvatarBaseViewV5

    private val animDuration = context.resources.getInteger(R.integer.anim_duration_avatar).toLong()
    private val scaleConsumers = ArrayList<ScaleConsumerV5>()

    @ExperimentalCoroutinesApi
    private val onSizeAvatarBack = MutableStateFlow(0 to 0)

    @ExperimentalCoroutinesApi
    private val onSizeAvatarFront = MutableStateFlow(0 to 0)

    /**
     * Размер дочерних Views
     */
    private lateinit var viewSize: Pair<Int, Int>

    /**
     * Это минимальное значение высоты для rectBitmapVisible. Оно в scaleMax-раз
     * меньше высоты View. То есть это высота rectBitmapVisible при максимальном зуме.
     */
    private var bitmapVisibleHeightMin: Float = 0f

    /**
     * Во сколько раз высота показываемой части битмапы (в px) может быть меньше высоты View (в px).
     * Это как бы максимальный зум в px. Загружается из R.dimen.bitmap_scale_max.
     */
    private var bitmapZoomMax: Float = Float.MIN_VALUE

    /**
     * Видимая часть Bitmap (в координатах bitmap)
     */
    private var rectBitmapVisible = Rect()

    /**
     * Область для показа картинки на поверхности дочерней View.
     */
    private val rectViewPort = Rect()

    override var bitmapScaleCurrent: Float = 1f
    override var bitmapScaleMin: Float = 0f

    init {
        View.inflate(context, R.layout.layout_avatar_components_v5, this).also { view ->
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

    /**
     * Подписка на извещения от дочерних элементов об окончании обработки onSizeChanged.
     * После этого им можно отдать bitmap'у.
     */
    @ExperimentalCoroutinesApi
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Максимальное значение для zoom'а
        val typedValue = TypedValue()
        resources.getValue(R.dimen.bitmap_zoom_max, typedValue, true)
        bitmapZoomMax = typedValue.float

        // Ожидать готовности дочерних элементов
        (context as AppCompatActivity).lifecycleScope.launch {
            combine(onSizeAvatarBack, onSizeAvatarFront) { backSize, frontSize ->
                listOf(backSize, frontSize)
            }.filter { list ->
                list.all { it.notZero }
            }.collect {
                viewSize = it[0]
                measureComponents()
            }
        }
    }

    /**
     * Определить оригинальный размер битмапы и её ориентацию относительно положения экрана.
     * В этом положении выделить для её показа область на экране (viewPortWidth/viewPortHeight).
     * Загрузить битмапу, расчитать rectDest и rectVisible.
     */
    private fun measureComponents() {

        measureAndLayoutViewPort(IMAGE_ID)

        // Битмапа загруженная как Sampled сохранит свои пропорции
        val bitmap = loadSampledBitmapFromResource(
            IMAGE_ID,
            rectViewPort.width(),
            rectViewPort.height()
        )

        rectBitmapVisible = Rect(0, 0, bitmap.width, bitmap.height)

        // Это минимальное значение высоты для rectBitmapVisible. Оно в bitmapScaleMax-раз
        // меньше высоты View. То есть это высота rectBitmapVisible при максимальном увеличении.
        bitmapVisibleHeightMin = rectViewPort.height() / bitmapZoomMax

        // Минимальное значение для скалирования размера видимой части битмапы.
        bitmapScaleMin = bitmapVisibleHeightMin / bitmap.height

        val mediaData = MediaData(
            bitmap,
            rectViewPort,
            bitmapVisibleHeightMin,
            bitmapScaleMin
        )

        avatarBack.onBitmapReady(mediaData)
        avatarFront.onBitmapReady(mediaData)
    }

    /**
     * 1. Определить размеры оригинальной битмапы.
     * 2. Определить положение битмапы относительно ориентации View (Wider/Higher/Same).
     * 3. Отскалировать размеры битмапы, чтобы она без искажений помещалась внутри View.
     * 4. В процессе скалирования инициализировать ViewPort (rectViewPort)
     */
    private fun measureAndLayoutViewPort(@DrawableRes resourceId: Int = IMAGE_ID) {

        val (viewWidth, viewHeight) = viewSize

        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, this)
        }

        val ratioW = viewWidth.toFloat() / bitmapOptions.outWidth
        val ratioH = viewHeight.toFloat() / bitmapOptions.outHeight

        when {
            // Wider: Горизонтальная фотка в вертикальной View
            (viewHeight > viewWidth && bitmapOptions.outHeight <= bitmapOptions.outWidth) -> {

                scaleHeightByHorizontalRatio(ratioW, bitmapOptions.outHeight)


            }
            // Higher: Вертикальная фотка в горизонтальной View
            (viewWidth > viewHeight && bitmapOptions.outHeight >= bitmapOptions.outWidth) -> {
                scaleWidthByVerticalRatio(ratioH, bitmapOptions.outWidth)

            }
            // Same: Ориентации фотки и View совпадают. Обе горизонтальные или обе вертикальные.
            else -> {

                if (ratioW <= ratioH) {
                    scaleHeightByHorizontalRatio(ratioW, bitmapOptions.outHeight)
                } else {
                    scaleWidthByVerticalRatio(ratioH, bitmapOptions.outWidth)
                }

            }
        }
    }


    /**
     * На входе ratio для скалирования высоты битмапы при котором она займет всю высоту View.
     * Нужно изменить ширину битмапы (сжать/растянуть) в соответствии с ratio. Полученное значение
     * не будет больше viewWidth.
     */
    private fun scaleWidthByVerticalRatio(ratio: Float, widthToScale: Int) {
        val (viewWidth, viewHeight) = viewSize

        val scaledWidth = (widthToScale * ratio).toInt()

        rectViewPort.apply {
            top = 0
            bottom = viewHeight
            left = ((viewWidth - scaledWidth) / 2f).toInt()
            right = left + scaledWidth
        }
    }

    /**
     * На входе ratio для скалирования ширины битмапы при котором она займет всю ширину View.
     * Нужно изменить высоту битмапы (сжать/растянуть) в соответствии с ratio. Полученное значение
     * не будет больше viewHeight.
     */
    private fun scaleHeightByHorizontalRatio(ratio: Float, heightToScale: Int) {

        val (viewWidth, viewHeight) = viewSize

        val scaledHeight = (heightToScale * ratio).toInt()

        rectViewPort.apply {
            left = 0
            right = viewWidth
            top = ((viewHeight - scaledHeight) / 2f).toInt()
            bottom = top + scaledHeight
        }
    }

    /**
     * Дочерний элемент просит запустить анимацию
     * @param factor - scale factor
     * @param pivot - фокус скалирования в координатах view
     */
    fun onScaleRequiredOld(factor: Float, pivot: PointF) {

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

    @ExperimentalCoroutinesApi
    override fun onScaleRequired(factor: Float, pivot: PointF) {

        // Действия во время каждой итерации
        val onUpdate: ValueAnimator.() -> Unit = {
            val fraction = this.animatedFraction
            scaleConsumers.forEach { it.onAnimate(fraction) }
            scaleConsumers.forEach { (it as View).invalidate() }
        }

        (context as AppCompatActivity).lifecycleScope.launch {

            // Подготовиться к началу анимации в дочерних Views
            scaleConsumers.forEach { it.onPreAnimate(factor, pivot) }

            // Запустить анимацию
            val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = animDuration
            }
            animator.start()

            // Ждать завершения suspend анимации, а потом...
            animator.awaitEnd(onUpdate)

            // ... вызвать у всех onPostScale()
            scaleConsumers.forEach { it.onPostAnimate() }
        }
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


    @ExperimentalCoroutinesApi
    override fun onFrontSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarFront.value = size
    }

    @ExperimentalCoroutinesApi
    override fun onBackSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarBack.value = size
    }
}