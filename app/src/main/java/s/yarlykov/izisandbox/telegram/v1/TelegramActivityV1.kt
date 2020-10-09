package s.yarlykov.izisandbox.telegram.v1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.dimensionPix
import s.yarlykov.izisandbox.extensions.setRoundedDrawable
import kotlin.math.max
import kotlin.math.min

class TelegramActivityV1 : AppCompatActivity() {

    lateinit var collapsingToolbar: CollapsingToolbarLayout
    lateinit var roundedDrawable: RoundedBitmapDrawable
    lateinit var appBar: AppBarLayout
    lateinit var ivAvatar: ImageView
    lateinit var tvLog: TextView
    lateinit var toolBar: Toolbar

    var cornerRadiusMax = 0f
    var step = 1f

    private val interpolatorCorners = AccelerateInterpolator(3f)
    private val interpolatorLayout = AccelerateInterpolator(1.2f)

    private var initStartMargin = 0
    private var initBottomMargin = 0
    private var initDiameter = 0

    private var scrollMax = 1
        set(value) {
            step = cornerRadiusMax / value
            field = value
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telegram)

        initStartMargin = dimensionPix(R.dimen.margin_rounded)
        initBottomMargin = dimensionPix(R.dimen.margin_rounded_bottom)
        initDiameter = dimensionPix(R.dimen.circle_diameter)

