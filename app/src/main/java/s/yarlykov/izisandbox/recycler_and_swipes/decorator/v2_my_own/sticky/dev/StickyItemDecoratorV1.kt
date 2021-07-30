package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.dev

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
 * Декоратор "копирует" битмапу этого верхнего sticky и рисует на канве выше видимой области экрана.
 * Поэтому в самом начале мы видим sticky в верхней строке списка и НЕ видим копию его битмапы
 * за верхней границей экрана.
 *
 * Далее двигаем пальцем вверх. RecyclerView.LayoutManager отрабатывает скроллинг и у sticky
 * view.y становится меньше нуля (выполнен offset элемента). Декоратор реагирует и скрывает
 * sticky (view.alpha = 0f) и вместо него рисует его битмапу в верхней части экрана (bitmap.y = 0).
 * Кажется, что элемент приклеился, а на самом деле скрылся и утилизировался.
 *
 * Если продолжать двигать пальцем вверх, то новый sticky приближается к битмапе, его view.y
 * попадает в диапазон '0 < y < bitmap.height' и sticky начинает "выталкивать" битмапу вверх
 * за экран. И когда его view.y станет меньше 0, то произойдет смена битмапы, а сам sticky
 * получит alpha 0f.
 */
class StickyItemDecoratorV1 : Decorator.RecyclerViewDecorator {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var currentStickyBitmap: Bitmap? = null

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

        // Смена битмапы происходит только когда верхний sticky вышел за верхнюю границу экрана.
        if (currentStickyBitmap == null || topStickyY < 0) {
            currentStickyBitmap = topStickyBitmap
        }

        val bitmapHeight = currentStickyBitmap?.height ?: 0
        // Надвигающийся снизу sticky касается битмапы и начинает "выталкивать" её вверх за экран.
        // И когда его view.y станет меньше 0, то произойдет смена битмапы, а сам sticky получит
        // alpha 0f.
        val bitmapTopOffset = if (0 <= topStickyY && topStickyY <= bitmapHeight) {
            topStickyY - bitmapHeight
        } else 0f

        topStickyHolder?.itemView?.alpha = if(topStickyY < 0f) 0f else 1f

        currentStickyBitmap?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paint)
        }
    }
}