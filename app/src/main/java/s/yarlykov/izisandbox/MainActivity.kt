package s.yarlykov.izisandbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import s.yarlykov.izisandbox.dialogs.DialogsActivity
import s.yarlykov.izisandbox.izilogin.IziLoginActivity
import s.yarlykov.izisandbox.navgraph.ActivityGraph1
import s.yarlykov.izisandbox.recycler.swipe_1.SwipeActivityFirst
import s.yarlykov.izisandbox.recycler.swipe_2.SwipeActivitySecond
import s.yarlykov.izisandbox.recycler.swipe_with_undo.SwipeWithUndoActivity
import s.yarlykov.izisandbox.theme.ThemeActivity
import s.yarlykov.izisandbox.transitions.shared_with_activities.ActivitySharedFrom
import s.yarlykov.izisandbox.transitions.shared_with_fragments.ActivityWithFragments
import s.yarlykov.izisandbox.transitions.using_scenes.UsingScenesActivity
import s.yarlykov.izisandbox.transitions.using_window.ActivityFrom

class MainActivity : AppCompatActivity() {

    lateinit var scrollView: NestedScrollView
    lateinit var cardsContainer: LinearLayout

    private val stages = mapOf(
        IziLoginActivity::class.java to R.string.menu_izi_login,
        ActivityGraph1::class.java to R.string.menu_nav_graph,
        ActivityFrom::class.java to R.string.menu_activity_transitions_1,
        ActivitySharedFrom::class.java to R.string.menu_activities_shared_views,
        ActivityWithFragments::class.java to R.string.menu_fragments_shared_views,
        UsingScenesActivity::class.java to R.string.menu_with_scenes,
        DialogsActivity::class.java to R.string.menu_dialogs,
        ThemeActivity::class.java to R.string.menu_themes,
        SwipeActivityFirst::class.java to R.string.menu_swipe_with_helper,
        SwipeActivitySecond::class.java to R.string.menu_swipe_gesture_detector,
        SwipeWithUndoActivity::class.java to R.string.menu_swipe_drop_undo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Скрываем Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        findView()
        inflateMenu(cardsContainer, stages)
    }

    private fun findView() {
        scrollView = findViewById(R.id.scroll_view)
        cardsContainer = findViewById(R.id.cards_container)
    }

    /**
     * Инфлайтим пункты меню. Каждый пункт создает отдельную активити.
     */
    private fun inflateMenu(root: LinearLayout, model: Map<out Class<*>, Int>) {

        val inflater = layoutInflater

        model.entries.forEach { entry ->
            val (clazz, stringId) = entry

            val cardView = inflater.inflate(R.layout.layout_card_main_menu, root, false)
            val titleView = cardView.findViewById<TextView>(R.id.tv_title)
            titleView.text = getString(stringId)

            cardView.setOnClickListener {
                startActivity(Intent(this, clazz))
            }

            root.addView(cardView)
        }
    }
}