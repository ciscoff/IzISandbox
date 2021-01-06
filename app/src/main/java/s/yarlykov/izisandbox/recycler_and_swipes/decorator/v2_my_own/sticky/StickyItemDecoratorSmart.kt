package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky

import android.graphics.*
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.StickyHolder
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.utils.logIt
import java.lang.Exception

/**
 * Алгоритм такой:
 * При первом показе списка самым верхним его элементом является sticky и его view.y = 0.
 * Декоратор копируем битмапу этого верхнего sticky и рисует на канве выше видимой области экрана.
 * То есть в самом начале мы видим sticky в верхней строке списка и НЕ видим копию его битмапы
 * за верхней границей экрана.
 *
 * Далее двигаем пальцем вверх. RecyclerView.LayoutManager отрабатывает скроллинг и у sticky
 * view.y становится меньше нуля (выполнен offset элемента). Декоратор реагирует и скрывает
 * sticky (view.alpha = 0f) и вместо него рисует его битмапу в верхней части экрана. Кажется, что
 * элемент приклеился, а на самом деле скрылся и утилизировался.
 *
 * Если продолжать двигать пальцем вверх, то новый sticky приблизится в битмапе и начинает
 * "выталкивать" её вверх за экран. И когда его view.y станет меньше 0, то произойдет смена битмапы,
 * а сам sticky получит alpha 0f.
 */
class StickyItemDecoratorSmart : Decorator.RecyclerViewDecorator {

    private val bitmapStack = StickyStack()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = LightingColorFilter(Color.CYAN, 0)
    }
    private var currentStickyBitmap: Bitmap? = null

    @ExperimentalStdlibApi
    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        // Найти все sticky на экране
        val stickyViewHolders = recyclerView.children
            .map { recyclerView.findContainingViewHolder(it) }
            .filter { it is StickyHolder }

        // Сделать все sticky видимыми
        stickyViewHolders.forEach { it?.itemView?.alpha = 1.0f }

        val topStickyHolder = try {
            stickyViewHolders.first()
        } catch (e: Exception) {
            logIt("${this::class.java.simpleName}: Illegal state in draw method")
            null
        }

        val topStickyBitmap = topStickyHolder?.itemView?.drawToBitmap()
        val topStickyY = topStickyHolder?.itemView?.y ?: 0f

        /**
         * 1. Определить чью битмапу будем использовать в качестве currentStickyBitmap
         */
        when {
            (currentStickyBitmap == null) -> {
                topStickyBitmap?.let {
                    currentStickyBitmap = it
                }
            }
            (topStickyY < 0) -> {

                topStickyBitmap?.let {
                    if(currentStickyBitmap?.sameAs(it) != true) {
                        currentStickyBitmap?.also (bitmapStack::pushOnce)
                        currentStickyBitmap = it
                    }
                }
            }
        }

        logIt("bitmapStack size = ${bitmapStack.size}")

        val bitmapHeight = currentStickyBitmap?.height ?: 0
        // Надвигающийся снизу sticky касается битмапы и начинает "выталкивать" её вверх за экран.
        // И когда его view.y станет меньше 0, то произойдет смена битмапы, а сам sticky получит
        // alpha 0f.
        val bitmapTopOffset = if (0 <= topStickyY && topStickyY <= bitmapHeight) {
            topStickyY - bitmapHeight
        } else {
            0f
        }

//        if((abs(bitmapTopOffset) == bitmapHeight.toFloat()) && bitmapStack.isNotEmpty()) {
//            currentStickyBitmap = bitmapStack.removeLast()
//        }

        topStickyHolder?.itemView?.alpha = if (topStickyY < 0f) 0f else 1f

//        logIt("topStickyY=$topStickyY, bitmapTopOffset=$bitmapTopOffset, bitmapHeight=$bitmapHeight")

        currentStickyBitmap?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paint)
        }
    }

    var started : Boolean = false

    private fun scrollProgress(recyclerView: RecyclerView): Float {
        val currentOffset = recyclerView.computeVerticalScrollOffset()
        val maxOffset =
            recyclerView.computeVerticalScrollRange() - recyclerView.computeVerticalScrollExtent()
        return currentOffset.toFloat() / maxOffset
    }

    private fun MutableList<Bitmap>.containsOther(other : Bitmap) : Boolean {
        forEach {
            if(it.sameAs(other)) return true
        }

        return false
    }

    private fun MutableList<Bitmap>.doesNotContain(other : Bitmap) : Boolean {
        return !containsOther(other)
    }

    private fun MutableList<Bitmap>.pushOnce(other : Bitmap) {
        if(doesNotContain(other)) {
            add(other)
        }
    }

}