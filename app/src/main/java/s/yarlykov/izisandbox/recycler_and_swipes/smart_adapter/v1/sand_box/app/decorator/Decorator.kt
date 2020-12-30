package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.v1.sand_box.app.decorator

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Основной класс, который собирает все в кучу и настраивает.
 */
object Decorator {

    /**
     * Если какой-то декор нужно вывести на все views, то устанавливаем viewType == EACH_VIEW
     */
    const val EACH_VIEW = -1

    class Builder {

        /**
         * Хранилище декораторов. Scope задает область их применения:
         * - Underlay - рисуем до onDraw() у view элемента списка.
         * - Overlay - рисуем после onDraw() у view элемента списка.
         * - ViewHolder - рисуем в области, занимаемой view элемента списка.
         * - RecyclerView - рисуем в любом месте RecyclerView.
         * - Offsets - просто определить отступы.
         *
         * В скоупе хранятся элементы DecorBinder, которые связывают viewType и его декоратор.
         */
        private var scopeViewHolderUnderlay: MutableList<DecorBinder<ViewHolderDecorator>> =
            mutableListOf()

        private var scopeViewHolderOverlay: MutableList<DecorBinder<ViewHolderDecorator>> =
            mutableListOf()

        private var scopeRecyclerViewUnderlay: MutableList<RecyclerViewDecorator> =
            mutableListOf()

        private var scopeRecyclerViewOverlay: MutableList<RecyclerViewDecorator> =
            mutableListOf()

        private var scopeOffsets: MutableList<DecorBinder<OffsetDecorator>> =
            mutableListOf()

        /**
         * underlay рисование для определенных viewType
         */
        fun underlay(pair: Pair<Int, ViewHolderDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply { scopeViewHolderUnderlay.add(DecorBinder(viewType, decorator)) }
        }

        /**
         * underlay рисование на ВСЕХ элементах
         */
        fun underlay(decor: ViewHolderDecorator): Builder {
            return apply { scopeViewHolderUnderlay.add(DecorBinder(EACH_VIEW, decor)) }
        }

        /**
         * underlay рисование на RecyclerView
         */
        fun underlay(decor: RecyclerViewDecorator): Builder {
            return apply { scopeRecyclerViewUnderlay.add(decor) }
        }

        /**
         * overlay рисование на RecyclerView
         */
        fun overlay(decor: RecyclerViewDecorator): Builder {
            return apply { scopeRecyclerViewOverlay.add(decor) }
        }

        /**
         * overlay рисование для определенных viewType
         */
        fun overlay(pair: Pair<Int, ViewHolderDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply {
                scopeViewHolderOverlay.add(DecorBinder(viewType, decorator))
            }
        }

        /**
         * overlay рисование на ВСЕХ элементах
         */
        fun overlay(decor: ViewHolderDecorator): Builder {
            return apply {
                scopeViewHolderOverlay.add(DecorBinder(EACH_VIEW, decor))
            }
        }

        /**
         * Добавить offset-декоратор для определенного viewType.
         */
        fun offset(pair: Pair<Int, OffsetDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply { scopeOffsets.add(DecorBinder(viewType, decorator)) }
        }

        /**
         * Добавить offset-декоратор для всех viewType.
         */
        fun offset(decor: OffsetDecorator): Builder {
            return apply { scopeOffsets.add(DecorBinder(EACH_VIEW, decor)) }
        }

        /**
         * Builds the main decorator
         */
        fun build(): MainDecorator {
            require(
                scopeOffsets.groupingBy { it.viewType }.eachCount().all { it.value == 1 }
            ) { "Any ViewHolder can have only a single OffsetDecorator" }

            return MainDecorator(
                DecorController(
                    scopeViewHolderUnderlay,
                    scopeOffsets
                )
            )
        }
    }

    /**
     * Интерфейс для получения offsets
     */
    interface OffsetDecorator {
        fun getItemOffsets(
            outRect: Rect,
            view: View,
            recyclerView: RecyclerView,
            state: RecyclerView.State
        )
    }

    /**
     * Интерфейс для рисования на поверхности, занимаемой элементом списка
     */
    interface ViewHolderDecorator {
        fun draw(canvas: Canvas, view: View, recyclerView: RecyclerView, state: RecyclerView.State)
    }

    /**
     * Интерфейс для рисования в произвольном месте поверхности RecyclerView
     */
    interface RecyclerViewDecorator {
        fun draw(canvas: Canvas, recyclerView: RecyclerView, state: RecyclerView.State)
    }
}