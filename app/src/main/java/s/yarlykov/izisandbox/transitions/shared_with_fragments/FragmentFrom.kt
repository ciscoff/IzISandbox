package s.yarlykov.izisandbox.transitions.shared_with_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.transition.TransitionInflater
import s.yarlykov.izisandbox.R

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
            .inflateTransition(R.transition.change_bounds)

        this.sharedElementReturnTransition = transition
        fragmentTo.sharedElementEnterTransition = transition

        for (i in 0 until root.childCount) {
            root.getChildAt(i).apply {
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    startFragmentTo(it, fragmentTo)
                }
            }
        }
    }


    /**
     * Нужно использовать supportFragmentManager, а не childFragmentManager
     */
    private fun startFragmentTo(view : View, fragmentTo : Fragment) {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_placeholder, fragmentTo, fragmentTo::class.java.simpleName)
            ?.addToBackStack(null)
            ?.addSharedElement(view, view.transitionName)
            ?.commit()
    }
}