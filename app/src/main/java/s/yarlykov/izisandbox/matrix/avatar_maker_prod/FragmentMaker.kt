package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.databinding.FragmentEditorAvatarBinding
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm.AvatarViewModel
import s.yarlykov.izisandbox.utils.args

class FragmentMaker : Fragment(R.layout.fragment_editor_avatar) {

    private var _binding: FragmentEditorAvatarBinding? = null
    private val binding get() = _binding!!

    private val bitmapPath: String by args()
    private val bitmapUri: String by args()

    private lateinit var viewModel: AvatarViewModel

    /**
     * Job'а корутины с подписками на Flow. Эти подписки нужно отменять при onDestroyView потому,
     * что фрагмент уничтожается, но viewModelScope продолжает жить и срать в эти Flow. И crash !
     */
    private lateinit var jobStateFlow: Job

    @ExperimentalCoroutinesApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorAvatarBinding.inflate(inflater, container, false)
        binding.root.bitmapPath = bitmapPath
        binding.root.bitmapUri = Uri.parse(bitmapUri)

        return binding.root
    }

    /**
     * Отменить корутины
     */
    override fun onDestroyView() {
        super.onDestroyView()
        jobStateFlow.cancel()
    }

    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =
            ViewModelProvider(requireContext() as AppCompatActivity).get(
                AvatarViewModel::class.java
            )

        jobStateFlow = viewModel.viewModelScope.launch {
            launch {
                viewModel.cancelState
                    .filter { it != 0L }
                    .collect { findNavController().navigateUp() }
            }
            launch {
                viewModel.readyState
                    .collect { findNavController().navigateUp() }
            }
        }
    }

    companion object {
        fun bundle(bitmapPath: String?, bitmapUri: Uri? = null) =
            bundleOf(
                "bitmapPath" to (bitmapPath ?: ""),
                "bitmapUri" to (bitmapUri?.toString() ?: Uri.EMPTY.toString())
            )
    }
}