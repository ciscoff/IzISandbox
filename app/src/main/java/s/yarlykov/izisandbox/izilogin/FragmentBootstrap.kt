package s.yarlykov.izisandbox.izilogin

import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import s.yarlykov.izisandbox.R

class FragmentBootstrap : Fragment() {

    private lateinit var logoAnimation: Animatable
    private lateinit var logoImage: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_bootstrap, container, false)

        root.setOnClickListener {
            findNavController().apply {
                navigate(R.id.action_to_auth)
            }
        }

        findViews(root)
        return root
    }

    override fun onResume() {
        super.onResume()
        showAnimation()
    }

    private fun findViews(root : View) {
        logoImage = root.findViewById(R.id.anim_logo)
        logoAnimation = (logoImage).drawable as Animatable
    }

    private fun showAnimation() {
        logoImage.visibility = View.VISIBLE
        logoAnimation.start()
    }
}