        findView()
    }

    override fun onResume() {
        super.onResume()
        scrollMax = max(scrollMax, appBar.totalScrollRange)
    }

    private fun findView() {
        tvLog = findViewById(R.id.tv_logging)
        appBar = findViewById(R.id.appbar_layout)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        toolBar = findViewById(R.id.toolbar_telegram)

        appBar.addOnOffsetChangedListener(listener)

        ivAvatar = findViewById(R.id.iv_rounded)
        ivAvatar.setRoundedDrawable(R.drawable.valery)

        roundedDrawable = ivAvatar.drawable as RoundedBitmapDrawable
        cornerRadiusMax = roundedDrawable.cornerRadius

        appBar.setExpanded(false)
    }

    /**
     * verticalOffset также устанавливает значение для step
     */
    private val listener =
        AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->

            scrollMax = max(scrollMax, appBar.totalScrollRange)

            val progress = (scrollMax + verticalOffset).toFloat() / scrollMax.toFloat()

            updateAvatarCornerRadius(progress)
            updateAvatarStartMargin(progress)
            //            updateAvatarMarginAndLayout(verticalOffset)
            updateAvatarSize(verticalOffset, progress)
            updateAvatarVerticalMargin(verticalOffset)

            ivAvatar.requestLayout()
            ivAvatar.invalidate()

            if (::tvLog.isInitialized) {
                val t = "offset= $verticalOffset, scroll range = ${scrollMax}"
                tvLog.text = t
            }
        }

    /**
     * progress меняется от 0 (свернутое состояние) до 1 (развернутое состояние)
     *
     * p1: 0 -> 0.3
     * p2: 0 -> 1.0
     *
     * p2 = p1 * (1/0.3)
     */
    private fun updateAvatarCornerRadius(p1: Float) {

        if (p1 >= 0.3f) return

        val progress = p1 * (1f / 0.3f)

        // Коффициент интерполятора, на который нужно корректировать progress.
        //val kC: Float = interpolatorCorners.getInterpolation(1f + progress / scrollMax)
        roundedDrawable.cornerRadius = max(cornerRadiusMax * (1f - progress), 0f)
    }

    private fun updateAvatarStartMargin(p1 : Float) {

        if (p1 >= 0.3f) return

        val progress = p1 * (1f / 0.3f)

        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        val currentMarginStart = max(
            (initStartMargin * (1f - progress)).toInt(),
            0
        )

        avatarMarginParams.marginStart = currentMarginStart

        ivAvatar.layoutParams = avatarMarginParams
    }

    /**
     */
    private fun updateAvatarVerticalMargin(verticalOffset: Int) {
        val progress = (scrollMax + verticalOffset).toFloat() / scrollMax.toFloat()

        if (progress <= 0.3) {
            increaseVerticalMargins(verticalOffset, progress)
        } else {
            decreaseVerticalMargins(verticalOffset, progress)
        }
    }

    /**
     * работает при progress 0 -> 0.3
     *
     * уведичивает margins, чтобы держаться вертикально по центру
     */
    private fun increaseVerticalMargins(verticalOffset: Int, progress: Float) {

        val avatarLayoutParams = ivAvatar.layoutParams
        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        val currentMarginVertical =
            (toolBar.measuredHeight + (scrollMax + verticalOffset) - initDiameter) / 2f

        avatarMarginParams.bottomMargin = currentMarginVertical.toInt()

        ivAvatar.layoutParams = avatarLayoutParams
//        ivAvatar.requestLayout()
//        ivAvatar.invalidate()

    }

    /**
     * работает при progress 0.3 -> 1
     *
     * уменьшает margins, чтобы держаться вертикально по центру
     */
    private fun decreaseVerticalMargins(verticalOffset: Int, progress: Float) {

        val avatarLayoutParams = ivAvatar.layoutParams
        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams


        val currentMarginVertical =
            (toolBar.measuredHeight + (scrollMax + verticalOffset) - initDiameter) / 2f

        avatarMarginParams.bottomMargin = currentMarginVertical.toInt()

        ivAvatar.layoutParams = avatarLayoutParams
//        ivAvatar.requestLayout()
//        ivAvatar.invalidate()
    }


    /**
     * progress меняется от 0 (свернутое состояние) до 1 (развернутое состояние)
     *
     * p1: 0.3 -> 1.0
     * p2: 0 -> 1.0
     *
     * или так:
     *
     * p1: 0 -> 0.7
     * ps: 0 -> 1.0
     *
     * p2 = (p1 - 0.3) * (1/0.7)
     */
    private fun updateAvatarSize(verticalOffset: Int, p1: Float) {

        if (p1 <= 0.3f) return

        val progress = (p1 - 0.3) * (1/0.7)

        val parentWidth = appBar.measuredWidth
        val parentHeight = appBar.measuredHeight

        val widthDelta = parentWidth.toFloat() - initDiameter
        val heightDelta = parentHeight.toFloat() - initDiameter

        val avatarLayoutParams = ivAvatar.layoutParams

        // Меняем размер ivAvatar
        avatarLayoutParams.width = min(initDiameter + (widthDelta * progress).toInt(), parentWidth)
        avatarLayoutParams.height = min(initDiameter + (heightDelta * progress).toInt(), parentHeight)

    }

    /**
     * scrollOffset имеет отрицательное значение при расширении CollapsingToolbarLayout'а
     * и становится равен 0 в нижней точке. Поэтому здесь используется выражение:
     *      scrollMax + scrollOffset
     * фактически это вычитание.
     */
    private fun updateAvatarCornerRadiusV1(scrollOffset: Int) {

        val progress = scrollMax + scrollOffset

        // Коффициент интерполятора, на который нужно корректировать progress.
        val kC: Float =
            interpolatorCorners.getInterpolation(1f + progress / scrollMax)
        roundedDrawable.cornerRadius = max(cornerRadiusMax - progress * step * kC, 0f)
    }

    /**
     * progress меняется от 0 (свернутое состояние) до 1 (развернутое состояние)
     */
    private fun updateAvatarCornerRadiusV2(progress: Float) {

        // Коффициент интерполятора, на который нужно корректировать progress.
        //val kC: Float = interpolatorCorners.getInterpolation(1f + progress / scrollMax)
        roundedDrawable.cornerRadius = max(cornerRadiusMax * (1f - progress), 0f)
    }

    /**
     * progress меняется от 0 (свернутое состояние) до 1 (развернутое состояние)
     *
     * p1: 0.3 -> 1.0
     * p2: 0 -> 1.0
     *
     * или так:
     *
     * p1: 0 -> 0.7
     * ps: 0 -> 1.0
     *
     * p2 = (p1 - 0.3) * (1/0.7)
     */
    private fun updateAvatarMarginAndLayout(verticalOffset: Int) {
        val p1 = (scrollMax + verticalOffset).toFloat() / scrollMax.toFloat()

        if (p1 < 0.3f) return

        val progress = (p1 - 0.3f) * (1f / 0.7f)


        val parentWidth = appBar.measuredWidth
        val parentHeight = appBar.measuredHeight

        val widthDelta = parentWidth.toFloat() - initDiameter
        val heightDelta = parentHeight.toFloat() - initDiameter

//        val kL = interpolatorLayout.getInterpolation(1f + progress / 100f)

        val avatarLayoutParams = ivAvatar.layoutParams
        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        // Меняем margin'ы
        val currentMarginStart = max(
            (initStartMargin * (1f - progress)).toInt(),
            0
        )

        val currentMarginBottom =
            (toolBar.measuredHeight + (scrollMax + verticalOffset) - initDiameter) / 2f

//        val currentMarginBottom = max(
//            (initBottomMargin * (1f - progress)).toInt(),
//            0
//        )
        avatarMarginParams.marginStart = currentMarginStart
        avatarMarginParams.bottomMargin = currentMarginBottom.toInt()
        logIt("margins: start=$currentMarginStart, bottom=$currentMarginBottom")

        // Меняем размер ivAvatar
//        avatarLayoutParams.width = min(
//            initDiameter + (widthDelta * progress).toInt(), parentWidth
//        )
//        avatarLayoutParams.height = min(initDiameter + (heightDelta * progress).toInt(), parentHeight)
//
        ivAvatar.layoutParams = avatarLayoutParams
        ivAvatar.requestLayout()
        ivAvatar.invalidate()

    }

    private fun updateAvatarMarginAndLayoutV1(scrollOffset: Int) {
        val progress = scrollMax + scrollOffset

        val parentWidth = appBar.measuredWidth

        val widthDelta = parentWidth.toFloat() - initDiameter

        val kL = interpolatorLayout.getInterpolation(1f + progress.toFloat() / 100f)

        val avatarLayoutParams = ivAvatar.layoutParams
        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        // Меняем margin'ы
        val currentMarginStart = max(
            initStartMargin - ((initStartMargin.toFloat() * (progress.toFloat() / scrollMax.toFloat())) * kL).toInt(),
            0
        )
        val currentMarginBottom = max(
            initBottomMargin - ((initBottomMargin.toFloat() * (progress.toFloat() / scrollMax.toFloat())) * kL).toInt(),
            0
        )
        avatarMarginParams.marginStart = currentMarginStart
        avatarMarginParams.bottomMargin = currentMarginBottom

        // Меняем размер ivAvatar
        avatarLayoutParams.width = min(
            initDiameter + ((widthDelta * (progress.toFloat() / scrollMax.toFloat())) * kL).toInt(),
            parentWidth
        )
        avatarLayoutParams.height = avatarLayoutParams.width

        ivAvatar.layoutParams = avatarLayoutParams
        ivAvatar.requestLayout()
        ivAvatar.invalidate()

    }

    /**
     * progress меняется от 0 (свернутое состояние) до 1 (развернутое состояние)
     */
    private fun updateAvatarMarginAndLayoutV2(verticalOffset: Int) {
        val progress = (scrollMax + verticalOffset).toFloat() / scrollMax.toFloat()

        val parentWidth = appBar.measuredWidth
        val parentHeight = appBar.measuredHeight

        val widthDelta = parentWidth.toFloat() - initDiameter
        val heightDelta = parentHeight.toFloat() - initDiameter

//        val kL = interpolatorLayout.getInterpolation(1f + progress / 100f)

        val avatarLayoutParams = ivAvatar.layoutParams
        val avatarMarginParams = ivAvatar.layoutParams as ViewGroup.MarginLayoutParams

        // Меняем margin'ы
        val currentMarginStart = max(
            (initStartMargin * (1f - progress)).toInt(),
            0
        )

        val currentMarginBottom =
            (toolBar.measuredHeight + (scrollMax + verticalOffset) - initDiameter) / 2f

//        val currentMarginBottom = max(
//            (initBottomMargin * (1f - progress)).toInt(),
//            0
//        )
        avatarMarginParams.marginStart = currentMarginStart
        avatarMarginParams.bottomMargin = currentMarginBottom.toInt()
        logIt("margins: start=$currentMarginStart, bottom=$currentMarginBottom")

        // Меняем размер ivAvatar
//        avatarLayoutParams.width = min(
//            initDiameter + (widthDelta * progress).toInt(), parentWidth
//        )
//        avatarLayoutParams.height = min(initDiameter + (heightDelta * progress).toInt(), parentHeight)
//
        ivAvatar.layoutParams = avatarLayoutParams
        ivAvatar.requestLayout()
        ivAvatar.invalidate()

    }


}