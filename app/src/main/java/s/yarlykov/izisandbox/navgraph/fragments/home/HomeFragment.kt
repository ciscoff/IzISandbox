package s.yarlykov.izisandbox.navgraph.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import s.yarlykov.izisandbox.R

class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        navController = findNavController()

        val buttonToStub: Button = root.findViewById(R.id.button_to_stub)
        buttonToStub.setOnClickListener(this)

        val buttonToGraph2: Button = root.findViewById(R.id.button_to_graph_2)
        buttonToGraph2.setOnClickListener(this)

        return root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button_to_stub -> {
                navController.navigate(R.id.action_to_nav_stub)
            }
            R.id.button_to_graph_2 -> {
                navController.navigate(R.id.action_to_graph_2)
            }
            else -> {
            }
        }

    }
}