package s.yarlykov.izisandbox.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionCatcher {

    const val REQUEST_PERM_LOCATION = 101
    const val REQUEST_PERM_CAMERA = 102
    const val REQUEST_PERM_READ_STORAGE = 103

    /**
     * Запрос разрешений на работу с координатами устройства
     */
    fun location(context: Context, observer: LiveDataT<Boolean>) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_PERM_LOCATION
                )
            }
        } else {
            observer.valueOrDefault = true
        }
    }

    /**
     * Запрос разрешений на работу с камерой. Это касается непосредственного
     * использования камеры как физического устройства. А если требуется сделать фотку,
     * то достаточно через Intent вызывать активити приложения для фотосъемки.
     */
    fun camera(context: Context, observer: LiveDataT<Boolean>) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.CAMERA
                )
            ) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.CAMERA
                    ),
                    REQUEST_PERM_CAMERA
                )
            }
        } else {
            observer.valueOrDefault = true
        }
    }

    fun gallery(context: Context, observer: LiveDataT<Boolean>) {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    context as Activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    REQUEST_PERM_READ_STORAGE
                )
            }
        } else {
            observer.valueOrDefault = true
        }
    }
}