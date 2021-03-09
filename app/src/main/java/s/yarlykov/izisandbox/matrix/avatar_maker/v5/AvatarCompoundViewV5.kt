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
import s.yarlykov.izisandbox.matrix.avatar_maker.BitmapParams
import s.yarlykov.izisandbox.matrix.avatar_maker.BitmapViewRelation.*
import s.yarlykov.izisandbox.matrix.avatar_maker.EditorAvatarActivity.Companion.IMAGE_ID
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.max

class AvatarCompoundViewV5 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), ScaleControllerV5 {

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
     * Ориентация битмапы относительно текущей ориентации экрана
     */
    private lateinit var bitmapParams: BitmapParams

    //      rectDest и rectVisible:
    //      --------------------------
    //      NOTE: 'Видимая часть битмапы' - это тот её Rect, который мы отправляем в канву.
    //
    //      rectDest:
    //      При первой отрисовке битмапа "распечатывается" целиком, но скалируется привязываясь
    //      к высоте viewPort'a. А это значит, что например её ширина может оказаться
    //      шире viewPort'a, что и показано на Рис.1. То есть rectDest - это есть область
    //      начально отрисованной битмапы.
    //
    //      rectVisible:
    //      Это область на экране внутри которой мы ввидим изображение. Она может совпадать с
    //      viewPort'ом или быть уже (например если битмапа сама по себе узкая и отскалировалась
    //      уже viewPort'а)
    //
    //      Здесь прямоугольник из точек - это ВСЯ битмапа, то есть rectDest.
    //      Прямоуольник из линий - это rectVisible, то что видит пользователь.
    //                                    Рис 2.
    //          Рис 1.                    Ширина rectVisible меньше
    //          rectVisible равен         ширины View (viewPort'а). Зазор между
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

