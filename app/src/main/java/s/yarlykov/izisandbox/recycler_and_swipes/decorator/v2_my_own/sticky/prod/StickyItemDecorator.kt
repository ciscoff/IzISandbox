package s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.sticky.prod

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.Paint
import androidx.core.view.children
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.controller.StickyHolder
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.v2_my_own.Decorator
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
 * Кажется, что элемент приклеился, но на самом деле скрылся и утилизировался.
 *
 * Если продолжать двигать пальцем вверх, то новый sticky приближается к битмапе, его view.y
 * попадает в диапазон '0 < y < bitmap.height' и sticky начинает "выталкивать" битмапу вверх
 * за экран. И когда его view.y станет меньше 0, то произойдет смена битмапы, а сам sticky
 * получит alpha 0f.
 *
 * В данной версии декоратора реализован контроль за правильным показом битмапы при
 * движении пальца вниз. В результате битмапа всегда показывает дату того блока записей, которые в
 * данный момент находятся в верхней части экрана.
 * Реализовано с использованием стэка битмап, хотя можно пойти другим путем и хранить в стеке
 * только данные Date и "рисовать" их на канве внутри закругленного фона. Или как-то ещё.
 *
 * NOTE: Алгоритм с "фотографированием" битмап и их хранением в стеке работает ненадежно.
 * При резком скроле/флинге часть битмап теряется (не успевает создаваться). Наверное нужно генерить
 * битмапы на лету в зависимости от текущего видимого контента. Но в целом, в демонстрационных
 * целях, вполне себе прилично работает.
 *
 * NOTE: Вся движуха происходит только в момент когда StickyView.y находится в диапазоне
 * от 0 до bitmap.height. Именно в этот момент sticky view таскает за собой битмапу вверх и вниз.
 * При любых других положениях StickyView.y битмапа спокойно висит в верху списка.
 */
class StickyItemDecorator : Decorator.RecyclerViewDecorator {

    private val stickyStack: Stack<StickyStack.Element> = StickyStack()

    private val paintSimple = Paint(Paint.ANTI_ALIAS_FLAG)

