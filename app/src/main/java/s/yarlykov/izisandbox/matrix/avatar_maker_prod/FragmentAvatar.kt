package s.yarlykov.izisandbox.matrix.avatar_maker_prod

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import s.yarlykov.izisandbox.BuildConfig
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.databinding.FragmentFunnyAvatarBinding
import s.yarlykov.izisandbox.extensions.showResultNotification
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.vm.AvatarViewModel
import s.yarlykov.izisandbox.utils.PermissionCatcher
import s.yarlykov.izisandbox.utils.PhotoHelper

/**
 * NOTE: На сраном планшете HUAWEI я столкнулся с такой проблемой:
 * http://android-gex.blogspot.com/2013/03/the-problem-with-external-camera-in.html
 *
 * В общем после возврата из активити камеры пересоздавалась моя FunnyAvatarActivity
 * и этот фрагмент. Соответственно обнулялись photoPath и photoURI и получался крэш
 * в onActivityResult.
 *
 * Решение: Сохранять photoPath и photoURI в бандле в методе onSaveInstanceState.
 *
 * NOTE: И кстати этот хуявей перезапускает целиком приложение при смене ориентации
 * экрана. При этом onSaveInstanceState отрабатывает правильно. Пришлось в манифесте
 * в разделе активити приколотить android:screenOrientation="portrait".
 */
class FragmentAvatar : Fragment(R.layout.fragment_funny_avatar) {

    private var _binding: FragmentFunnyAvatarBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_GALLERY = 2

        const val KEY_URI = "KEY_URI"
        const val KEY_PATH = "KEY_PATH"
    }

    /**
     * Для работы с фотографиями
     */
    private var photoPath: String? = null
    private var photoURI: Uri? = null

    private var isCameraPermitted = false
    private var isGalleryPermitted = false
    private val viewModel: AvatarViewModel by navGraphViewModels(R.id.nav_avatar_graph)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFunnyAvatarBinding.inflate(inflater, container, false)

        savedInstanceState?.let { bundle ->
            photoURI = bundle.getString(KEY_URI)?.let { Uri.parse(it) }
            photoPath = bundle.getString(KEY_PATH)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /** Запросить разрешения на работу с камерой и файлами */
        PermissionCatcher.apply {
            camera(requireContext(), viewModel.permissionCamera)
            gallery(requireContext(), viewModel.permissionStorage)
        }

        viewModel.permissionCamera.observe(viewLifecycleOwner) {
            isCameraPermitted = it
        }

        viewModel.permissionStorage.observe(viewLifecycleOwner) {
            isGalleryPermitted = it
        }

        viewModel.bitmapLive.observe(viewLifecycleOwner) {
            binding.avatarView.setImageBitmap(it)
        }

        binding.avatarView.liveURI = viewModel.avatarLiveUri

        binding.fabCamera.setOnClickListener {
            takePhoto(requireContext())
        }

        binding.avatarView.setOnClickListener {
            takeGalleryImage()
        }

        binding.avatarView.setImageResource(R.drawable.shape_oval_gray)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        photoURI?.let { outState.putString(KEY_URI, it.toString()) }
        photoPath?.let { outState.putString(KEY_PATH, it) }
    }

    /**
     * Обработка фотографий.
     *
     * NOTE: Когда возвращаемся из активити камеры, то data == null.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                photoURI?.let { uri ->
                    photoPath?.let { path -> startEditor(path, uri) }
                }
            }
            REQUEST_IMAGE_GALLERY -> {
                data?.let { intent ->
                    (intent.data)?.let { uri -> startEditor(null, uri) }
                }
            }
        }
    }

    /**
     * Запуск фрагмента для создания аватара
     */
    private fun startEditor(path: String?, uri: Uri? = null) {
        findNavController().navigate(
            R.id.action_from_viewer_to_maker,
            FragmentMaker.bundle(path, uri)
        )
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
                        REQUEST_IMAGE_CAPTURE
                    )
                }
            } catch (e: Exception) {
                binding.fabCamera.showResultNotification(R.string.camera_access_issue, false)
            }
        }
    }

    private fun takeGalleryImage() {

        if (isGalleryPermitted) {
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(
                Intent.createChooser(
                    intent,
                    requireContext().getString(R.string.fragment_avatar_select_image)
                ), REQUEST_IMAGE_GALLERY
            )
        } else {
            // TODO Диалог с сообщением о недостатке permissions
        }
    }

}