package s.yarlykov.izisandbox.recycler_and_swipes.smart_adapter.sand_box.java;

import androidx.recyclerview.widget.RecyclerView;

public class BaseItem<H extends RecyclerView.ViewHolder> {

    /**
     * Next item in ItemList
     */
    public BaseItem<H> nextItem;

    /**
     * Previous item in ItemList
     */
    public BaseItem<H> previousItem;

}