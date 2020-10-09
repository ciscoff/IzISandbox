package s.yarlykov.izisandbox.recycler_and_swipes.items_animation.item_animators

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class CustomItemAnimatorV1 : DefaultItemAnimator() {

    override fun canReuseUpdatedViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ): Boolean = true

    /**
     * Этот метод вызывается RecyclerView перед началом фазы layout. В нём мы (при необходимости)
     * можем прочитать какую-нибудь инфу из ViewHolder'а или его View и сохранить в возвращаемом
     * инстансе ItemHolderInfo. Потом эта инфа будет передана в метод анимации.
     *
     * @payloads
     *  - вообще это данные, которые были переданы методу Adapter.notifyItemChanged(int, Object).
     *    видимо MutableList - это обертка для Object, чтобы не работать с null.
     *
     * Предлагаю поступить так: каждый элемент имеет TextView, где хранит число, которое будет
     * определять продолжительность анимации.
     */
    override fun recordPreLayoutInformation(
        state: RecyclerView.State,
        viewHolder: RecyclerView.ViewHolder,
        changeFlags: Int,
        payloads: MutableList<Any>
    ): ItemHolderInfo {

        if (changeFlags == RecyclerView.ItemAnimator.FLAG_CHANGED) {

            /**
             * Наша задача взять из холдера целочисленное значение и сохранить
             */
            if (viewHolder is ViewHolderDoodle) {
                return CustomItemHolderInfo(viewHolder.tv.text.toString().toInt())
            }
        }
        return super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads)
    }

    /**
     * Called by the RecyclerView when an adapter item is present both before and after the layout and RecyclerView
     * has received a notifyItemChanged(int) call for it. This method may also be called when notifyDataSetChanged()
     * is called and adapter has stable ids so that RecyclerView could still rebind views to the same ViewHolders.
     */
    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {

        if (preInfo is CustomItemHolderInfo
            && oldHolder is ViewHolderDoodle
            && preInfo.duration != 0
        ) {
            animateItemView(oldHolder, preInfo.duration)
        }

        return super.animateChange(oldHolder, newHolder, preInfo, postInfo)
    }

    private fun animateItemView(holder: ViewHolderDoodle, _duration: Int) {

        holder.iv.scaleX = 0.0f
        holder.iv.scaleY = 0.0f

        ObjectAnimator.ofPropertyValuesHolder(
            holder.iv,
            PropertyValuesHolder.ofFloat("scaleX", 0.0f, 2.0f, 1.0f),
            PropertyValuesHolder.ofFloat("scaleY", 0.0f, 2.0f, 1.0f),
            PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f)
        ).apply {
            duration = _duration.toLong()
        }.start()
    }
}