package s.yarlykov.izisandbox.extensions

import android.text.InputType
import android.widget.EditText
import com.redmadrobot.inputmask.MaskedTextChangedListener

fun EditText.bindTextMask(format: String): EditText {
    val listener = MaskedTextChangedListener(
        format, true, this, null,

        object : MaskedTextChangedListener.ValueListener {
            override fun onTextChanged(
                maskFilled: Boolean,
                extractedValue: String,
                formattedValue: String
            ) {

            }
        }
    )
    addTextChangedListener(listener)
    onFocusChangeListener = listener
    hint = listener.placeholder()
    setSelection(length())
    return this
}

fun EditText.bindPhoneMask(): EditText {
    inputType = InputType.TYPE_CLASS_PHONE
    return bindTextMask("+[0] [000] [000] [00] [00]")
}