    @ExperimentalCoroutinesApi
    override fun onFrontSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarFront.value = size
    }

    @ExperimentalCoroutinesApi
    override fun onBackSizeChanged(size: Pair<Int, Int>) {
        onSizeAvatarBack.value = size
    }

    /**
     * Определить оригинальный размер битмапы и её ориентацию относительно положения экрана.
     * В этом положении выделить для её показа область на экране (viewPortWidth/viewPortHeight).
     * Загрузить битмапу, расчитать rectDest и rectVisible.
     */
    private fun measureComponents() {

        bitmapParams = measureBitmap(IMAGE_ID)

        val bitmap = loadSampledBitmapFromResource(
            IMAGE_ID,
            bitmapParams.viewPortWidth,
            bitmapParams.viewPortHeight
        )

        rectBitmapVisible = Rect(0, 0, bitmap.width, bitmap.height)

        measureRectDest()
        measureRectVisible()

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
     * Определить размеры оригинальной битмапы и её положение относительно ориентации View.
     * Нормализовать размер битмапы по одной из сторон View (если ориентации разные).
     */
    private fun measureBitmap(@DrawableRes resourceId: Int = IMAGE_ID): BitmapParams {

        val (viewWidth, viewHeight) = viewSize

        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(context.resources, resourceId, this)
        }

        return when {
            // Wider: Горизонтальная фотка в вертикальной View
            (viewHeight > viewWidth && bitmapOptions.outHeight <= bitmapOptions.outWidth) -> {
                val ratio = viewWidth.toFloat() / bitmapOptions.outWidth
                BitmapParams(
                    Wider,
                    bitmapOptions.outWidth,
                    bitmapOptions.outHeight,
                    viewWidth,
                    (bitmapOptions.outHeight * ratio).toInt()
                )
            }
            // Higher: Вертикальная фотка в горизонтальной View
            (viewWidth > viewHeight && bitmapOptions.outHeight >= bitmapOptions.outWidth) -> {
                val ratio = viewHeight.toFloat() / bitmapOptions.outHeight
                BitmapParams(
                    Higher,
                    bitmapOptions.outWidth,
                    bitmapOptions.outHeight,
                    (bitmapOptions.outWidth * ratio).toInt(),
                    viewHeight
                )
            }
            // Same: Ориентации фотки и View совпадают. Обе горизонтальные или обе вертикальные.
            else -> {
                BitmapParams(
                    Same,
                    bitmapOptions.outWidth,
                    bitmapOptions.outHeight,
                    viewWidth,
                    viewHeight
                )
            }
        }
    }


    /**
     * Сжать
     */
    private fun squeezeBitmap(ratio : Float, inHeight : Boolean) {
        val (viewPortWidth, viewPortHeight) = bitmapParams.viewPortWidth to bitmapParams.viewPortHeight
        val (viewWidth, viewHeight) = viewSize

        if(inHeight) {
            val scaledHeight = (rectBitmapVisible.height() * ratio).toInt()

            rectDest.apply {
                left = 0
                right = viewPortWidth
                top = ((viewHeight - scaledHeight) / 2f).toInt()
                bottom = top + scaledHeight
            }
        }
        else {
            val scaledWidth = (rectBitmapVisible.width() * ratio).toInt()

            rectDest.apply {
                top = 0
                bottom = viewPortHeight
                left = ((viewWidth - scaledWidth) / 2f).toInt()
                right = left + scaledWidth
            }
        }

        logIt("rectDest=$rectDest, w=${rectDest.width()}, h=${rectDest.height()}")
    }

    /**
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" видимую часть битмапы по высоте viewPort'a. При этом,
     * часть битмапы может оказаться за пределами боковых границ view. Но это фигня.
     * Главное, что нет искажений от растяжки/сжатия.
     */
    private fun measureRectDest() {

        /**
         * В этом месте viewPort и битмапа одинаково сориентированы, поэтому
         * не нужно делать дополнительных проверок, а нужно просто битмапу
         * притянуть к высоте viewPort'a.
         */

        val (viewPortWidth, viewPortHeight) = bitmapParams.viewPortWidth to bitmapParams.viewPortHeight
        val (viewWidth, viewHeight) = viewSize

        when (bitmapParams.relation) {
            Same -> {

                val ratioW = viewPortWidth.toFloat() / rectBitmapVisible.width()
                val ratioH = viewPortHeight.toFloat() / rectBitmapVisible.height()
                logIt("view w/h = $viewWidth,$viewHeight; viewPort w/h = $viewPortWidth, $viewPortHeight; ratioW=$ratioW, ratioH=$ratioH; bitmap w/h = ${rectBitmapVisible.width()}, ${rectBitmapVisible.height()}")

                when {
                    /**
                     * viewPort внутри битмапы, т.е. битмапу нужно сжать. Сжатие делаем
                     * по меньшей ratio
                     */
                    ratioW < 1f && ratioH < 1f -> {
                        if(ratioW > ratioH) {
                            squeezeBitmap(ratioH, false)
                        } else {
                            squeezeBitmap(ratioW, true)
                        }
                    }
                    /**
                     * viewPort вокруг битмапы, т.е. битмапу нужно растянуть. Растяжение делаем
                     * по меньшей ratio.
                     */
                    ratioW > 1f && ratioH > 1f -> {

                    }

                    /**
                     * Битмапа по высоте выше viewPort'a. Нужно сжать битмапу по высоте.
                     */
                    ratioW > 1f && ratioH < 1f -> {

                    }

                    /**
                     * Битмапа по ширине шире viewPort'a. Нужно сжать битмапу по ширине.
                     */
                    ratioW < 1f && ratioH > 1f -> {

                    }
                }


                // Ближе по высоте (нормализуем по высоте)
                if (ratioH <= ratioW) {
                    val scaledWidth = (rectBitmapVisible.width() * ratioH).toInt()

                    rectDest.apply {
                        top = 0
                        bottom = viewPortHeight
                        left = ((viewPortWidth - scaledWidth) / 2f).toInt()
                        right = left + scaledWidth
                    }
                }
                // Ближе по ширине (нормализуем по ширине)
                else /*(ratioW > ratioH)*/ {
                    val scaledHeight = (rectBitmapVisible.height() * ratioW).toInt()


                    rectDest.apply {
                        left = 0
                        right = viewPortWidth
                        top = ((viewHeight - scaledHeight) / 2f).toInt()
                        bottom = top + scaledHeight
                    }
                }
            }
            // Нормализуем по высоте viewPort'a
            Higher -> {
                val ratio = viewPortHeight.toFloat() / rectBitmapVisible.height()
                val scaledWidth = (rectBitmapVisible.width() * ratio).toInt()

                rectDest.apply {
                    left = ((viewWidth - scaledWidth) / 2f).toInt()
                    right = left + scaledWidth
                    top = 0
                    bottom = viewPortHeight
                }
            }
            // Нормализуем по ширине viewPort'a
            Wider -> {
                val ratio = viewPortWidth.toFloat() / rectBitmapVisible.width()
                val scaledHeight = (rectBitmapVisible.height() * ratio).toInt()

                rectDest.apply {
                    left = 0
                    right = viewPortWidth
                    top = ((viewHeight - scaledHeight) / 2f).toInt()
                    bottom = top + scaledHeight
                }
            }
        }
    }

    /**
     * Зависит от rectDest
     */
    private fun measureRectVisible() {

        val (viewPortWidth, viewPortHeight) = bitmapParams.viewPortWidth to bitmapParams.viewPortHeight
        val (viewWidth, viewHeight) = viewSize

        when (bitmapParams.relation) {
            Same, Higher -> {
                rectVisible.apply {
                    top = 0
                    bottom = viewPortHeight
                    left =
                        if (rectDest.left <= 0) 0 else rectDest.left
                    right =
                        if (rectDest.right >= viewPortWidth) viewPortWidth else rectDest.right
                }
            }
            Wider -> {

                (viewHeight - viewPortHeight) / 2

                rectVisible.apply {
                    left = 0
                    right = viewPortWidth
                    top = (viewHeight - viewPortHeight) / 2
                    bottom = top + viewPortHeight
                }

                rectVisible.intersect(rectDest)
            }
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