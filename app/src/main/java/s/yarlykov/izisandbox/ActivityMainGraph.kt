package s.yarlykov.izisandbox

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.navigation.NavigationView

class ActivityMainGraph : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var navController: NavController
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findView()
        setSupportActionBar(toolbar)
        initViews()

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
    }

    private fun findView() {
        toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
    }

    private fun initViews() {
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
                    navController.popBackStack(R.id.nav_home, true)
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
            R.id.nav_graph_2 -> {

                val intent = Intent(this, ActivityGraph2::class.java)
                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)

        return true

    }
}