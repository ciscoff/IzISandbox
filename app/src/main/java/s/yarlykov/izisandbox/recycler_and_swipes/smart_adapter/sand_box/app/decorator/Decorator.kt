package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.app.decorator

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

object Decorator {

    /**
     * Если какой-то декор нужно вывести на все views, то устанавливаем viewType == EACH_VIEW
     */
    const val EACH_VIEW = -1

    class Builder {

        /**
         * Scope of underlay decors for ViewHolder
         */
        private var underlayViewHolderScope: MutableList<DecorBinder<ViewHolderDecorator>> =
            mutableListOf()

        private var offsetsScope: MutableList<DecorBinder<OffsetDecorator>> = mutableListOf()


        /**
         * Добавить ViewHolder-декоратор для определенного viewType.
         * @param pair Pair of ViewHolder's ViewType and [ru.surfstudio.android.recycler.decorator.base.ViewHolderDecor]
         */
        fun underlay(pair: Pair<Int, ViewHolderDecorator>): Builder {
            val (viewType, decorator) = pair

            return apply {
                underlayViewHolderScope.add(DecorBinder(viewType, decorator))
            }
        }

        /**
         * Добавить offset-декоратор для определенного viewType.
         */
        fun offset(pair: Pair<Int, OffsetDecorator>): Builder {
            val (viewType, decorator) = pair
            return apply { offsetsScope.add(DecorBinder(viewType, decorator)) }
        }

        /**
         * Builds the main decorator
         */
        fun build(): MainDecorator {
            require(
                offsetsScope.groupingBy { it.viewType }.eachCount().all { it.value == 1 }
            ) { "Any ViewHolder can have only a single OffsetDecorator" }

            return MainDecorator(
                DecorController(
                    underlayViewHolderScope,
                    offsetsScope)
//                DecorsBridge(
//                    underlayViewHolderScope,
//                    underlayRecyclerScope,
//                    overlayViewHolderScope,
//                    overlayRecyclerScope,
//                    offsetsScope
//                )
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
     * Interface for implementation own ViewHolderDecor
     */
    interface ViewHolderDecorator {
        fun draw(canvas: Canvas, view: View, recyclerView: RecyclerView, state: RecyclerView.State)
    }
}