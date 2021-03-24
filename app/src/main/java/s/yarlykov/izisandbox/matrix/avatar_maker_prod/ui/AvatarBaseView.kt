package s.yarlykov.izisandbox.matrix.avatar_maker_prod.ui

import android.animation.FloatEvaluator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.LightingColorFilter
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.MediaData
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.media.MediaDataConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale.ScaleConsumer
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.scale.ScaleController
import s.yarlykov.izisandbox.utils.logIt

abstract class AvatarBaseView @JvmOverloads constructor(
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
     * Область для показа картинки на поверхности View.
     */
    protected val rectViewPort = Rect()

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

        rectViewPort.set(mediaData.viewPort)

        // Это минимальное значение высоты для rectBitmapVisible. Оно в bitmapScaleMax-раз
        // меньше высоты View. То есть это высота rectBitmapVisible при максимальном увеличении.
        bitmapVisibleHeightMin = mediaData.bitmapVisibleHeightMin

        // Минимальное значение для скалирования размера видимой части битмапы.
        bitmapScaleMin = mediaData.bitmapScaleMin
    }

    /**
     * Код для Scale и имплементация ScaleConsumer
     */
    lateinit var scaleController: ScaleController

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