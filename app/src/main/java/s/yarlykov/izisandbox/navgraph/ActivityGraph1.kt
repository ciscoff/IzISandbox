package s.yarlykov.izisandbox.navgraph

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView
import s.yarlykov.izisandbox.R

class ActivityGraph1 : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph1)
//        setupWindowAnimations()

        findView()
        setSupportActionBar(toolbar)
        initViews()
    }

    override fun finish() {
        super.finish()
        ActivityNavigator.applyPopAnimationsToPendingTransition(this)
    }

    private fun findView() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
    }

    /**
     * Passing each menu ID as a set of Ids because each
     * menu should be considered as top level destinations.
     */
    private fun initViews() {
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setNavigationItemSelectedListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

//    override fun onBackPressed() {
//        when (navController.currentDestination?.id) {
//            R.id.nav_home -> {
//
//            }
//            R.id.nav_gallery -> {
//
//            }
//            R.id.nav_slideshow -> {
//
//            }
//            R.id.nav_stub -> {
//
//            }
//            else -> {
//                finish()
//            }
//        }
//    }

    /**
     * Обработка нажатий в боковом меню.
     */
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val currentDestination = navController.currentDestination

        when (item.itemId) {
            R.id.nav_home -> {
                if (currentDestination?.id != R.id.nav_home) {
                    /**
                     * Возврат в начало графа выполнять с помощью destination,
                     * которое является самим графом. Его корнем, а не startDestination.
                     */
                    navController.popBackStack(R.id.mobile_navigation, false)
                }
            }
            R.id.action_to_nav_gallery -> {
                if (currentDestination?.id != R.id.nav_gallery) {
                    navController.navigate(R.id.action_to_nav_gallery)
                }
            }
            R.id.action_to_nav_slideshow -> {
                if (currentDestination?.id != R.id.nav_slideshow) {
                    navController.navigate(R.id.action_to_nav_slideshow)
                }
            }
            R.id.action_to_nav_stub -> {
                if (currentDestination?.id != R.id.nav_stub) {
                    navController.navigate(R.id.action_to_nav_stub)
                }
            }
            R.id.action_to_graph_2 -> {
                navController.navigate(R.id.action_to_graph_2)
//                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }

    private fun setupWindowAnimations() {
        val slide = TransitionInflater.from(this).inflateTransition(R.transition.activity_slide)
        window.exitTransition = slide
    }
}