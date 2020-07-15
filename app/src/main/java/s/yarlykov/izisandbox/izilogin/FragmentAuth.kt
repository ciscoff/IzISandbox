package s.yarlykov.izisandbox.izilogin

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.bindPhoneMask

class FragmentAuth : Fragment() {

    private lateinit var snackbarWrapper: CoordinatorLayout
    private lateinit var til: TextInputLayout
    private lateinit var passInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var btnAuth: Button
    private lateinit var phone: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_auth, container, false)

        findViews(root)
        initViews()
        return root
    }

    private fun findViews(root: View) {
        snackbarWrapper = root.findViewById(R.id.snackbar_wrapper)
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
        val snackbar = Snackbar
            .make(snackbarWrapper, message, Snackbar.LENGTH_SHORT)
            .setTextColor(Color.WHITE)
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.colorAuthSnackBar))

        val snackBarLayout = snackbar.view as Snackbar.SnackbarLayout

        for (i in 0 until snackBarLayout.childCount) {
            val parent = snackBarLayout.getChildAt(i)
            if (parent is LinearLayout) {
                parent.setRotation(180f)
                break
            }
        }
        snackbar.show()
    }
}

/**
 * Рекурсивный проход по иерархии View
 */
private fun showChild(parent: ViewGroup) {
    for (i in 0 until parent.childCount) {
        parent.getChildAt(i)?.let { child ->

            if (child !is ViewGroup) {
                Log.d(
                    "TAG_TAG",
                    "class=${child::class.java.simpleName}, id=${child.id}, parent=${child.parent::class.java.simpleName}"
                )
            } else {
                Log.d("TAG_TAG", "I'm View Group ${child::class.java.simpleName}")
                showChild(child as ViewGroup)
            }

            child.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
