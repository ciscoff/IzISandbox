package s.yarlykov.izisandbox.theme

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.R

class ThemeActivity : AppCompatActivity() {

    lateinit var inputOne : TextInputLayout
    lateinit var inputTwo : TextInputLayout
    lateinit var editText : TextInputEditText
    lateinit var inputChild : AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        findView()
        initViews()
    }

    private fun findView() {
        inputOne = findViewById(R.id.input_first)
        inputTwo = findViewById(R.id.input_third)
        editText = findViewById(R.id.input_text_first)
        inputChild = findViewById(R.id.input_children)

    }

    private fun initViews() {

        val children = resources.getStringArray(R.array.children)
        val adapterChildren = ArrayAdapter(this, R.layout.layout_child_info_list_item, children)
        inputChild.setAdapter(adapterChildren)
    }
}