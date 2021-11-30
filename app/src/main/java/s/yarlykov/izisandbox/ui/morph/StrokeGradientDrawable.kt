package s.yarlykov.izisandbox.ui.morph

import android.graphics.drawable.GradientDrawable

/**
 * Обертка над GradientDrawable, но не понятно зачем [color] потому что для градиента нужен
 * не один цвет, а несколько.
 */
class StrokeGradientDrawable(val gradientDrawable: GradientDrawable) {

    var strokeWidth: Int = 0
        set(value) {
            field = value
            gradientDrawable.setStroke(value, strokeColor)
        }

    var strokeColor: Int = 0
        set(value) {
            field = value
            gradientDrawable.setStroke(strokeWidth, value)
        }

    var cornerRadius: Float = 0f
        set(value) {
            field = value
            gradientDrawable.cornerRadius = value
        }

    var color: Int = 0
        set(value) {
            field = value
            gradientDrawable.setColor(value)
        }
}