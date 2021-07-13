package s.yarlykov.izisandbox

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.transition.Fade
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import s.yarlykov.izisandbox.dialogs.DialogsActivity
import s.yarlykov.izisandbox.extensions.ZDate
import s.yarlykov.izisandbox.extensions.toReadable
import s.yarlykov.izisandbox.izilogin.IziLoginActivity
import s.yarlykov.izisandbox.matrix.avatar_maker_dev.EditorAvatarActivity
import s.yarlykov.izisandbox.matrix.avatar_maker_prod.FunnyAvatarActivity
import s.yarlykov.izisandbox.matrix.scale_animated.ScaleAnimatedActivity
import s.yarlykov.izisandbox.matrix.surface.v02.SmoothDraggingActivity
import s.yarlykov.izisandbox.matrix.v1.MatrixActivityV1
import s.yarlykov.izisandbox.matrix.v2.MatrixActivityV2
import s.yarlykov.izisandbox.navgraph.ActivityGraph1
import s.yarlykov.izisandbox.notifier.NotificationSenderActivity
import s.yarlykov.izisandbox.places.PlacesAutoCompleteActivity
import s.yarlykov.izisandbox.recycler_and_swipes.decorator.app.DecoratedListActivity
import s.yarlykov.izisandbox.recycler_and_swipes.grid.GridListActivity
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_01.InfiniteRecyclerActivity
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_02.InfiniteDatePickerActivity
import s.yarlykov.izisandbox.recycler_and_swipes.infinite_loop.infinite_calendar.InfiniteCalendarActivity
import s.yarlykov.izisandbox.recycler_and_swipes.items_animation.item_animators.RecyclerViewItemAnimationActivity
import s.yarlykov.izisandbox.recycler_and_swipes.items_animation.layout_animation.RecyclerViewLayoutAnimationActivity
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_1.SwipeActivityFirst
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_2.SwipeActivitySecond
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_3.SmartSwipeActivity
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_4.SwipeAnyWhereActivity
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_5.AccordionSwipeActivity
import s.yarlykov.izisandbox.recycler_and_swipes.swipe_with_undo.SwipeWithUndoActivity
import s.yarlykov.izisandbox.recycler_and_swipes.time_line_2d.TimeLineAdvancedActivity
import s.yarlykov.izisandbox.ui.shimmer.ShimmerActivity
import s.yarlykov.izisandbox.telegram.v1.TelegramActivityV1
import s.yarlykov.izisandbox.telegram.v2.TelegramActivityV2
import s.yarlykov.izisandbox.theme.ThemeActivity
import s.yarlykov.izisandbox.time_line.TimeLineActivityEdu
import s.yarlykov.izisandbox.time_line.TimeLineActivityPro
import s.yarlykov.izisandbox.transitions.shared_with_activities.ActivitySharedFrom
import s.yarlykov.izisandbox.transitions.shared_with_fragments.ActivityWithFragments
import s.yarlykov.izisandbox.transitions.using_scenes.ScenesInsideActivity1
import s.yarlykov.izisandbox.transitions.using_scenes.ScenesInsideActivity2
import s.yarlykov.izisandbox.transitions.using_scenes.ScenesInsideActivity3
import s.yarlykov.izisandbox.transitions.using_window.ActivityFrom
import s.yarlykov.izisandbox.ui.ButtonsListActivity
import s.yarlykov.izisandbox.ui.CustomBottomAppBarActivity

class MainActivity : AppCompatActivity() {

    lateinit var scrollView: NestedScrollView
    lateinit var cardsContainer: LinearLayout

