package s.yarlykov.izisandbox.theme

import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.utils.logIt

class InputDataWatcher(
    view: EditText,
    private val cslNoText: ColorStateList,
    private val cslWithText: ColorStateList
) : TextWatcher {

    companion object {
        /**
         * Рекурсивно найти родителя класса TextInputLayout
         */
        fun View.findParent(): TextInputLayout? {
            return parent?.let {
                if (it is TextInputLayout) {
                    it
                } else {
                    (it as View).findParent()
                }
            }
        }
    }

    private val viewParent = view.findParent()

    override fun afterTextChanged(s: Editable?) {
        logIt("afterTextChanged: ${s.toString()}", "PLPLPL")
        val csl = if (s.toString().isNotEmpty()) {
            logIt("afterTextChanged: with text", "PLPLPL")
            cslWithText
        } else {
            logIt("afterTextChanged: no text", "PLPLPL")
            cslNoText
        }
        viewParent?.setBoxStrokeColorStateList(csl)
        viewParent?.defaultHintTextColor = csl
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        logIt("beforeTextChanged: ${s.toString()}", "PLPLPL")
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        logIt("onTextChanged: ${s.toString()}", "PLPLPL")
    }
}