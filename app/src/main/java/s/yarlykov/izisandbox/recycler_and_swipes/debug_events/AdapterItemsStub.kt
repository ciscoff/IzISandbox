package s.yarlykov.izisandbox.recycler_and_swipes.debug_events

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.izisandbox.R

class AdapterItemsStub(private val context: Context) : RecyclerView.Adapter<ViewHolderStub>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderStub {
        val view = ItemView(context).apply {
            val itemHeight = context.resources.getDimensionPixelSize(R.dimen.item_stub_height)
//            setBackgroundColor(Color.parseColor("#6b71e3"))
            layoutParams =
                RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight).apply {
                    marginStart = 20
                    marginEnd = 20
                    topMargin = if(viewType == 0) 20 else 10
                    bottomMargin = if(viewType == (itemCount - 1)) 20 else 10
                }
        }

        return ViewHolderStub(view)
    }

    override fun onBindViewHolder(holder: ViewHolderStub, position: Int) {
        // nothing
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int = 15
}