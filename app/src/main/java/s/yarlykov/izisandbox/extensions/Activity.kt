package s.yarlykov.izisandbox.extensions

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.Utils.logIt
import java.util.logging.Handler


fun Activity.createRegistrationFormDialog() {

    fun inflatePhoneView(container : ViewGroup) {
        val root = layoutInflater.inflate(R.layout.layout_item_add_phone, container, true)

        val phoneView = root.findViewById<EditText>(R.id.input_phone)
        phoneView.bindPhoneMask()

        root.findViewById<ImageView>(R.id.delete_phone).setOnClickListener {

            if(container.childCount > 1) {
                root.visibility = View.GONE
            }
        }
    }

    val clientTypes = resources.getStringArray(R.array.client_types)
    val clientSources = resources.getStringArray(R.array.client_sources)

    val view = layoutInflater.inflate(R.layout.layout_dialog_client_create, null)

    val inputFio = view.findViewById<EditText>(R.id.input_fio)
    val inputType =  view.findViewById<TextInputLayout>(R.id.input_type)
    val inputSource =  view.findViewById<TextInputLayout>(R.id.input_source)
    val buttonOk = view.findViewById<Button>(R.id.action_ok)
    val phonesContainer = view.findViewById<LinearLayout>(R.id.phones_container)

    val adapterTypes = ArrayAdapter(this, R.layout.layout_client_info_list_item, clientTypes)
    val adapterSources = ArrayAdapter(this, R.layout.layout_client_info_list_item, clientSources)

    (inputType.editText as? AutoCompleteTextView)?.setAdapter(adapterTypes)
    (inputSource.editText as? AutoCompleteTextView)?.setAdapter(adapterSources)

    inflatePhoneView(phonesContainer)

    val dialog = AlertDialog.Builder(this).setView(view)
//        .setPositiveButton(resources.getString(R.string.action_ok), null)
        .create()

    // Задать прозрачный фон окну диалога, чтобы использовать свой фон из макета
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    // Здесь пока ничего не нужно. Кнопка есть в макете.
//    dialog.setOnShowListener{
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.colorAccentUi))
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
//            // TODO Здесь будем обрабатывать пользовательский выбор
//         }
//    }

    dialog.show()



}