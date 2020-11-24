package s.yarlykov.izisandbox.matrix.avatar_maker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.activity_funny_avatar.*
import s.yarlykov.izisandbox.BuildConfig
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.showResultNotification
import s.yarlykov.izisandbox.utils.LiveDataT
import s.yarlykov.izisandbox.utils.PermissionCatcher
import s.yarlykov.izisandbox.utils.PhotoHelper
import java.lang.IllegalArgumentException

class FunnyAvatarActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_GALLERY = 2
    }

    private var isCameraPermitted = false
    private var isGalleryPermitted = false
    private val model: LocalModel by lazy {
        ViewModelProvider(this).get(LocalModel::class.java)
    }

    /**
     * Для работы с фотографиями
     */
    private var photoPath: String? = null
    private var photoURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_funny_avatar)

        PermissionCatcher.apply {
            camera(this@FunnyAvatarActivity, model.permissionCamera)
            gallery(this@FunnyAvatarActivity, model.permissionStorage)
        }

        model.permissionCamera.observe(this) {
            isCameraPermitted = it
        }

        model.permissionStorage.observe(this) {
            isGalleryPermitted
        }

        avatarView.liveURI = model.avatarLiveUri

        fabCamera.setOnClickListener {
            takePhoto(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                // TODO показать диалог с thumbnail и кнопкой ОК
                // TODO при нажатии на которую отправить запрос на обновление в REST
                if (PhotoHelper.reduceImageFile(this, photoURI!!, photoPath!!)) {
                    model.avatarLiveUri.value = photoURI!!
                }
            }
            REQUEST_IMAGE_GALLERY -> {
                data?.let { intent ->

                    val imgPath = PhotoHelper.createImageFile(this).path

                    if (PhotoHelper.reduceImageFile(this, intent.data as Uri, imgPath)) {
                        // TODO
                    }
                }
            }
        }
    }

    /**
     * Сфотать аватарку с помощью системного софта работы с камерой.
     *
     * В photoPath/photoURI сохраняем путь к файлу в разных форматах для последующей
     * работой с файлом разными способами.
     */
    private fun takePhoto(context: Context) {
        if (!isCameraPermitted) return

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            try {
                PhotoHelper.createImageFile(context).also { file ->
                    photoPath = file.path
                    photoURI = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID,
                        file
                    )

                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } catch (e: Exception) {
                fabCamera.showResultNotification(R.string.camera_access_issue, false)
            }
        }
    }

    private fun takeImage() {

    }

    class LocalModel : ViewModel() {
        val permissionCamera = LiveDataT(false)
        val permissionStorage = LiveDataT(false)
        val avatarLiveUri = LiveDataT<Uri>(Uri.EMPTY)
    }
}