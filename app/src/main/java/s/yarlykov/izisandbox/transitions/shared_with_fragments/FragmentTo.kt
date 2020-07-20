package s.yarlykov.izisandbox.transitions.shared_with_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import s.yarlykov.izisandbox.R

class FragmentTo : Fragment() {

    companion object {
        const val KEY_LAYOUT_ID_TO = "KEY_LAYOUT_ID_TO"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layoutId = arguments?.getInt(KEY_LAYOUT_ID_TO) ?: R.layout.fragment_shared_to_1
        return inflater.inflate(layoutId, container, false)
    }
}