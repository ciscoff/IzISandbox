package s.yarlykov.izisandbox.extensions

import android.animation.Animator
import android.animation.ValueAnimator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * awaitEnd поддерживает как отмену самой внешней корутины, так и отмену аниматора.
 * В первом случае сработает подписка cont.invokeOnCancellation.
 *
 * @param onUpdate - действия для AnimatorUpdateListener
 */
@ExperimentalCoroutinesApi
suspend fun ValueAnimator.awaitEnd(onUpdate: ValueAnimator.() -> Unit) =

    suspendCancellableCoroutine<Unit> { cont ->

        cont.invokeOnCancellation { cancel() }

        addUpdateListener { it.onUpdate() }

        addListener(object : Animator.AnimatorListener {
            private var endedSuccessfully = true

            // Анимация завершилась и ...
            override fun onAnimationEnd(animation: Animator?) {

                animation?.removeListener(this)

                // ... корутина активна и ....
                if (cont.isActive) {
                    // ... статус endedSuccessfully == true,
                    // то делаем resume, чтобы продолжить код внутри корутины.
                    if (endedSuccessfully) {
                        cont.resume(Unit, null)
                    }
                    // ... или если анимация была отменена (endedSuccessfully == false),
                    // то отменяем корутину
                    else {
                        cont.cancel()
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
                endedSuccessfully = false
            }

            override fun onAnimationStart(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }
        })
    }