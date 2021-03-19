package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.databinding.FragmentEditorAvatarBinding
import s.yarlykov.izisandbox.utils.args

class FragmentMaker : Fragment(R.layout.fragment_editor_avatar) {

    private var _binding: FragmentEditorAvatarBinding? = null
    private val binding get() = _binding!!

    private val bitmapPath: String by args()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorAvatarBinding.inflate(inflater, container, false)
        binding.root.bitmapPath = bitmapPath
        return binding.root
    }

    companion object {
        fun bundle(bitmapPath: String) = bundleOf("bitmapPath" to bitmapPath)
    }
}