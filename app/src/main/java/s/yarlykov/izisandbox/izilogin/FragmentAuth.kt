package s.yarlykov.izisandbox.izilogin

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.application.App
import s.yarlykov.izisandbox.extensions.bindPhoneMask
import s.yarlykov.izisandbox.extensions.showSnackBarNotification
import s.yarlykov.izisandbox.izilogin.di.ModuleFragmentAuth
import s.yarlykov.izisandbox.utils.logIt
import javax.inject.Inject
import javax.inject.Named

class FragmentAuth : Fragment() {

    private lateinit var snackBarWrapper: CoordinatorLayout
    private lateinit var til: TextInputLayout
    private lateinit var passInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var btnAuth: Button
    private lateinit var phone: String

    @Inject
    lateinit var names : Flow<String>

    @Inject
    @Named("auth")
    lateinit var authUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_auth, container, false)

        (requireContext().applicationContext as App)
            .appComponent
            .componentLoginActivity
            .componentAuthBuilder
            .addModule(ModuleFragmentAuth("/auth"))
            .build()
            .inject(this)

        findViews(root)
        initViews()
        return root
    }

    @InternalCoroutinesApi
    override fun onResume() {
        super.onResume()
        logIt("${this::class.simpleName}::${object {}.javaClass.enclosingMethod?.name} $authUri")

        viewLifecycleOwner.lifecycleScope.launch {
            names.collect {
                requireView().showSnackBarNotification(it)
            }
        }
    }

    private fun findViews(root: View) {
        snackBarWrapper = root.findViewById(R.id.snackbar_wrapper)
        phoneInput = root.findViewById(R.id.phone_input)
        passInput = root.findViewById(R.id.pass_input)
        btnAuth = root.findViewById(R.id.btn_auth)

        til = root.findViewById(R.id.text_input_layout)

    }

    private fun initViews() {
        // Форматирование вводимого номера телефона по маске "+[0] [000] [000] [00] [00]"
        phoneInput.bindPhoneMask()

        btnAuth.setOnClickListener { view ->

            phone = phoneInput.text
                .toString()
                .replace("+", "")
                .replace(" ", "")

            showNotification(passInput.text.toString())
            view.isEnabled = false

            view.postDelayed({
                view.isEnabled = true
            }, 2000)
        }
    }

    /**
     * Вывести сообщение
     */
    private fun showNotification(message: String) {
        val snackBar = Snackbar
            .make(snackBarWrapper, message, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.WHITE)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorAuthSnackBar))

        val snackBarLayout = snackBar.view as Snackbar.SnackbarLayout

        for (i in 0 until snackBarLayout.childCount) {
            val parent = snackBarLayout.getChildAt(i)
            if (parent is LinearLayout) {
                parent.setRotation(180f)
                break
            }
        }
        snackBar.show()
    }
}
