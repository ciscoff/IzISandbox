package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky

import android.graphics.*
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.StickyHolder
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
import s.yarlykov.izisandbox.utils.logIt
import kotlin.math.abs

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
 *
 * В данной версии декоратора (Smart) реализован контроль за правильным показом битмапы при
 * движении пальца вниз. В результате битмапа всегда показывает дату того блока записей, которые в
 * данный момент находятся в верхней части экрана.
 * Реализовано с использованием стэка битмап, хотя можно пойти другим путем и хранить в стеке
 * только данные Date и "рисовать" их на канве внутри закругленного фона. Или как-то ещё.
 *
 * NOTE: Алгоритм с "фотографированием" битмап и их хранением в стеке работает ненадежно.
 * При резком скроле/флинге часть битмап теряется (не успевает создаваться). Наверное нужно генерить
 * битмапы на лету в зависимости от текущего видимого контента. Но в целом, в демонстрационных
 * целях, вполне себе прилично работает.
 */
class StickyItemDecoratorSmart : Decorator.RecyclerViewDecorator {

    private val bitmapStack: Stack<Bitmap> = StickyStack()

    private val paintSimple = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintDebug = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = LightingColorFilter(Color.GREEN, 0)
    }

    private var paintCurrent = paintSimple

    fun highlightMode(mode: Boolean) {
        paintCurrent = if (mode) paintDebug else paintSimple
    }

    private var currentStickyBitmap: Bitmap? = null
    private var prevBitmapTopOffset: Float = 0f

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
         * Битмапа попадает в стек только побывав в currentStickyBitmap. Когда битмапа снимается со
         * стека, то попадает в currentStickyBitmap.
         */
        when {
            // Начальное состояние - старт активити.
            (currentStickyBitmap == null) -> {
                topStickyBitmap?.let {
                    currentStickyBitmap = it
                }
            }
            // sticky view выходит за верхнюю границу. В этом случае создается её битмапа и
            // помещается в currentStickyBitmap, а содержимое currentStickyBitmap уходит в стек.
            // topStickyBitmap не перезапишет currentStickyBitmap, если она там уже есть (sameAs) и
            // currentStickyBitmap не попадет в стек, если она там уже есть (pushOnce).
            (topStickyY < 0) -> {
                topStickyBitmap?.let {
                    if (currentStickyBitmap?.sameAs(it) != true) {
                        currentStickyBitmap?.also(bitmapStack::pushOnce)
                        currentStickyBitmap = it
                    }
                }
            }
        }

        val bitmapHeight = currentStickyBitmap?.height ?: 0

        val bitmapTopOffset =
            /**
             * Когда sticky view.y попадает в диапазон '0 < y < bitmap.height', то он начинает
             * тянуть вниз за собой или толкать вверх битмапу. Это достигается изменением
             * top-позиции битмапы в координатах канвы. Например, надвигающийся снизу
             * sticky касается битмапы снизу и начинает "выталкивать" её вверх за экран.
             *
             * Если sticky view.y в другом диапазоне, то битмапа имеет top = 0 и висит
             * вверху экрана.
             */
            if (0 <= topStickyY && topStickyY <= bitmapHeight) {
                topStickyY - bitmapHeight
            } else {
                0f
            }

        /**
         * После того как вычислили текущее top-смещение битмапы нужно сравнить его с предыдущим
         * значением. Нужно поймать момент, когда значение из ненулевого (битмапа частично или
         * полностью ниже верхней границы экрана) переходит в 0. И в этом случае берем верхнюю
         * битмапу из стека и помещаем в currentStickyBitmap.
         *
         * NOTE: Забираем из стека только если двигали пальцем сверху вниз. Поэтому выполняется
         * проверка 'abs(prevBitmapTopOffset.toInt()) < bitmapHeight/2'
         */
        if (prevBitmapTopOffset != 0f &&
            abs(prevBitmapTopOffset.toInt()) < bitmapHeight/2
            && bitmapTopOffset == 0f) {

            with(bitmapStack) {
                if (isNotEmpty() && peek()?.sameAs(currentStickyBitmap!!) != true) {
                    currentStickyBitmap = pop()
                }
            }
        }

        prevBitmapTopOffset = bitmapTopOffset

        topStickyHolder?.itemView?.alpha = if (topStickyY < 0f) 0f else 1f

        currentStickyBitmap?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paintCurrent)
        }

        // Очистить стек если верхний sticky (adapterPosition 0) полностью на экране
        if (topStickyHolder?.adapterPosition == 0 && topStickyY >= 0) {
            bitmapStack.clear()
        }
    }
}