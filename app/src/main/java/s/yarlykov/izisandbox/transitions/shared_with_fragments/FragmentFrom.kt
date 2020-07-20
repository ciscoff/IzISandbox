package s.yarlykov.izisandbox.transitions.shared_with_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.transition.TransitionInflater
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt

class FragmentFrom : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_shared_from, container, false)
        initViews(root as ViewGroup)

        return root
    }

    private fun initViews(root: ViewGroup) {
        val fragmentTo = FragmentTo()

        val transition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.slide_and_changebounds_sequential)

        this.sharedElementReturnTransition = transition
        fragmentTo.sharedElementEnterTransition = transition

        val sharedViews = arrayListOf<View>()
        collectSharedViews(root, sharedViews)

        sharedViews.forEach {
            it.isClickable = true
            it.isFocusable = true

            it.setOnClickListener {
                startFragmentTo(sharedViews, fragmentTo)
            }
        }
    }

    /**
     * Рекурсивно собрать в array все дочерние shared views.
     * Они НЕ ViewGroup и имеют transition name.
     */
    private fun collectSharedViews(root : ViewGroup, array : ArrayList<View>) {
        for(i in 0 until root.childCount) {
            val child = root.getChildAt(i)

            if(child is ViewGroup) {
                collectSharedViews(child, array)
            } else if (child.transitionName != null ){
                array.add(child)
            }
        }
    }

    /**
     * Нужно использовать supportFragmentManager, а не childFragmentManager
     */
    private fun startFragmentTo(views : ArrayList<View>, fragmentTo : Fragment) {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_placeholder, fragmentTo, fragmentTo::class.java.simpleName)
            ?.addToBackStack(null)
            ?.addSharedElements(views)
            ?.commit()
    }
}

fun FragmentTransaction.addSharedElements(views : ArrayList<View>) : FragmentTransaction {

    views.forEach {
        addSharedElement(it, it.transitionName)
    }
    return this
}