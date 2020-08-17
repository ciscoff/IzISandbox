package s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.hash
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo.SwipeWithUndoActivity.Companion.PENDING_REMOVAL_TIMEOUT

class TestAdapterV2 : RecyclerView.Adapter<TestViewHolder>() {

    // Модель которую показываем (строки)
    private val model = mutableListOf<String>()

    private var initModelSize: Int = 15

    init {

        // Генерим элементы
        for (i in 0 until initModelSize) {
            model.add("Item $i")
        }
    }

    // Undo on/off. Настраивается из меню.
    var isUndoOn = false

    private val handler = Handler()

    // Элементы ожидающие удаления
    private var pendingRemovers = mutableMapOf<Int, Remover>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {

        return TestViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_swiped_row_view,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = model.size

    /**
     * Добавить элементы в модель
     */
    fun addItems(howMany: Int) {

        var next = model.lastIndex

        if (howMany > 0) {
            for (i in 0 until howMany) {
                model.add("Item ${++next}")
                notifyItemInserted(model.size - 1)
            }
        }
    }

    /**
     * Добавить в список на удаление
     */
    fun putInRemoval(position: Int) {
        val hash = model[position].hash

        if (!pendingRemovers.keys.contains(hash)) {

            val remover = object : Remover {
                override fun run() {
                    removeFromModel(hash)
                }
            }
            pendingRemovers[hash] = remover
            handler.postDelayed(remover, PENDING_REMOVAL_TIMEOUT)

            // Это вызовет повторную инициализацию соотв ViewHolder'а,
            // то есть будет вызван onBindViewHolder, и там элемент будет перерисован:
            // красный фон, кнопка Undo.
            notifyItemChanged(position)
        }
    }

    /**
     * Удалить элемент из модели (когда isUndoOn включен)
     */
    fun removeFromModel(hash: Int) {

        // Поиск в списке
        val index = model.map { it.hash }.indexOf(hash)

        if (index != -1) {

            // Если в списке на удаление, то удалить
            pendingRemovers.remove(hash)

            model.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Удалить элемент из модели (когда isUndoOn выключен)
     */
    fun removeInstantly(pos: Int) {

        if (pos <= model.lastIndex) {
            model.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    /**
     * Проверка обреченных на удаление
     */
    fun isPendingRemoval(position: Int): Boolean {
        val item = model[position]
        return pendingRemovers.keys.contains(item.hash)
    }

    override fun onBindViewHolder(viewHolder: TestViewHolder, position: Int) {

        val hash = model[position].hash

        // Отрисовка элемента в состоянии Undo.
        // Это красный фон, кнопка справа и скрытый текст
        if (pendingRemovers.keys.contains(hash)) {

            viewHolder.itemView.setBackgroundColor(Color.RED)
            viewHolder.titleTextView.visibility = View.GONE
            viewHolder.undoButton.visibility = View.VISIBLE

            // Нажатие на кнопку отменяет удаление. То есть нужно остановить
            // зашедуленый Remover, и перерисовать элемент в нормальное состояние.
            // Перерисовка будет выполнена повторным входом в onBindViewHolder
            // после вызова notifyItemChanged(position)
            viewHolder.undoButton.setOnClickListener {

                pendingRemovers[hash]?.let { remover ->
                    handler.removeCallbacks(remover)
                    pendingRemovers.remove(hash)
                    notifyItemChanged(position)
                }
            }
        }
        // Отрисовка элемента в нормальном состоянии
        else {
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            viewHolder.titleTextView.visibility = View.VISIBLE
            viewHolder.titleTextView.text = model[position]
            viewHolder.undoButton.visibility = View.GONE
            viewHolder.undoButton.setOnClickListener(null)
        }
    }
}
