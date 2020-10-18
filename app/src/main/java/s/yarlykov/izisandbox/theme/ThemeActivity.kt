package s.yarlykov.izisandbox.theme

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.R

class ThemeActivity : AppCompatActivity() {

    lateinit var inputOne : TextInputLayout
    lateinit var inputTwo : TextInputLayout
    lateinit var editText : TextInputEditText
    lateinit var inputChild : AutoCompleteTextView

    lateinit var tiLast : TextInputLayout
    lateinit var etLast : TextInputEditText

    lateinit var cslInit : ColorStateList
    lateinit var cslWork : ColorStateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        findView()
        initViews()
    }

    private fun findView() {

        cslInit = ContextCompat.getColorStateList(this, R.color.selector_box_and_label_init)!!
        cslWork = ContextCompat.getColorStateList(this, R.color.selector_box_and_label_work)!!

        inputOne = findViewById(R.id.input_first)
        inputTwo = findViewById(R.id.input_third)
        editText = findViewById(R.id.input_text_first)
        inputChild = findViewById(R.id.input_children)

        tiLast = findViewById(R.id.input_last)
        etLast = findViewById(R.id.input_text_last)
    }

    private fun initViews() {

        val children = resources.getStringArray(R.array.children)
        val adapterChildren = ArrayAdapter(this, R.layout.layout_child_info_list_item, children)
        inputChild.setAdapter(adapterChildren)

        tiLast.setBoxStrokeColorStateList(cslInit)

        etLast.setOnFocusChangeListener { v, hasFocus ->

            val view = v as EditText

            if(hasFocus) {
                if(view.text.toString().isNotEmpty()) {
                    tiLast.setBoxStrokeColorStateList(cslWork)
                } else {
                    tiLast.setBoxStrokeColorStateList(cslWork)
                }
            } else {
                if(view.text.toString().isNotEmpty()) {
                    tiLast.setBoxStrokeColorStateList(cslWork)
                } else {
                    tiLast.setBoxStrokeColorStateList(cslInit)
                }
            }

        }
    }
}