    private val stages = mapOf(
//        RecyclerViewEventsActivity::class.java to R.string.menu_rv_events,
        ShimmerActivity::class.java to R.string.menu_shimmer_effect,
        TimeLineAdvancedActivity::class.java to R.string.menu_time_line_2d,
        DecoratedListActivity::class.java to R.string.menu_sticky_decor,
        FunnyAvatarActivity::class.java to R.string.menu_funny_avatar,
        EditorAvatarActivity::class.java to R.string.menu_create_avatar,
        ScaleAnimatedActivity::class.java to R.string.menu_scale_big_bitmap,
        TelegramActivityV1::class.java to R.string.menu_telegram_profile_v1,
        TelegramActivityV2::class.java to R.string.menu_telegram_profile_v2,
        TimeLineActivityEdu::class.java to R.string.menu_time_line_edu,
        TimeLineActivityPro::class.java to R.string.menu_time_line_pro,
        SwipeActivityFirst::class.java to R.string.menu_swipe_with_helper,
        SwipeActivitySecond::class.java to R.string.menu_swipe_gesture_detector,
        SmartSwipeActivity::class.java to R.string.menu_swipe_smart,
        SwipeAnyWhereActivity::class.java to R.string.menu_swipe_any_where,
        SwipeWithUndoActivity::class.java to R.string.menu_swipe_drop_undo,
        AccordionSwipeActivity::class.java to R.string.menu_swipe_accordion,
        RecyclerViewLayoutAnimationActivity::class.java to R.string.menu_rv_layout_animation,
        RecyclerViewItemAnimationActivity::class.java to R.string.menu_rv_item_animation,
        InfiniteRecyclerActivity::class.java to R.string.menu_rv_infinite_loop,
        InfiniteDatePickerActivity::class.java to R.string.menu_rv_infinite_date_picker,
        InfiniteCalendarActivity::class.java to R.string.menu_rv_scrolling_calendar,
//        DslActivity::class.java to R.string.menu_dsl_edu,
        CustomBottomAppBarActivity::class.java to R.string.menu_bottom_bar,
        ScenesInsideActivity1::class.java to R.string.menu_with_scenes_1,
        ScenesInsideActivity2::class.java to R.string.menu_with_scenes_2,
        ScenesInsideActivity3::class.java to R.string.menu_with_scenes_3,
        ActivityGraph1::class.java to R.string.menu_nav_graph,
        ActivityFrom::class.java to R.string.menu_activity_transitions_1,
        ActivitySharedFrom::class.java to R.string.menu_activities_shared_views,
        ActivityWithFragments::class.java to R.string.menu_fragments_shared_views,
        DialogsActivity::class.java to R.string.menu_dialogs,
        MatrixActivityV1::class.java to R.string.menu_matrix_reflect_image,
        MatrixActivityV2::class.java to R.string.menu_matrix_transform_image,
        ThemeActivity::class.java to R.string.menu_themes,
        IziLoginActivity::class.java to R.string.menu_izi_login,
        PlacesAutoCompleteActivity::class.java to R.string.menu_places_auto_complete,
        SmoothDraggingActivity::class.java to R.string.menu_surface_edu_activity,
        GridListActivity::class.java to R.string.menu_grid_list,
        ButtonsListActivity::class.java to R.string.menu_buttons_containers,
        NotificationSenderActivity::class.java to R.string.menu_send_notification
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowTransitions()
        setContentView(R.layout.activity_main)

        // Скрываем Status Bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        findView()
        inflateMenu(cardsContainer, stages)
        showDate()
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

                /**
                 * Некторые активити запускаем с enterAnimation. Для этого используем
                 * activity.startNew()
                 */
                when (clazz) {
                    ActivityFrom::class.java -> {
                        ActivityFrom.startNew(this)
                    }
                    ScenesInsideActivity1::class.java -> {
                        ScenesInsideActivity1.startNew(this)
                    }
                    else -> {
                        startActivity(Intent(this, clazz))
                    }
                }
            }
            root.addView(cardView)
        }
    }


    private fun setWindowTransitions() {

        val animDuration = resources.getInteger(R.integer.animation_activity_in_out).toLong()

        /**
         * Нужно делать до вызова setContentView(layout_id)
         */
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            allowReturnTransitionOverlap = true

            enterTransition = Fade(Fade.IN).apply {
                duration = animDuration
            }

            exitTransition = Explode().apply {
                duration = animDuration

                /**
                 * Анимируем только контент нашей активити. Нам не нужно
                 * анимировать system bars и action bar.
                 */
//                addTarget(R.id.main_container)
            }

            reenterTransition = Fade(Fade.IN).apply {
                duration = animDuration
                addTarget(R.id.main_container)
            }
        }
    }

    private fun showDate() {

        findViewById<TextView>(R.id.tv_date).apply {
            text = ZDate.now().toReadable(this@MainActivity)
        }
    }
}