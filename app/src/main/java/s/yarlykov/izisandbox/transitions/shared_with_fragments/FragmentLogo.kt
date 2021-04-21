package s.yarlykov.izisandbox.transitions.shared_with_fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.application.App
import s.yarlykov.izisandbox.transitions.shared_with_fragments.di.ModuleFragmentLogo
import javax.inject.Inject

class FragmentLogo : Fragment() {

    @Inject
    lateinit var title : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logo, container, false)

        (view.context.applicationContext as App)
            .appComponent
            .builderComponentFragmentLogo
            .plus(ModuleFragmentLogo(R.string.title_select_example))
            .build()
            .inject(this)

        view.findViewById<TextView>(R.id.tvTitle).text = title

        return view
    }
}