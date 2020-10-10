package s.yarlykov.izisandbox.telegram.v2

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import s.yarlykov.izisandbox.extensions.setRoundedDrawable
import kotlin.math.abs
import kotlin.math.max

class ActionBarLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    enum class AnimationType {
        Collapse,
        Expand
    }

    companion object {
        const val animationDuration = 150L
        const val progressMax = 100f
        const val progressMin = 0f
        const val progressThreshold = 40

        const val dYMin = -160f
        const val dYMax = -460f
    }

    private lateinit var roundedDrawable: RoundedBitmapDrawable
    private lateinit var ivAvatar: ImageView

    /**
     * Размер аватарки в свернутом состоянии
     */
    private var avatarCollapsedSize = 0f

    /**
     * При сворачивании показывает размер от которого картинка уменьшается, при разворачивании
     * показывает максимальный размер к которому стремимся.
     */
    private var wMax = 0
    private var hMax = 0

    /**
     * Анимация выполняется/не_выполняется
     */
    private var isAnimating = false

    /**
     * Флаг аватар свернут/развернут
     */
    private var isAvatarCollapsing = false

    /**
     * Флаг AppBar свернут/развернут
     */
    private var isAppBarCollapsing = true

    /**
     * Смещение по Y, которое устанавливается в матрице трансформаций
     */
    private var dY = 0f

    /**
     * Габариты RoundedBitmapDrawable
     */
    private var drawableWidth = 0
    private var drawableHeight = 0

    /**
     * Скролимся или меняем высоту AppBar'а
     */
    var scrollingAllowed: Boolean = false
        private set

    /**
     * Текущий прогресс изменения высоты.
     * Диапазон изменений от 100 до 0
     */
    private var progressManual = progressMax

    /**
     * Последнее значение из ValueAnimator'а
     * Диапазон изменений от 1 до 0
     */
    private var progressAnimator = 0f

    /**
     * Высота системного элемента statusBar
     */
    private var statusBarHeight = 0

    private val minHeight: Float
    private val maxHeight: Float
    private val scrollRange: Float
        get() = maxHeight - minHeight

    /**
     * Текущее скалирование аватара.
     * Высота ImageView меняется и при каждом изменении нужно знать значение scale.
     */
    private val currentScale: Float
        get() {
            val viewWidth: Float = getImageViewWidth(ivAvatar).toFloat()
            val viewHeight: Float = getImageViewHeight(ivAvatar).toFloat()

            val widthScale = viewWidth / drawableWidth
            val heightScale = viewHeight / drawableHeight

            return max(widthScale, heightScale)/*widthScale.coerceAtLeast(heightScale)*/
        }

    init {
        maxHeight = context.resources.getDimension(R.dimen.action_bar_max_height)
        minHeight = context.resources.getDimension(R.dimen.avatar_min_height)
        avatarCollapsedSize = resources.getDimension(R.dimen.avatar_collapsed)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ivAvatar = findViewById<ImageView>(R.id.ivAvatar).apply {
            setRoundedDrawable(R.drawable.valery)
            setOnApplyWindowInsetsListener { _, insets ->
                statusBarHeight = insets.systemWindowInsetTop
                insets.consumeSystemWindowInsets()
            }
        }

        roundedDrawable = ivAvatar.drawable as RoundedBitmapDrawable
        roundedDrawable.cornerRadius = 0f
        drawableWidth = roundedDrawable.intrinsicWidth
        drawableHeight = roundedDrawable.intrinsicHeight

        ivAvatar.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->

            logIt("addOnLayoutChangeListener: dY=$dY", TAG_DEBUG)

            /**
             * В зависимости от того запущена анимация или нет вызываем разные функции трансформации.
             * manualTransform (Up/Down) - анимация не запущена, прогресс получаем от скроллинга.
             * animatedTransform - анимация запущена, прогресс получаем из ValueAnimator
             */
            when {
                isAnimating -> animatedTransform(progressAnimator)
                isAppBarCollapsing -> manualTransformUp()
                else -> manualTransformDown()
            }
        }
    }

    /**
     * Палец тянут вверх
     *
     * NOTE: скроллинг контента становится возможным только если пальцем дотолкали AppBar
     * до минимальной высоты. Тогда скроллинг открывается и можно продолжать прокрутку
     * контента вверх. То есть в этот момент мы как бы "соскакиваем" с зацепа.
     */
    private fun siblingScrollingUp(dy: Int) {
        var isOwnHeightChanged: Boolean

        layoutParams.apply {
            val h = measuredHeight - dy

            // Учитываем высоту statusBar, чтобы не быть слишком низким
            val minHeightWithDecor = minHeight + statusBarHeight

            if (h <= minHeightWithDecor) {
                isOwnHeightChanged = measuredHeight != minHeightWithDecor.toInt()
                height = minHeightWithDecor.toInt()
                scrollingAllowed = true
            } else {
                height = measuredHeight - dy
                isOwnHeightChanged = true
                scrollingAllowed = false
            }
        }

        if (isOwnHeightChanged) requestLayout()
    }

    /**
     * Палец тянут вниз
     */
    private fun siblingScrollingDown(dy: Int) {

        var isOwnHeightChanged: Boolean
        scrollingAllowed = false

        layoutParams.apply {

            val h = measuredHeight + dy

            if (h >= maxHeight) {
                isOwnHeightChanged = measuredHeight != maxHeight.toInt()
                height = maxHeight.toInt()
            } else {
                height = measuredHeight + dy
                isOwnHeightChanged = true
            }
        }
        if (isOwnHeightChanged) requestLayout()
    }

    /**
     * Трансформируем картинку по сообщениям от Scroll-view
     * - postTranslate двигает битмапу вверх внутри viewport
     * - postScale масштабирует битмапу таким образом, чтобы она по одному измерению
     *   полностью разместилась внутри viewport (по другому измерению она может вылезать).
     *   Это аналог centerCrop.
     *
     * Во время ручной трансформации накапливается отрицательное смещение по оси Y (parallax).
     * Когда переключаемся на collapse анимацию, то это смещение нужно анимированно уменьшить до 0.
     * И наоборот, при включении анимации expand, нужно анимированно создать смещение по оси Y.
     */
    private fun manualTransformUp() {
        ivAvatar.drawable ?: return

        val scale = currentScale

        val k = (progressMax - progressManual) / (progressMax - progressThreshold)

        // Смещение по Y для матрицы трансформаций
        val offsetY = dYMin + (dYMax - dYMin) * k

        logIt("manualTransformUp: offsetY=$offsetY, progressManual=$progressManual, k=$k, (dYMax - dYMin)=${dYMax - dYMin}", TAG_DEBUG)

        val baseMatrix = Matrix().apply {
            postTranslate(0f, offsetY)
            postScale(scale, scale)
        }

        ivAvatar.imageMatrix = baseMatrix
    }

    private fun manualTransformDown() {
        ivAvatar.drawable ?: return

        val avatarViewHeight: Float = getImageViewHeight(ivAvatar).toFloat()
        val scale = currentScale

//        val k = 0.1f + 0.5f * (progressMax - progressManual) / progressMax
//        // Смещение по Y которое устанавливается в матрице трансформаций
//        dY = (avatarViewHeight - drawableHeight * scale) * k
        val offsetY = dY * (progressMax - progressManual) / progressMax

        logIt(
            "manualTransformDown: dY=$dY, progressManual=$progressManual, avatarViewHeight=$avatarViewHeight, scale=$scale",
            TAG_DEBUG
        )

        val baseMatrix = Matrix().apply {
            postTranslate(0f, offsetY)
            postScale(scale, scale)
        }

        ivAvatar.imageMatrix = baseMatrix
    }

    /**
     * Трансформация через ViewAnimator.
     * @progress:
     * - если collapse, то меняется от 1 до 0
     * - если expand, то меняется от 0 до 1
     */
    private fun animatedTransform(progress: Float) {
        ivAvatar.drawable ?: return

        logIt("animatedTransform: dY=$dY", TAG_DEBUG)

        val scale = currentScale
        val offsetY = dY * progress

        val baseMatrix = Matrix().apply {
            postTranslate(0f, offsetY)
            postScale(scale, scale)
        }

        ivAvatar.imageMatrix = baseMatrix

        isAnimating = estimateAnimationState(progress)
    }

    /**
     * Изменить высоту картинки. Следать её равной высоте AppBar'а
     */
    private fun updateImageViewHeight() {
        val avatarLayoutParams = ivAvatar.layoutParams
        avatarLayoutParams.height =
            layoutParams.height/*(minHeight + progress * heightStep).toInt()*/
        ivAvatar.requestLayout()
    }

    /**
     * Расчитать прогресс для изменения размеров аватара. Прогресс меняется от 100 до 0.
     * (по аналогии прогресса от SeekBar)
     * 100 - AppBar имеет полную высоту
     * 0 - AppBar в свернутом состоянии (minHeight)
     */
    private fun mapOffsetToProgress(offset: Int): Float {

        val h = measuredHeight + offset

        return when {
            h >= maxHeight -> progressMax
            h <= minHeight -> progressMin
            else -> ((h - minHeight) / scrollRange * progressMax)
        }
    }

    private fun getImageViewWidth(imageView: ImageView): Int {
        return imageView.width - imageView.paddingLeft - imageView.paddingRight
    }

    private fun getImageViewHeight(imageView: ImageView): Int {
        return imageView.height - imageView.paddingTop - imageView.paddingBottom
    }

    /**
     * Функция вызывается в тот момент, когда требуется анимированно изменить размер, а также
     * скруглить/раскруглить ImageView. Пара @wMax/@hMax используется как опорное значение:
     * - при сворачивании она определяет начальный размер, от которого уменьшаемся.
     * - при разворачивании показывает целевой максимум.
     *
     * NOTE: Вызов changeAvatarViewSize вносит изменения в layoutParams аватарки, что приведет
     * к новой layout.
     */
    private fun animateBounds(type: AnimationType) {
        wMax = measuredWidth
        hMax = measuredHeight

        val animator = when (type) {
            AnimationType.Collapse -> {
                ValueAnimator.ofFloat(1f, 0f)
            }
            AnimationType.Expand -> {
                ValueAnimator.ofFloat(0f, 1f)
            }
        }.apply {
            duration = animationDuration

            addUpdateListener {
                isAnimating = true
                progressAnimator = animatedValue as Float
                changeAvatarViewSize(progressAnimator)
                changeAvatarDrawableCornerRadius(progressAnimator)
            }
        }

        if (!animator.isStarted) {
            animator.start()
        }
    }

    /**
     * Изменение размера ImageView
     * @measuredWidth/@measuredHeight - размеры AppBar'а
     */
    private fun changeAvatarViewSize(progress: Float) {
        val size = avatarCollapsedSize

//        ivAvatar.layoutParams = ivAvatar.layoutParams.apply {
//            width = (size + (wMax - size) * progress).toInt()
//            height = (size + (hMax - size) * progress).toInt()
//        }

        ivAvatar.layoutParams = ivAvatar.layoutParams.apply {
            width = (size + (measuredWidth - size) * progress).toInt()
            height = (size + (measuredHeight - size) * progress).toInt()
        }
    }

    /**
     * @progress изменяется от 1 до 0. При этом радиус увеличивается от 0 и до своего max
     *
     * NOTE: Радиус устанавливается не для ImageView, а для RoundedBitmapDrawable, поэтому
     * для круглой картинки нужно ориентироваться именно на размеры RoundedBitmapDrawable.
     */
    private fun changeAvatarDrawableCornerRadius(progress: Float) {
        roundedDrawable.cornerRadius = (1 - progress) * max(drawableWidth, drawableHeight) / 2f
    }

    /**
     * В соседнем Scrolling-элементе произошла прокрутка на расстояние offset
     *
     * Методы updateImageViewHeight, siblingScrollingUp и siblingScrollingDown
     * изменяют layoutParams. Каждый в конце вызывает requestLayout для своей View.
     */
    fun onOffsetChanged(offset: Int) {
        progressManual = mapOffsetToProgress(offset)

        /**
         * Изменение высоты ActionBarLayout с последующим requestLayout()
         */
        if (offset < 0) {
            isAppBarCollapsing = true
            siblingScrollingUp(abs(offset))

        } else if (offset > 0) {
            isAppBarCollapsing = false
            siblingScrollingDown(abs(offset))
        }

        if (progressManual < progressThreshold) {
            if (!isAvatarCollapsing) {
                isAvatarCollapsing = true
                animateBounds(AnimationType.Collapse)
            }
        } else {
            // Это нужно вызывать после методов siblingScrolling потому что в них
            // менятеся значение высоты в layoutParams AppBar'a. И это значение используется
            // в updateImageViewHeight для высоты картинки.
            if (isAvatarCollapsing) {
                isAvatarCollapsing = false
                animateBounds(AnimationType.Expand)
            } else {
                updateImageViewHeight()
            }
        }
    }

    /**
     * Аватарка может находиться в одном из двух состояний, которое определяется значением
     * переменной isAnimating (true/false). Пока аватарка notAnimating, то её размеры меняются
     * "вручную", в ответ на скроллинг списка. Но как только включается анимация коллапсинга,
     * то аватарка переходит в состояние isAnimating и останется в нем до тех пор, пока не
     * выполнится обратная анимация экспадинга. То есть условие выхода из состояния isAnimating
     * можно записать так:
     *  !isAvatarCollapsed && p >= 1
     *
     * В состоянии isAnimating аватарка игнорит события от скроллинга. Это важно !!!
     * Итак, аватарка может быть в одном из состояний isAnimating (true/false) и находясь в состоянии
     * isAnimating (true) она может быть ещё в двух сабсостояниях: isAvatarCollapsed (true/false).
     *
     * По значениям isAvatarCollapsed и прогресса определяем состояние isAnimating.
     * Для этого оцениваем прогресс с точностью до двух цифр после запятой (accuracy = 100)
     */
    private fun estimateAnimationState(progress: Float, accuracy: Int = 100): Boolean {
        val p = (progress * accuracy).toInt()
        return !(!isAvatarCollapsing && p >= accuracy)
    }
}