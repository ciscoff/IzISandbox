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
import s.yarlykov.izisandbox.extensions.awaitEnd
import s.yarlykov.izisandbox.extensions.notZero
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleConsumerV5
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleControllerV5
import s.yarlykov.izisandbox.utils.logIt

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

    //      NOTE: Видимая часть битмапы - это тот её Rect, который мы отправляем в канву.
    //
    //      Здесь прямоугольник из точек - это 'видимая часть битмапы' внутри rectDest.
    //      Прямоуольник из линий - это rectVisible, то что видит пользователь.
    //                                    Рис 2.
    //          Рис 1.                    Ширина rectVisible меньше
    //          rectVisible равен         ширины View. Зазор между
    //          размеру View              view и rect показан линиями.
    //          ________________           ____________
    //     ....|................|....     |............|
    //     ....|................|....     |............|
    //     ....|................|....   __|............|__
    //     ....|................|....     |............|
    //     ....|................|....     |............|
    //     ....|................|....     |............|
    //     ....|................|....   __|............|__
    //     ....|................|....     |............|
    //     ....|................|....     |............|
    //         ------------------         --------------

    /**
     * View и её Canvas находятся в единой системе координат, то есть у них общая база (0,0).
     * Канва игнорирует padding'и и работает по всей поверхности View. У кастомной View
     * нужно самостоятельно учитывать padding'и при рисовании. Например с помощью clipRect.
     *
     * @rectDest прямоугольник (в координатах канвы) куда будем заливать видимую часть Bitmap'ы.
     *
     * Для того чтобы не искажать картинку он может выходить краями за пределы видимой области
     * канвы и иметь отрицательные left/top.
     */
    private val rectDest = Rect()

    /**
     * @rectVisible это как бы clipping для области rectDest. То есть rectVisible говорит нам
     * какая часть rectDest отображается на View. rectVisible также задается в координатах канвы.
     *
     * NOTE: rectDest и rectVisible устанавливаются только один раз - при onSizeChanged и больше
     * не меняются, но rectVisible используется во всех последующих операциях как база и
     * ограничитель перемещаемой рамки.
     */
    private val rectVisible = Rect()

    /**
     * Это минимальное значение высоты для rectBitmapVisible. Оно в scaleMax-раз
     * меньше высоты View. То есть это высота rectBitmapVisible при максимальном зуме.
     */
    var bitmapVisibleHeightMin: Float = 0f

    /**
     * Во сколько раз высота показываемой части битмапы (в px) может быть меньше высоты View (в px).
     * Это как бы максимальный зум в px. Загружается из R.dimen.bitmap_scale_max.
     */
    private var bitmapZoomMax: Float = Float.MIN_VALUE

    /**
     * Исходная Bitmap
     */
    private var sourceImageBitmap: Bitmap? = null

    /**
     * Видимая часть Bitmap (в координатах bitmap)
     */
    private var rectBitmapVisible = Rect()

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

    /**
     * Подписка на извещения от дочерних элементов об окончании обработки onSizeChanged.
     * После этого им можно отдать bitmap'у.
     */
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
                onChildrenReady()
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

    private fun onChildrenReady() {

        val bitmap = loadSampledBitmapFromResource(R.drawable.m_4, width, height)

        sourceImageBitmap = bitmap
        rectBitmapVisible = Rect(0, 0, bitmap.width, bitmap.height)

        rectDestUpdate()
        rectVisibleUpdate()
        logIt("${this::class.simpleName}, rectDest=$rectDest, width=$width")
        logIt("${this::class.simpleName}, rectVisible=$rectVisible, width=$width")

        // Это минимальное значение высоты для rectBitmapVisible. Оно в bitmapScaleMax-раз
        // меньше высоты View. То есть это высота rectBitmapVisible при максимальном увеличении.
        bitmapVisibleHeightMin = rectVisible.height() / bitmapZoomMax

        // Минимальное значение для скалирования размера видимой части битмапы.
        bitmapScaleMin = bitmapVisibleHeightMin / bitmap.height

        val mediaData = MediaData(
            bitmap,
            rectDest,
            rectVisible,
            bitmapVisibleHeightMin,
            bitmapScaleMin
        )

        avatarBack.onBitmapReady(mediaData)
        avatarFront.onBitmapReady(mediaData)
    }

    /**
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" видимую часть битмапы по высоте view. При этом,
     * часть битмапы может оказаться за пределами боковых границ view. Но это фигня.
     * Главное, что нет искажений от растяжки/сжатия.
     */
    private fun rectDestUpdate() {

        val h = height - paddingTop - paddingBottom
        val w = width - paddingStart - paddingLeft

        val ratio = h.toFloat() / rectBitmapVisible.height()
        val scaledWidth = (rectBitmapVisible.width() * ratio).toInt()

        rectDest.apply {
            top = 0
            bottom = h
            left = ((w - scaledWidth) / 2f).toInt()
            right = left + scaledWidth
        }
    }

    /**
     * Зависит от rectDest
     */
    private fun rectVisibleUpdate() {
        val h = height - paddingTop - paddingBottom
        val w = width - paddingStart - paddingLeft

        rectVisible.apply {
            top = 0
            bottom = h
            left =
                if (rectDest.left <= 0) 0 else rectDest.left
            right =
                if (rectDest.right >= w) w else rectDest.right
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
}