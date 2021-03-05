package s.yarlykov.izisandbox.matrix.avatar_maker.v5

import android.animation.FloatEvaluator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.LightingColorFilter
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.utils.logIt

abstract class AvatarBaseViewV5 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MediaDataConsumerV5, ScaleConsumerV5 {

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    protected val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    protected val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    protected val animDuration =
        context.resources.getInteger(R.integer.anim_duration_avatar).toLong()

    /**
     * Во сколько раз высота показываемой части битмапы (в px) может быть меньше высоты View (в px).
     * Это как бы максимальный зум в px. Загружается из R.dimen.bitmap_scale_max.
     */
//    private var bitmapZoomMax: Float = Float.MIN_VALUE

    /**
     * Это как бы противоположная bitmapZoomMax'у величина. Она показывает минимальное
     * значение scale, то есть значение bitmapVisibleHeightMin / sourceImageBitmap.height
     */
    private var bitmapScaleMin: Float = 0f

    /**
     * Это минимальное значение высоты для rectBitmapVisible. Оно в scaleMax-раз
     * меньше высоты View. То есть это высота rectBitmapVisible при максимальном зуме.
     */
    var bitmapVisibleHeightMin: Float = 0f

    /**
     * View и её Canvas находятся в единой системе координат, то есть у них общая база (0,0).
     * Канва игнорирует padding'и и работает по всей поверхности View. У кастомной View
     * нужно самостоятельно учитывать padding'и при рисовании. Например с помощью clipRect.
     *
     * @rectDest прямоугольник (в координатах канвы) куда будем заливать видимую часть Bitmap'ы.
     *
     * Для того чтобы не искажать картинку
     * Он может выходить краями за пределы видимой области канвы и иметь отрицательные left/top.
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
    protected val rectVisible = Rect()

    //      Здесь прямоугольник из точек - это видимая часть битмапы внутри rectDest.
    //      Прямойгольник из линий - это rectVisible, то что видит пользователь.
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
     * Исходная Bitmap и её размеры
     *
     * sourceImageBitmap - битмапа
     * rectSourceImage - прямоугольник видимой части битмапы в координатах БИТМАПЫ
     */
    protected var sourceImageBitmap: Bitmap? = null
    protected var rectBitmapVisible = Rect()

    /**
     * Когда битмапа готова, то нужно сразу вычислить как она 'отзумилась' внутри rectVisible.
     * Это будет её начальным зумом. От него будем идти в большую/меньшую стороны.
     *
     * @scaleBase не должен превышать scaleMax
     *
     * @heightMin - минимальная высота для прямоугольника rectBitmapVisible. При этой высоте
     * битмапа будет в максимальном зуме.
     *
     */
    override fun onBitmapReady(mediaData: MediaData) {
        sourceImageBitmap = mediaData.bitmap
        rectBitmapVisible = Rect(0, 0, mediaData.bitmap.width, mediaData.bitmap.height)

        rectDest.set(mediaData.rectDest)
        rectVisible.set(mediaData.rectVisible)

        logIt("${this::class.simpleName}, rectDest=$rectDest, width=$width")
        logIt("${this::class.simpleName}, rectVisible=$rectVisible, width=$width")

        // Это минимальное значение высоты для rectBitmapVisible. Оно в bitmapScaleMax-раз
        // меньше высоты View. То есть это высота rectBitmapVisible при максимальном увеличении.
        bitmapVisibleHeightMin = mediaData.bitmapVisibleHeightMin

        // Минимальное значение для скалирования размера видимой части битмапы.
        bitmapScaleMin = mediaData.bitmapScaleMin
    }

    /**
     * Код для Scale и имплементация ScaleConsumer
     */
    lateinit var scaleController: ScaleControllerV5

    protected val evaluator = FloatEvaluator()
    protected var animationScaleDownAvailable = false
    protected var animationScaleUpAvailable = false

    protected var scaleFrom = 1f
    protected var scaleTo = 1f

    override fun onScaleDownAvailable(isAvailable: Boolean) {
        animationScaleDownAvailable = isAvailable
    }

    override fun onScaleUpAvailable(isAvailable: Boolean) {
        animationScaleUpAvailable = isAvailable
    }
}