    private val paintDebug = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = LightingColorFilter(Color.GREEN, 0)
    }

    private var paintCurrent = paintSimple

    fun highlightMode(mode: Boolean) {
        paintCurrent = if (mode) paintDebug else paintSimple
    }

    private var currentSticky: StickyStack.Element? = null
    private var prevBitmapTopOffset: Float = 0f

    private var prevTopStickyViewY = 0

    @ExperimentalStdlibApi
    override fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        // Найти все sticky на экране
        val stickyViewHolders = recyclerView.children
            .map { recyclerView.findContainingViewHolder(it) }
            .filter { it is StickyHolder }

        // Сделать все sticky видимыми
        stickyViewHolders.forEach { it?.itemView?.alpha = 1.0f }

        val topStickyHolder = stickyViewHolders.firstOrNull() ?: return
        val topStickyViewId = (topStickyHolder as StickyHolder).id
        val topStickyViewY = topStickyHolder.itemView.y

        /**
         * Первым делом нужно определиться с currentSticky, т.е. с битмапой, которую будем
         * отрисовывать в данном draw().
         * - Её может ещё не быть (начальное состояние).
         * - Её требуется заменить (если очередной StickyView выталкивает с экрана чужую битмапу).
         * Её не требуется менять в данном draw()
         *
         * Путь битмапы в стек:
         *   StickyView.toBitmap() -> currentSticky.bitmap -> Stack
         * Путь из стека:
         *   Stack -> currentSticky.bitmap
         */
        when {
            /**
             * TODO Проверяем, что находимся в начальном состоянии.
             * - Это старт активити/фрагмента/родительского_элемента.
             * - RecyclerView только что создан и отрисован первый раз.
             */
            (currentSticky == null) -> {
                currentSticky =
                    StickyStack.Element(topStickyViewId, topStickyHolder.itemView.drawToBitmap())
            }
            /**
             * TODO Проверяем, что StickyView.Y выше верхней границы RecyclerView.
             * В этом случае два варианта:
             * 1. Первый StickyView адаптера поднимается вверх. В этом случае других sticky ещё
             *    не было, currentSticky.id == topStickyId и поэтому ничего делать не надо.
             * 2. Очередной StickyView поднялся вверх. Его id отличается от id текущей битмапы и в
             *    этом случае он должен заменить currentSticky, который предварительно сохранится
             *    в стеке.
             *
             * NOTE: topSticky не заменит currentSticky, если они совпадают, а currentSticky не
             * попадет в стек дважды (pushOnce проверяет).
             */
            (topStickyViewY < 0) -> {
                if (currentSticky?.id != topStickyViewId) {
                    currentSticky?.let(stickyStack::pushOnce)
                    currentSticky =
                        StickyStack.Element(
                            topStickyViewId,
                            topStickyHolder.itemView.drawToBitmap()
                        )
                }
            }
        }

        /**
         * На текущий момент ужё есть ясность относительно currentSticky поэтому можно посчитать
         * метрики.
         */
        val bitmapHeight = currentSticky?.bitmap?.height ?: 0

        /**
         * Когда StickyView.Y попадает в диапазон '0 < y < bitmap.height', то он начинает тянуть
         * вниз за собой или толкать вверх битмапу. Это достигается изменением top-позиции битмапы
         * в координатах канвы. Например, надвигающийся снизу sticky касается битмапы снизу и
         * начинает "выталкивать" её вверх за экран.
         *
         * Если StickyView.Y в другом диапазоне, то ничего не происходит и битмапа имеет 'top == 0'
         * и висит вверху экрана.
         */
        val bitmapTopOffset =
            if (0 <= topStickyViewY && topStickyViewY <= bitmapHeight) {
                topStickyViewY - bitmapHeight
            } else {
                0f
            }

        /**
         * TODO Далее проверяем, что StickyView спустилась сверху и стала полностью видна.
         * В этой ситуации нужно скопировать (peek) элемент с верхушки стека потому что он имеет
         * "предыдущее" значение id чем у topStickyView. И тогда на экране sticky элементы
         * будут появляться в хронологической последовательности без дублирования дат.
         */
        if (prevTopStickyViewY < 0 &&
            topStickyViewY >= 0 &&
            topStickyViewY < bitmapHeight / 2
        ) {
            currentSticky = stickyStack.peek()
        }
        prevTopStickyViewY = topStickyViewY.toInt()

        /**
         * TODO Проверяем, что StickyBitmap спустилась сверху и стала полностью видна.
         * После того как известно текущее bitmapTopOffset нужно сравнить его с предыдущим
         * значением. Нужно поймать момент, когда оно из отрицательного (верхняя граница
         * битмапы выше верхней границы RecyclerView) переходит в 0. И в этом случае снимаем со
         * стека верхний элемент (pop) и помещаем его в currentSticky.
         *
         * NOTE: Забираем из стека только если двигали пальцем сверху вниз. Поэтому выполняется
         * проверка 'abs(prevBitmapTopOffset.toInt()) < bitmapHeight/2'
         */
        if (bitmapTopOffset.toInt() >= 0 &&
            prevBitmapTopOffset.toInt() < 0 &&
            abs(prevBitmapTopOffset.toInt()) < bitmapHeight / 2
        ) {
            currentSticky = stickyStack.pop()
        }
        prevBitmapTopOffset = bitmapTopOffset

        /**
         * И последняя проверка для случая когда резко свайпанули пальцем вниз. В этом случае
         * верхний StickyView по "инерции" пролетел куда-то вниз. Во время свайпа стек не
         * освобождался правильно. Поэтому нужно чтобы в качестве sticky оказался элемент
         * который имеет предыдущее значение id, чем StickyView.id.
         */
//        if(topStickyViewY > bitmapHeight) {
//            stickyStack.popUpTo(topStickyViewId)?.let { element ->
//                currentSticky = element
//            }
//        }

        topStickyHolder.itemView.alpha = if (topStickyViewY < 0f) 0f else 1f

        currentSticky?.bitmap?.let {
            canvas.drawBitmap(it, 0f, bitmapTopOffset, paintCurrent)
        }

        // Очистить стек если верхний sticky (adapterPosition 0) полностью на экране
        if (topStickyHolder.adapterPosition == 0 && topStickyViewY >= 0) {
            stickyStack.clear()
        }
    }

    companion object {
        private const val LOG_TAG = "STICKY"
    }
}