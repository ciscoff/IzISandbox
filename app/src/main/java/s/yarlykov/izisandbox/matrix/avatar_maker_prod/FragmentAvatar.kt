package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import s.yarlykov.izisandbox.BuildConfig
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.databinding.FragmentFunnyAvatarBinding
import s.yarlykov.izisandbox.extensions.showResultNotification
import s.yarlykov.izisandbox.utils.PermissionCatcher
import s.yarlykov.izisandbox.utils.PhotoHelper

class FragmentAvatar : Fragment(R.layout.fragment_funny_avatar) {

    private var _binding: FragmentFunnyAvatarBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_GALLERY = 2
    }

    /**
     * Для работы с фотографиями
     */
    private var photoPath: String? = null
    private var photoURI: Uri? = null

    private var isCameraPermitted = false
    private var isGalleryPermitted = false
    private val model: FunnyAvatarActivity.LocalModel by lazy {
        ViewModelProvider(this).get(FunnyAvatarActivity.LocalModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFunnyAvatarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        PermissionCatcher.apply {
            camera(requireContext(), model.permissionCamera)
            gallery(requireContext(), model.permissionStorage)
        }

        model.permissionCamera.observe(viewLifecycleOwner) {
            isCameraPermitted = it
        }

        model.permissionStorage.observe(viewLifecycleOwner) {
            isGalleryPermitted
        }

        binding.avatarView.liveURI = model.avatarLiveUri

        binding.fabCamera.setOnClickListener {
            takePhoto(requireContext())
        }

        binding.avatarView.setOnClickListener {
            findNavController().navigate(R.id.action_from_viewer_to_maker)
        }

        binding.avatarView.setImageResource(R.drawable.shape_oval_gray)
    }

    /**
     * Сфотать аватарку с помощью системного софта работы с камерой.
     *
     * В photoPath/photoURI сохраняем путь к файлу в разных форматах для последующей работы
     * с файлом разными способами.
     */
    private fun takePhoto(context: Context) {
        if (!isCameraPermitted) return

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            try {
                PhotoHelper.createImageFile(context).also { file ->
                    photoPath = file.path
                    photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        BuildConfig.APPLICATION_ID,
                        file
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        FunnyAvatarActivity.REQUEST_IMAGE_CAPTURE
                    )
                }
            } catch (e: Exception) {
                binding.fabCamera.showResultNotification(R.string.camera_access_issue, false)
            }
        }
    }

}