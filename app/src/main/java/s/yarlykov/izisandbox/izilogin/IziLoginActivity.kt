package s.yarlykov.izisandbox.izilogin

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.application.App
import s.yarlykov.izisandbox.utils.logIt
import javax.inject.Inject

class IziLoginActivity : AppCompatActivity() {

    @Inject
    lateinit var baseUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_izi_login)

        // Скрываем Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        (application as App).appComponent.componentLoginActivity.inject(this)
    }

    override fun onStart() {
        super.onStart()

        val message = if (::baseUri.isInitialized) {
            baseUri.toString()
        } else {
            getString(R.string.dagger_injection_fuck_up)
        }

        logIt("${this::class.simpleName}::${object {}.javaClass.enclosingMethod?.name} '$message'")
    }
}