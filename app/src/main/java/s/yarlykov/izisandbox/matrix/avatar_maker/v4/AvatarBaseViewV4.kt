package s.yarlykov.izisandbox.matrix.avatar_maker.v4

import android.animation.FloatEvaluator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.LightingColorFilter
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker.MediaDataConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker.ScaleController

abstract class AvatarBaseViewV4 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), MediaDataConsumer, ScaleConsumer {

    /**
     * Цветовые фильтры поярче/потемнее.
     */
    protected val colorFilterLighten = LightingColorFilter(0xFFFFFFFF.toInt(), 0x00222222)
    protected val colorFilterDarken = LightingColorFilter(0xFF7F7F7F.toInt(), 0x00000000)

    protected val animDuration =
        context.resources.getInteger(R.integer.anim_duration_avatar).toLong()

    /**
     * @scaleMax -  начальный масштаб rectBitmapVisible.height относительно sourceImageBitmap.height
     * @scaleMin -  это такой зум, при котором rectBitmapVisible.height меньше
     *              rectVisible.height в 3 раза.
     * @scaleCurrent - это значение в диапазоне 'scaleMax -> scaleMin -> scaleMax'.
     */
    protected var scaleMax = 1f         // Максимальный масштаб
    protected var scaleCurrent = 1f     // Текущий масштаб
    protected var scaleMin = 1f

    // Это минимальное значение высоты для rectBitmapVisible. Оно в scaleMax-раз
    // меньше высоты View. То есть это высота rectBitmapVisible при максимальном зуме.
    var rectBitmapVisibleHeightMin : Float = 0f
    // Это показатель того как битмапа отзумилась внутри View при загрузке. Это соотношение
    // высоты целевой View к высоте битмапы.
    var viewHeightToBitmapHeightRatio: Float = 0f


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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        // Максимальное значение для zoom'а
        val typedValue = TypedValue()
        resources.getValue(R.dimen.bitmap_scale_max, typedValue, true)
        scaleMax = typedValue.float
    }

    /**
     * Для AvatarBackView достаточно этих операцию + invalidate.
     * Для AvatarFrontView нужны дополнительные вычисления + invalidate.
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rectDestUpdate()
        rectVisibleUpdate()
    }

    /**
     * Растянуть по высоте с ratio.
     *
     * При любой ориентации "натягиваем" видимую часть битмапы по высоте view. При этом,
     * часть битмапы может оказаться за пределами боковых границ view. Но это фигня.
     * Главное, что нет искажений от растяжки/сжатия.
     */
    private fun rectDestUpdate() {

        val ratio = height.toFloat() / rectBitmapVisible.height()
        val scaledWidth = (rectBitmapVisible.width() * ratio).toInt()

        rectDest.apply {
            top = 0
            bottom = this@AvatarBaseViewV4.height
            left = ((this@AvatarBaseViewV4.width - scaledWidth) / 2f).toInt()
            right = left + scaledWidth
        }
    }

    /**
     * Зависит от rectDest
     */
    private fun rectVisibleUpdate() {
        rectVisible.apply {
            top = 0
            bottom = this@AvatarBaseViewV4.height
            left =
                if (rectDest.left <= 0) 0 else rectDest.left
            right =
                if (rectDest.right >= this@AvatarBaseViewV4.width) this@AvatarBaseViewV4.width else rectDest.right
        }
    }

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
    override fun onBitmapReady(bitmap: Bitmap) {
        sourceImageBitmap = bitmap
        rectBitmapVisible = Rect(0, 0, bitmap.width, bitmap.height)

        // Начальный зум - показатель того как битмапа отзумилась внутри View при загрузке.
        // Это соотношение высоты целевой View к высоте битмапы.
        viewHeightToBitmapHeightRatio = rectVisible.height().toFloat() / rectBitmapVisible.height()

        // Это минимальное значение высоты для rectBitmapVisible. Оно в scaleMax-раз
        // меньше высоты View. То есть это высота rectBitmapVisible при максимальном зуме.
        rectBitmapVisibleHeightMin = rectVisible.height().toFloat() / scaleMax

        // Это минимальный масштаб для rectBitmapVisible внутри БИТМАПЫ. Поясняю: при первой
        // загрузке битмапы rectBitmapVisible совпадает по размеру с размером битмапы и
        // соответственно зум этого прямоугольника относительно битмапы равен 1.
        // Когда высота rectBitmapVisible уменьшится до rectBitmapVisibleHeightMin, то его
        // масштаб от начального 1 будет равен scaleMin. А в промежуточных состояниях он будет
        // меняться от 1 до scaleMin и обратно к 1.
        scaleMin = rectBitmapVisibleHeightMin / rectBitmapVisible.height().toFloat()

        // Промежуточное значение между scaleMax и scaleMin. При первой загрузке равно 1 (scaleMax)
        scaleCurrent = scaleMax
    }

    /**
     * Код для Scale и имплементация ScaleConsumer
     */
    var scaleController: ScaleController? = null
    protected val evaluator = FloatEvaluator()
    protected var isScaleDownAvailable = true
    protected var isScaleUpAvailable = true

    protected var scaleFrom = 1f
    protected var scaleTo = 1f

    override fun onScaleDownAvailable(isAvailable: Boolean) {
        isScaleDownAvailable = isAvailable
    }

    override fun onScaleUpAvailable(isAvailable: Boolean) {
        isScaleUpAvailable = isAvailable
    }
}