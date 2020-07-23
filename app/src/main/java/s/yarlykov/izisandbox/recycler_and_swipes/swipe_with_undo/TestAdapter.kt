package s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo

import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo.SwipeWithUndoActivity.Companion.PENDING_REMOVAL_TIMEOUT

class TestAdapter : RecyclerView.Adapter<TestViewHolder>() {

    // Модель которую показываем (строки)
    private val items = mutableListOf<String>()

    // Элементы ожидающие удаления (строки)
    private val itemsPendingRemoval = mutableListOf<String>()

    private var lastInsertedIndex: Int = 15

    init {

        for (i in 1..lastInsertedIndex) {
            items.add("Item $i")
        }
    }

    // is undo on, you can turn it on from the toolbar menu
    var isUndoOn = false

    // handler for running delayed runnables
    private val handler = Handler()

    // map of items to pending runnables, so we can cancel a removal if need be
    private var pendingRunnables = mutableMapOf<String, Runnable>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {

        return TestViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_swiped_row_view,
                parent,
                false)
        )
    }

    override fun getItemCount(): Int = items.size

    /**
     * Utility method to add some rows for testing purposes. You can add rows from the toolbar menu.
     */
    fun addItems(howMany: Int) {

        if (howMany > 0) {

            for (i in lastInsertedIndex + 1..lastInsertedIndex + howMany) {
                items.add("Item $i")
                notifyItemInserted(items.size - 1)
            }
            lastInsertedIndex += howMany
        }
    }

    fun pendingRemoval(position: Int) {
        val item = items[position]

        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item)

            // Это вызовет повторную инициализацию соотв ViewHolder'а,
            // то есть будет вызван onBindViewHolder
            notifyItemChanged(position)

            // let's create, store and post a runnable to remove the item
            val pendingRemovalRunnable = Runnable { removeFromModel(position) }

            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT)
            pendingRunnables[item] = pendingRemovalRunnable
        }
    }

    /**
     * Удалить элемент из модели
     */
    fun removeFromModel(position: Int) {
        val item = items[position]

        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item)
        }
        if (items.contains(item)) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun isPendingRemoval(position: Int): Boolean {
        val item = items[position]
        return itemsPendingRemoval.contains(item)
    }


    override fun onBindViewHolder(viewHolder: TestViewHolder, position: Int) {

        val item = items[position]

        if (itemsPendingRemoval.contains(item)) {

            // we need to show the "undo" state of the row
            viewHolder.itemView.setBackgroundColor(Color.RED)
            viewHolder.titleTextView.visibility = View.GONE
            viewHolder.undoButton.visibility = View.VISIBLE
            viewHolder.undoButton.setOnClickListener {

                // user wants to undo the removal, let's cancel the pending task

                val pendingRemovalRunnable = pendingRunnables[item]
                pendingRunnables.remove(item)

                if (pendingRemovalRunnable != null) handler.removeCallbacks(
                    pendingRemovalRunnable
                )
                itemsPendingRemoval.remove(item)

                // this will rebind the row in "normal" state
                notifyItemChanged(items.indexOf(item))

            }
        } else {
            // we need to show the "normal" state
            viewHolder.itemView.setBackgroundColor(Color.WHITE)
            viewHolder.titleTextView.visibility = View.VISIBLE
            viewHolder.titleTextView.text = item
            viewHolder.undoButton.visibility = View.GONE
            viewHolder.undoButton.setOnClickListener(null)
        }
    }
}
