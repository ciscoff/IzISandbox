package s.yarlykov.izisandbox.ui.stub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import s.yarlykov.izisandbox.R

class StubFragment : Fragment() {

    private lateinit var stubViewModel: StubViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        stubViewModel =
            ViewModelProvider(this).get(StubViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_stub, container, false)

        val textView: TextView = root.findViewById(R.id.text_stub)
        stubViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}