package s.yarlykov.izisandbox.matrix.avatar_maker_prod.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.Button
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.*
import s.yarlykov.izisandbox.matrix.avatar_maker_dev.EditorAvatarActivity.Companion.IMAGE_ID
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.BitmapOptions
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.MediaData
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale.ScaleConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale.ScaleController
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm.AvatarViewModelAccessor
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm.AvatarViwModelInjector
import kotlin.math.abs

@ExperimentalCoroutinesApi
class AvatarCompoundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr),
    ScaleController,
    AvatarViewModelAccessor by AvatarViwModelInjector(context) {

    private val avatarBack: AvatarBaseView
    private val avatarFront: AvatarBaseView
    private val buttonRotate: Button
    private val buttonCancel: Button
    private val buttonReady: Button

    private val animDuration = context.resources.getInteger(R.integer.anim_duration_avatar).toLong()
    private val scaleConsumers = ArrayList<ScaleConsumer>()

    /**
     * Устанавливаются внешним кодом после inflate AvatarCompoundView
     */
    lateinit var bitmapPath: String
    lateinit var bitmapUri: Uri

    private val onSizeAvatarBack = MutableStateFlow(0 to 0)

    private val onSizeAvatarFront = MutableStateFlow(0 to 0)

    /**
     * Размер дочерних Views
     */
    private lateinit var childViewSize: Pair<Int, Int>

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
     * Область для показа картинки на поверхности дочерней View. Эта область ОХВАТЫВАЕТ
     * картинку, но не равна ей. Картинка будет совпадать размером только по одной стороне
     * rectViewPort, другая сторона будет меньше, но отскалирована в соответствии с пропорциями
     * битмапы.
     */
    private val rectViewPort = Rect()

    override var bitmapScaleCurrent: Float = 1f
    override var bitmapScaleMin: Float = 0f

    init {
        View.inflate(context, R.layout.layout_avatar_components_prod, this).also { view ->
            avatarBack = view.findViewById(R.id.avatarBack)
            avatarFront = view.findViewById(R.id.avatarFront)

            buttonRotate = view.findViewById(R.id.buttonRotate)
            buttonCancel = view.findViewById(R.id.buttonCancel)
            buttonReady = view.findViewById(R.id.buttonReady)

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
        viewModel.viewModelScope.launch {
            combine(onSizeAvatarBack, onSizeAvatarFront) { backSize, frontSize ->
                listOf(backSize, frontSize)
            }.filter { list ->
                list.all { it.notZero }
            }.collect {
                childViewSize = it[0]
                measureComponents()
            }
        }

        buttonRotate.setOnClickListener {
            rotateCcw()
        }

        buttonReady.setOnClickListener {
            viewModel.onReady()
        }

        buttonCancel.setOnClickListener {
            viewModel.onCancel()
        }
    }

    /**
     * Определить оригинальный размер битмапы и её ориентацию относительно положения экрана.
     * В этом положении выделить для её показа область на экране (viewPortWidth/viewPortHeight).
     * Загрузить битмапу, расчитать rectDest и rectVisible.
     */
    private fun measureComponents() {
        require(::bitmapPath.isInitialized) {
            "${this::class.simpleName}:${object {}.javaClass.enclosingMethod?.name} " +
                    "Illegal '${::bitmapPath.name}' value"
        }

        require(::bitmapUri.isInitialized) {
            "${this::class.simpleName}:${object {}.javaClass.enclosingMethod?.name} " +
                    "Illegal '${::bitmapUri.name}' value"
        }

        // Измерить исходную битмапу и ориентацию камеры
        val bitmapOptions = when {
            bitmapUri != Uri.EMPTY -> measureBitmap(bitmapUri)
            bitmapPath.isNotEmpty() -> measureBitmap(bitmapPath)
            else -> throw Throwable("Illegal bitmap path/uri")
        }

        // Измерить и спозиционировать viewPort
        measureAndLayoutViewPort(bitmapOptions)

        // Битмапа загруженная как Sampled сохранит свои пропорции. Yes !
        val bitmap = loadSampledBitmap(bitmapOptions)

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
     * Определить оригинальные размеры битмапы и ориентацию
     */
    private inline fun <reified T : Any> measureBitmap(source: T): BitmapOptions {

        return BitmapFactory.Options().run {
            inJustDecodeBounds = true

            when (source) {
                is String -> {
                    BitmapFactory.decodeFile(source, this)
                    BitmapOptions(
                        outWidth,
                        outHeight,
                        bitmapPath = source,
                        orientation = context.cameraOrientation(source)
                    )
                }
                is Uri -> {
                    context.contentResolver.openInputStream(source)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, this)
                    }
                    BitmapOptions(
                        outWidth,
                        outHeight,
                        bitmapUri = source,
                        orientation = context.cameraOrientation(source)
                    )
                }
                is Int -> {
                    BitmapFactory.decodeResource(context.resources, source, this)
                    BitmapOptions(outWidth, outHeight)
                }
                else -> throw Throwable(
                    "${this::class.simpleName}:${object {}.javaClass.enclosingMethod?.name} " +
                            "Illegal argument 'source'"
                )
            }
        }
    }

    /**
     * 1. Определить размеры оригинальной битмапы.
     * 2. Определить положение битмапы относительно ориентации View (Wider/Higher/Same).
     * 3. Отскалировать размеры битмапы, чтобы она без искажений помещалась внутри View.
     * 4. В процессе скалирования инициализировать ViewPort (rectViewPort)
     */
    private fun measureAndLayoutViewPort(bitmapOptions: BitmapOptions) {
        val (viewWidth, viewHeight) = childViewSize

        /**
         * Определить реальные w/h битмапы с учетом поворота камеры.
         * Это требуется чтобы правильно назначить w/h для viewPort'a.
         */
        val (bitmapWidth, bitmapHeight) = bitmapOptions.oriented

        val ratioW = viewWidth.toFloat() / bitmapWidth
        val ratioH = viewHeight.toFloat() / bitmapHeight

        when {
            // Wider: Горизонтальная фотка в вертикальной View
            (viewHeight > viewWidth && bitmapHeight <= bitmapWidth) -> {
                scaleHeightByHorizontalRatio(ratioW, bitmapHeight)
            }
            // Higher: Вертикальная фотка в горизонтальной View
            (viewWidth > viewHeight && bitmapHeight >= bitmapWidth) -> {
                scaleWidthByVerticalRatio(ratioH, bitmapWidth)
            }
            // Same: Ориентации фотки и View совпадают. Обе горизонтальные или обе вертикальные.
            else -> {

                if (ratioW <= ratioH) {
                    scaleHeightByHorizontalRatio(ratioW, bitmapHeight)
                } else {
                    scaleWidthByVerticalRatio(ratioH, bitmapWidth)
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
        val (viewWidth, viewHeight) = childViewSize

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

        val (viewWidth, viewHeight) = childViewSize

        val scaledHeight = (heightToScale * ratio).toInt()

        rectViewPort.apply {
            left = 0
            right = viewWidth
            top = ((viewHeight - scaledHeight) / 2f).toInt()
            bottom = top + scaledHeight
        }
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
     * Выбрать загружаемый источник и загрузить
     */
    private fun loadSampledBitmap(bitmapOptions: BitmapOptions): Bitmap {

        return when {
            bitmapOptions.bitmapUri != null -> loadSampledBitmapFromUri(
                bitmapOptions.bitmapUri,
                rectViewPort.width(),
                rectViewPort.height(),
                bitmapOptions.orientation
            )
            bitmapOptions.bitmapPath != null -> loadSampledBitmapFromFile(
                bitmapOptions.bitmapPath,
                rectViewPort.width(),
                rectViewPort.height(),
                bitmapOptions.orientation
            )
            else -> loadSampledBitmapFromResource(
                IMAGE_ID,
                rectViewPort.width(),
                rectViewPort.height()
            )
        }
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

            inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory.decodeResource(context.resources, resourceId, this)
        }
    }

    private fun loadSampledBitmapFromFile(
        path: String,
        reqWidth: Int,
        reqHeight: Int,
        orientation: Int
    ): Bitmap {

        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, this)
            val (rawWidth, rawHeight) = oriented(orientation)

            inSampleSize = calculateInSampleSize(rawWidth, rawHeight, reqWidth, reqHeight)

            inJustDecodeBounds = false
            BitmapFactory.decodeFile(path, this).rotate(orientation)
        }
    }

    private fun loadSampledBitmapFromUri(
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int,
        orientation: Int
    ): Bitmap {

        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, this)
            }
            val (rawWidth, rawHeight) = oriented(orientation)

            inSampleSize = calculateInSampleSize(rawWidth, rawHeight, reqWidth, reqHeight)

            inJustDecodeBounds = false
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, this)
            }?.rotate(orientation) ?: throw Throwable("Illegal file Uri '$uri'")
        }
    }

    /**
     * inSampleSize - это количество пикселей исходного изображения, которое соответствует
     * одному пикселю создаваемого (декодированного) изображения. Например, если
     * inSampleSize = 2, то декодированная картинка будет иметь размеры W/2 x H/2 и количество
     * пикселей W/2 * H/2 = (W*H)/4, то есть в 4 раза меньше, чем в исходном.
     */
    private fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {

        // Если картинка достаточно маленькая, то декодируем без изменений.
        if (rawHeight < reqHeight / 2 && rawWidth < reqWidth / 2) return 1

        // Для остальных случаев вычисляем.
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

    /**
     * Выполнить поворот бэка и фронта со скалированием.
     *
     * Когда устанавливаем значение view.rotation кратным 180, то это значит, что view окажется
     * в оригинальной ориентации или вверх ногами, то есть её размеры будут оригинальными и нужно
     * указать scale = 1. При другом значение rotation view "ляжет на бок" и её размеры нужно
     * отскалить по отношению view.width / view.height.
     */
    override fun rotateCcw() {
        val degreeCcw = -90f

        val pivotX = avatarBack.width.toFloat() / 2f
        val pivotY = avatarBack.height.toFloat() / 2f
        val rotation = avatarBack.rotation + degreeCcw

        val scale = if (abs(rotation).toInt() % 180 == 0)
            1f
        else
            avatarBack.width.toFloat() / avatarBack.height.toFloat()

        avatarBack.pivotX = pivotX
        avatarBack.pivotY = pivotY
        avatarFront.pivotX = pivotX
        avatarFront.pivotY = pivotY

        avatarBack
            .animate().rotation(rotation).scaleX(scale).scaleY(scale).setDuration(150).start()
        avatarFront
            .animate().rotation(rotation).scaleX(scale).scaleY(scale).setDuration(150).start()
    }
}