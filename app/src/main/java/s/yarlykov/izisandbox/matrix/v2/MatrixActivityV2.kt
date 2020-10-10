package s.yarlykov.izisandbox.matrix.v2

import android.animation.ValueAnimator
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.activity_matrix_v2.*
import s.yarlykov.izisandbox.R
import kotlin.math.max

const val TAG_ANIM = "TAG_ANIM"
const val ANIMATION_DURATION = 150L

/**
 * Работа с ползунком
 * ==================
 * 1. onProgressChanged
 * 2. -> updateImageViewHeight (via layoutParams)
 * 3.       -> layout
 * 4.           -> updateImageViewTransform -> manualTransform
 *
 * Анимация
 * ========
 * 1. animateBounds
 * 2.1 changeAvatarViewSize
 * 3.       -> layout
 * 4.           -> updateImageViewTransform -> animatedTransform
 * 2.2 changeAvatarDrawableCornerRadius
 *
 */
class MatrixActivityV2 : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    lateinit var roundedDrawable: RoundedBitmapDrawable

    /**
     * Максимум для SeekBar
     */
    private val seekMax = 100f

    /**
     * Габариты ImageView
     */
    private var avatarHeightMax = 0f
    private var avatarHeightMin = 0f
    private var avatarCollapsed = 0f
    private var heightStep = 0f

    private var wMax = 0
    private var hMax = 0

    /**
     * Габариты RoundedBitmapDrawable
     */
    private var drawableWidth = 0
    private var drawableHeight = 0

    /**
     * Анимация выполняется/не_выполняется
     */
    private var isAnimating = false

    /**
     * Последнее значение из ValueAnimator'а
     */
    private var animatorUpdate = 0f

    /**
     * Смещение по Y которое устанавливается в матрице трансформаций
     */
    private var dY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matrix_v2)

        avatarHeightMax = resources.getDimension(R.dimen.avatar_max_height)
        avatarHeightMin = resources.getDimension(R.dimen.avatar_min_height)
        avatarCollapsed = resources.getDimension(R.dimen.avatar_collapsed)
        heightStep = (avatarHeightMax - avatarHeightMin) / seekMax

        ivAvatar.setRoundedDrawable(R.drawable.valery)

        roundedDrawable = ivAvatar.drawable as RoundedBitmapDrawable
        drawableWidth = roundedDrawable.intrinsicWidth
        drawableHeight = roundedDrawable.intrinsicHeight

        seekBar.apply {
            max = seekMax.toInt()
            progress = seekMax.toInt()
            setOnSeekBarChangeListener(this@MatrixActivityV2)
        }

        ivAvatar.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->

            /**
             * В зависимости от того запущена анимация или нет вызываем разные функции трансформации.
             * manualTransform - работает с прогрессом из SeekBar
             * animatedTransform - работает с прогрессом из ValueAnimator
             */
            if (isAnimating) {
            animatedTransform(animatorUpdate)
        } else {
            manualTransform(seekBar.progress)
        }

            wMax = max(v.measuredWidth, wMax)
            hMax = max(v.measuredHeight, hMax)
        }

        btnAnim.setOnClickListener {
            animateBounds()
        }
    }

    /**
     * Высота ImageView меняется и при каждом изменении нужно знать значение scale
     */
    private val currentScale: Float
        get() {
            val viewWidth: Float = getImageViewWidth(ivAvatar).toFloat()
            val viewHeight: Float = getImageViewHeight(ivAvatar).toFloat()

            val widthScale = viewWidth / drawableWidth
            val heightScale = viewHeight / drawableHeight

            return max(widthScale, heightScale)/*widthScale.coerceAtLeast(heightScale)*/
        }

    /**
     * Трансформация через ViewAnimator.
     * @progress меняется от 1 до 0
     */
    private fun animatedTransform(progress: Float) {
        ivAvatar.drawable ?: return

        val scale = currentScale
        val offsetY = dY * progress

        val baseMatrix = Matrix().apply {
            postTranslate(0f, offsetY)
            postScale(scale, scale)
        }

        ivAvatar.imageMatrix = baseMatrix

        isAnimating = progress >= 0f
    }

    /**
     * Трансформируем картинку по сообщениям от seekBar
     */
    private fun manualTransform(progress: Int) {
        ivAvatar.drawable ?: return

        val viewHeight: Float = getImageViewHeight(ivAvatar).toFloat()
        val scale = currentScale

        val k = 0.05f * (seekMax - progress) / seekMax
        dY = (viewHeight - drawableHeight) * (0.1f + k)
        val offsetY = dY

        val baseMatrix = Matrix().apply {
            postTranslate(0f, offsetY)
            postScale(scale, scale)
        }

        ivAvatar.imageMatrix = baseMatrix
    }

    private fun updateImageViewHeight(progress: Int) {
        val avatarLayoutParams = ivAvatar.layoutParams
        avatarLayoutParams.height = (avatarHeightMin + progress * heightStep).toInt()
        ivAvatar.layoutParams = avatarLayoutParams
    }

    private fun getImageViewWidth(imageView: ImageView): Int {
        return imageView.width - imageView.paddingLeft - imageView.paddingRight
    }

    private fun getImageViewHeight(imageView: ImageView): Int {
        return imageView.height - imageView.paddingTop - imageView.paddingBottom
    }

    private fun animateBounds() {

        /**
         * Перед началом анимации нужно зафиксировать текущую высоту ImageView, потому что
         * от этого значения будем анимаровать.
         */
        wMax = ivAvatar.measuredWidth
        hMax = ivAvatar.measuredHeight

        val animator = ValueAnimator.ofFloat(1f, 0f).apply {
            duration = ANIMATION_DURATION

            addUpdateListener {
                isAnimating = true
                animatorUpdate = animatedValue as Float
                changeAvatarViewSize(animatorUpdate)
                changeAvatarDrawableCornerRadius(animatorUpdate)
            }
        }

        if (!animator.isStarted) {
            animator.start()
        }
    }

    /**
     * Изменение размера ImageView
     */
    private fun changeAvatarViewSize(progress: Float) {
        val size = avatarCollapsed

        ivAvatar.layoutParams = ivAvatar.layoutParams.apply {
            width = (size + (wMax - size) * progress).toInt()
            height = (size + (hMax - size) * progress).toInt()
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
     * После того как ползунок сдвинулся вызываем updateImageViewHeight, которая
     * меняет высоту ImageView. Изменения выполняются через layoutParams, что в свою очередь
     * приводит к вызову ivValery.addOnLayoutChangeListener(). И именно оттуда вызываем
     * функцию, которая делает трасформацию картинки. То есть трансформация выполняется
     * после layout.
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateImageViewHeight(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    private fun ImageView.setRoundedDrawable(drawableId: Int) {
        val resources = this.context.resources
        val srcBitmap = BitmapFactory.decodeResource(resources, drawableId)

        this.setImageDrawable(
            RoundedBitmapDrawableFactory.create(resources, srcBitmap)
        )
    }
}