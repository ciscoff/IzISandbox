package s.yarlykov.izisandbox.matrix.scale_animated

import android.animation.TypeEvaluator
import android.graphics.RectF

/**
 * https://stackoverflow.com/questions/18828049/animate-zoom-and-translate-a-part-of-image-on-android-canvas-view
 *
 * Evaluator для работы с ValueAnimator.
 */
class RectEvaluator : TypeEvaluator<RectF> {

    private val rectBetween = RectF()

    override fun evaluate(fraction: Float, rectSrc: RectF, rectDest: RectF): RectF {

        rectBetween.apply {
            top = rectSrc.top + fraction * (rectDest.top - rectSrc.top)
            left = rectSrc.left + fraction * (rectDest.left - rectSrc.left)
            right = rectSrc.right + fraction * (rectDest.right - rectSrc.right)
            bottom = rectSrc.bottom + fraction * (rectDest.bottom - rectSrc.bottom)
        }

        return rectBetween
    }
}