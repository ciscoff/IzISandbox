package s.yarlykov.izisandbox.theme

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import s.yarlykov.izisandbox.R

class ThemeActivity : AppCompatActivity() {

    lateinit var inputOne: TextInputLayout
    lateinit var inputTwo: TextInputLayout
    lateinit var editText: TextInputEditText
    lateinit var inputChild: AutoCompleteTextView

    lateinit var inputStyled: TextInputLayout
    lateinit var inputStyledText: TextInputEditText

    lateinit var inputDumb: TextInputLayout
    lateinit var inputDumbText: TextInputEditText


    lateinit var cslNoText: ColorStateList
    lateinit var cslWithText: ColorStateList

    lateinit var button : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        findView()
        initViews()
    }

    private fun findView() {

        cslNoText = ContextCompat.getColorStateList(this, R.color.selector_box_without_text)!!
        cslWithText = ContextCompat.getColorStateList(this, R.color.selector_box_with_text)!!

        inputOne = findViewById(R.id.input_first)
        inputTwo = findViewById(R.id.input_third)
        editText = findViewById(R.id.input_text_first)
        inputChild = findViewById(R.id.input_children)

        button = findViewById(R.id.textButton)

        inputStyled = findViewById(R.id.inputStyled)
        inputStyledText = findViewById(R.id.inputStyledText)

        inputDumb = findViewById(R.id.inputDumb)
        inputDumbText = findViewById(R.id.inputDumbText)
    }

    private fun initViews() {

        val children = resources.getStringArray(R.array.children)
        val adapterChildren = ArrayAdapter(this, R.layout.layout_child_info_list_item, children)
        inputChild.setAdapter(adapterChildren)

        inputStyled.setBoxStrokeColorStateList(cslNoText)
        inputStyledText.onFocusChangeListener = onFocusListener

        button.setOnClickListener {
            inputDumbText.setText(R.string.app_name)
        }

        inputDumbText.addTextChangedListener(InputDataWatcher(inputDumbText, cslNoText, cslWithText))
    }

    /**
     * Обработчик смены фокуса.
     * Во-первых для изменения цвета рамки нужно использовать не метод setBoxStrokeColor, а метод
     * setBoxStrokeColorStateList, в который передаем ColorStateList. Во-вторых, основным моментом
     * здесь является то, какой цвет в CSL мы укажем для дефолтового состояния. Это состояние, когда
     * элемент НЕ В ФОКУСЕ. Мы хотим, чтобы при потере фокуса рамка была цвета purple, если
     * пользователь ввёл какой-то текст. Или была бы серой, если текста нет. Соответственно у нас два
     * CSL, в одном дефолтовый цвет - purple, в другом - серый. И при потере фокуса мы их меняем. При
     * получении фокуса можно вообще ничего не делать потому что в обоих CSL для фокусного состояния
     * цвет синий.
     *
     * Теперь по поводу цвета подсказки. Он также регулируется с помошью CSL.
     */
    private val onFocusListener = View.OnFocusChangeListener { v, hasFocus ->
        val view = v as EditText

        if (hasFocus) {
            // Цвет рамки (в фокусе всегда синий)
            inputStyled.setBoxStrokeColorStateList(cslWithText)
        } else {
            val csl = if (view.text.toString().isNotEmpty()) cslWithText else cslNoText

            inputStyled.setBoxStrokeColorStateList(csl)
            inputStyled.defaultHintTextColor = csl
        }
    }
}