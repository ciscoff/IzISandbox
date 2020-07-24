package s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo.SwipeWithUndoActivity.Companion.PENDING_REMOVAL_TIMEOUT

// Переименуем для удобства
interface Remover : Runnable

class TestAdapter : RecyclerView.Adapter<TestViewHolder>() {

    // Модель которую показываем (строки)
    private val items = mutableListOf<String>()

    // Элементы ожидающие удаления (строки)
//    private val itemsPendingRemoval = mutableListOf<String>()

    private var initModelSize: Int = 15

    init {

        // Генерим элементы
        for (i in 0 until initModelSize) {
            items.add("Item $i")
        }
    }

    // Undo on/off. Настраивается из меню.
    var isUndoOn = false

    private val handler = Handler()

    // Элементы ожидающие удаления
    private var pendingRemovers = mutableMapOf<String, Remover>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {

        return TestViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_swiped_row_view,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    /**
     * Добавить элементы в модель
     */
    fun addItems(howMany: Int) {

        var next = items.lastIndex

        if (howMany > 0) {
            for (i in 0 until howMany) {
                items.add("Item ${++next}")
                notifyItemInserted(items.size - 1)
            }
        }
    }

    fun putInRemoval(position: Int) {
        val item = items[position]

        if (!pendingRemovers.keys.contains(item)) {

            val remover = object : Remover {
                override fun run() {
                    removeFromModel(position)
                }
            }
            pendingRemovers[item] = remover
            handler.postDelayed(remover, PENDING_REMOVAL_TIMEOUT)
        }

//        if (!itemsPendingRemoval.contains(item)) {
//
//
//            // Это вызовет повторную инициализацию соотв ViewHolder'а,
//            // то есть будет вызван onBindViewHolder
//            notifyItemChanged(position)
//
//            // let's create, store and post a runnable to remove the item
//            val pendingRemovalRunnable = Runnable { removeFromModel(position) }
//
//            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT)
//            pendingRemoval[item] = pendingRemovalRunnable
//        }
    }

    /**
     * Удалить элемент из модели
     */
    fun removeFromModel(position: Int) {
        val item = items[position]

        // Если в списке на удаление, то удалить
        pendingRemovers.remove(item)

        // Удалить из основной модели
        if (items.contains(item)) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun isPendingRemoval(position: Int): Boolean {
        val item = items[position]
        return pendingRemovers.keys.contains(item)
    }


    override fun onBindViewHolder(viewHolder: TestViewHolder, position: Int) {

        val item = items[position]

        // Отрисовка элемента в состоянии Undo.
        // Это красный фон, кнопка справа и скрытый текст
        if (pendingRemovers.keys.contains(item)) {

            viewHolder.itemView.setBackgroundColor(Color.RED)
            viewHolder.titleTextView.visibility = View.GONE
            viewHolder.undoButton.visibility = View.VISIBLE

            // Нажатие на кнопку отменяет удаление. То есть нужно остановить
            // зашедуленый Remover, и перерисовать элемент в нормальное состояние.
            // Перерисовка будет выполнена повторным входом в onBindViewHolder
            // после вызова notifyItemChanged(position)
            viewHolder.undoButton.setOnClickListener {

                val remover = pendingRemovers[item]

                pendingRemovers.remove(item)

                if (remover != null) handler.removeCallbacks(
                    remover
                )
                notifyItemChanged(position)
            }
        }
        // Отрисовка элемента в нормальном состоянии
        else {
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            viewHolder.titleTextView.visibility = View.VISIBLE
            viewHolder.titleTextView.text = item
            viewHolder.undoButton.visibility = View.GONE
            viewHolder.undoButton.setOnClickListener(null)
        }
    }
}
