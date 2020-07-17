package s.yarlykov.izisandbox.dialogs

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.extensions.bindPhoneMask
import s.yarlykov.izisandbox.extensions.px

class RegistrationDialog(val context: Context, val inflater: LayoutInflater) {

    val disposable = CompositeDisposable()

    private val clientTypes = context.resources.getStringArray(R.array.client_types)
    private val clientSources = context.resources.getStringArray(R.array.client_sources)

    private val view = inflater.inflate(R.layout.layout_dialog_client_create, null)

    private val inputFio = view.findViewById<EditText>(R.id.input_fio)
    private val inputType = view.findViewById<TextInputLayout>(R.id.input_type)
    private val inputSource = view.findViewById<TextInputLayout>(R.id.input_source)
    private val buttonOk = view.findViewById<Button>(R.id.action_ok)
    private val phonesContainer = view.findViewById<LinearLayout>(R.id.phones_container)

    private val adapterTypes =
        ArrayAdapter(context, R.layout.layout_client_info_list_item, clientTypes)
    private val adapterSources =
        ArrayAdapter(context, R.layout.layout_client_info_list_item, clientSources)

    private val alertDialog: AlertDialog

    init {
        (inputType.editText as? AutoCompleteTextView)?.setAdapter(adapterTypes)
        (inputSource.editText as? AutoCompleteTextView)?.setAdapter(adapterSources)

        inflatePhoneViewV3(phonesContainer)

        alertDialog = AlertDialog.Builder(context).setView(view).create().apply {
            // Задать прозрачный фон окну диалога, чтобы использовать свой фон из макета
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

//        setPosition(alertDialog, 60)
    }

    fun show() {
        alertDialog.show()
    }

    private fun phonesObserver(phone: String) {
        inflatePhoneViewV1(phonesContainer)
    }

    private fun inflatePhoneViewV1(container: ViewGroup) {
        val root = inflater.inflate(R.layout.layout_item_add_phone_v1, container, true)
        root.id = root.hashCode()

        val phoneView = root.findViewById<EditText>(R.id.input_phone)
        phoneView.bindPhoneMask()

        val editText = root.findViewById<TextInputEditText>(R.id.input_phone)
        disposable += InputObservable.fromView(editText).subscribe(::phonesObserver)

        root.findViewById<ImageView>(R.id.phone_control).setOnClickListener {
            if (container.childCount > 1) {
                root.visibility = View.GONE
            }
        }
    }

    private fun inflatePhoneViewV3(container: ViewGroup) {
        val root =
            inflater.inflate(R.layout.layout_item_add_phone_v3, container, false)  as TextInputLayout

        root.apply {
            id = hashCode()

            val phoneView = findViewById<EditText>(R.id.input_phone)
            phoneView.bindPhoneMask()

            setEndIconOnClickListener {icon ->

                when {
                    // Если это иконка корзинки, то удалить элемент полностью
                    (icon.tag == true) -> {
                        container.removeView(root)
                    }
                    // Если это иконка (+), то проверить минимальное количество символов
                    // и создать новое поле для ввода телефона. Также сменить иконку на корзинку
                    // и пометить тегом true.
                    (icon is ImageView) -> {
                        if(phoneView.text.length > 5) {
                            icon.setImageResource(R.drawable.ic_delete)
                            icon.tag = true
                            inflatePhoneViewV3(container)
                            phoneView.isEnabled = false
                        }
                    }
                }
            }
        }

        container.addView(root)
    }

    private fun getPhones(ll: ViewGroup): List<String> {
        val list = mutableListOf<String>()

        for (i in 0 until ll.childCount) {
            val child = ll.getChildAt(i)

            if (child.visibility == View.VISIBLE) {

                val phoneView = child.findViewById<EditText>(R.id.input_phone)
                val phone = phoneView?.text.toString() ?: ""

                if (phone.isNotEmpty()) {
                    list.add(phone)
                }
            }
        }
        return list
    }

    /**
     * Задать позицию диалога на экране. Она работает, но пропадают контуры
     * окна диалога.
     */
    private fun setPosition(dialog: AlertDialog, yValue: Int, isDimBehind: Boolean = true) {
        dialog.window?.let { w ->

            /**
             * Если стереть этот флаг, то диалог не будт иметь ни контуров ни теней,
             * просто орисуется поверх другого контента. Если флаг не стирать,
             * то все что "ниже" окна диалога закрасится непрозрачным цветом.
             */
            if (!isDimBehind) {
                w.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }

            // Это LayoutParams окна
            w.attributes.apply {
                gravity = Gravity.TOP// or Gravity.CENTER_HORIZONTAL
                y = yValue.px

                // Двигает по горизонтали вправо/влево в зависимости от знака
//                horizontalMargin = -12.px.toFloat()
            }
        }
    }
}