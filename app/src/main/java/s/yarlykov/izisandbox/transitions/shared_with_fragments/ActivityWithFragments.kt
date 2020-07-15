package s.yarlykov.izisandbox.transitions.shared_with_fragments

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import s.yarlykov.izisandbox.R

class ActivityWithFragments : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_fragments)

        initFragment(savedInstanceState)
    }

    private fun initFragment(savedInstanceState: Bundle?) {
        if(savedInstanceState != null) return

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_placeholder, FragmentFrom(), FragmentFrom::class.java.simpleName)
            .commit()
    }
}