package s.yarlykov.izisandbox.transitions.shared_with_fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import s.yarlykov.izisandbox.R
import s.yarlykov.izisandbox.application.App
import s.yarlykov.izisandbox.transitions.shared_with_fragments.di.ModuleTransitionActivity
import s.yarlykov.izisandbox.utils.logIt
import javax.inject.Inject

class ActivityWithFragments : AppCompatActivity() {

    lateinit var buttonExample1: Button
    lateinit var buttonExample2: Button
    lateinit var flexBox: FlexboxLayout

    @Inject
    lateinit var titleWrapper: TitleWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_fragments)

        // Тест. Создать компонент у которого модуль с аргументом.
        (application as App)
            .appComponent
            .builderComponentTransitionActivity
            .plus(ModuleTransitionActivity(R.string.title_select_example))
            .build()
            .inject(this)

        findView()
        initViews()
        initFragment(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        logIt("${this::class.simpleName}::${object {}.javaClass.enclosingMethod?.name} '${titleWrapper.title}'")
    }

    private fun findView() {
        flexBox = findViewById(R.id.flex_box)
        buttonExample1 = findViewById(R.id.example_1)
        buttonExample2 = findViewById(R.id.example_2)

        setFlexboxChildLayoutParams(buttonExample1)
        setFlexboxChildLayoutParams(buttonExample2)
    }

    private fun initViews() {
        buttonExample1.setOnClickListener {
            navigateTo(FragmentFrom::class.java, R.layout.fragment_shared_from_1)
        }

        buttonExample2.setOnClickListener {
            navigateTo(FragmentFrom::class.java, R.layout.fragment_shared_from_2)
        }
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) return

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_placeholder,
                FragmentLogo(), FragmentLogo::class.java.simpleName
            )
            .commit()
    }

    private fun navigateTo(clazz: Class<*>, layoutId: Int) {

        val fragment = clazz.getConstructor().newInstance() as Fragment

        fragment.arguments = Bundle().apply {
            putInt(FragmentFrom.KEY_LAYOUT_ID_FROM, layoutId)
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.fragment_placeholder,
                fragment, clazz.simpleName
            )
            .commit()
    }

    /**
     * Настройка LayoutParams для кнопок внутри макета Flexbox
     *
     * NOTE: Ширина устанавливается как процент от ширины родителя в диапазоне 0.0-1.0
     */
    private fun setFlexboxChildLayoutParams(
        child: View,
        childVisibility: Int = View.VISIBLE,
        widthPercent: Float = 0.4F
    ) {

        child.apply {
            visibility = childVisibility
            layoutParams = (layoutParams as FlexboxLayout.LayoutParams).also {
                it.flexBasisPercent = widthPercent
            }
        }
    }
}