/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.app.TaskStackBuilder
import androidx.drawerlayout.widget.DrawerLayout
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.tutorial.TutorialActivity

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 * Last changed on 18.03.18
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.java)
 */
abstract class BaseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Navigation drawer:
    @JvmField
    var mDrawerLayout: DrawerLayout? = null
    private var mNavigationView: NavigationView? = null
    @JvmField
    var toggle: ActionBarDrawerToggle? = null

    // Helper
    private var mHandler: Handler? = null
    protected var mSharedPreferences: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for every activity, create/open database if necessary
        PlayersDataSourceSingleton.getInstance(this.getApplicationContext()).open()
        GamesDataSourceSingleton.getInstance(this.getApplicationContext()).open()

        // start service to close database when app is killed
        startService(Intent(getBaseContext(), DetectTaskClearedService::class.java))

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mHandler = Handler()

        overridePendingTransition(0, 0)
    }

    override fun onBackPressed() {
        val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    protected abstract val navigationDrawerID: Int

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.getItemId()

        return goToNavigationItem(itemId)
    }

    protected fun goToNavigationItem(itemId: Int): Boolean {
        if (itemId == this.navigationDrawerID) {
            // just close drawer because we are already in this activity
            mDrawerLayout.closeDrawer(GravityCompat.START)
            return true
        }

        // delay transition so the drawer can close
        mHandler!!.postDelayed(object : Runnable {
            override fun run() {
                callDrawerItem(itemId)
            }
        }, NAVDRAWER_LAUNCH_DELAY.toLong())

        mDrawerLayout.closeDrawer(GravityCompat.START)

        selectNavigationItem(itemId)

        // fade out the active activity
        val mainContent = findViewById<View?>(R.id.main_content)
        if (mainContent != null) {
            mainContent.animate().alpha(0f).setDuration(MAIN_CONTENT_FADEOUT_DURATION.toLong())
        }
        return true
    }

    // set active navigation item
    private fun selectNavigationItem(itemId: Int) {
        for (i in 0..<mNavigationView.getMenu().size()) {
            val b = itemId == mNavigationView.getMenu().getItem(i).getItemId()
            mNavigationView.getMenu().getItem(i).setChecked(b)
        }
    }

    /**
     * Enables back navigation for activities that are launched from the NavBar. See
     * `AndroidManifest.xml` to find out the parent activity names for each activity.
     * so in case back button pressed --> go directly to parent
     * @param intent
     */
    private fun createBackStack(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            val builder = TaskStackBuilder.create(this)
            builder.addNextIntentWithParentStack(intent)
            builder.startActivities()
        } else {
            startActivity(intent)
            finish()
        }
    }

    /**
     * This method manages the behaviour of the navigation drawer
     * Add your menu items (ids) to res/menu/activity_main_drawer.xml
     * @param itemId Item that has been clicked by the user
     */
    private fun callDrawerItem(itemId: Int) {
        val intent: Intent?

        if (itemId == R.id.nav_example) {
            intent = Intent(this, MainActivity::class.java)
            intent!!.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else if (itemId == R.id.nav_tutorial) {
            intent = Intent(this, TutorialActivity::class.java)
            intent!!.setAction(TutorialActivity.ACTION_SHOW_ANYWAYS)
            createBackStack(intent)
        } else if (itemId == R.id.nav_about) {
            intent = Intent(this, AboutActivity::class.java)
            createBackStack(intent!!)
        } else if (itemId == R.id.nav_help) {
            intent = Intent(this, HelpActivity::class.java)
            createBackStack(intent!!)
        } else if (itemId == R.id.nav_player_management) {
            intent = Intent(this, PlayerManagementActivity::class.java)
            createBackStack(intent!!)
        } else if (itemId == R.id.nav_game_history) {
            intent = Intent(this, GameHistoryActivity::class.java)
            createBackStack(intent!!)
        } else if (itemId == R.id.nav_backup) {
            intent = Intent(this, BackUpActivity::class.java)
            createBackStack(intent!!)
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        val toolbar = findViewById<View?>(R.id.toolbar) as Toolbar?
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar)
        }

        mDrawerLayout = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        toggle = ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mDrawerLayout!!.addDrawerListener(toggle!!)
        toggle!!.syncState()

        mNavigationView = findViewById<View?>(R.id.nav_view) as NavigationView
        mNavigationView.setNavigationItemSelectedListener(this)

        selectNavigationItem(this.navigationDrawerID)

        val mainContent = findViewById<View?>(R.id.main_content)
        if (mainContent != null) {
            mainContent.setAlpha(0f)
            mainContent.animate().alpha(1f).setDuration(MAIN_CONTENT_FADEIN_DURATION.toLong())
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        toggle!!.onConfigurationChanged(newConfig)
    }


    /**
     * show a toast with certain text as message
     * @param text
     */
    fun showToast(text: String?) {
        val toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
        toast.show()
    }

    /**
     * before starting to work, check if Database and Singleton Object (use to save some objects
     * and transferring objects between activity), if any attribute is null --> move to main activity
     * and remove all other activities --> start new
     */
    fun checkIfSingletonDataIsCorrupt(): Boolean {
        if (!(GamesDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull()
                    && PlayersDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull())
        ) {
            val intent = Intent(this, MainActivity::class.java)
            // clear all other activities
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        } else return false
    }

    /**
     * make sure to call this method after onPostCreate of Activity finishes
     * @param enabled
     */
    open fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED

        mDrawerLayout!!.setDrawerLockMode(lockMode)

        toggle!!.setDrawerIndicatorEnabled(enabled)

        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!enabled)
            actionBar.setDisplayShowHomeEnabled(enabled)
            actionBar.setHomeButtonEnabled(enabled)
        }
        if (!enabled) {
            toggle!!.setToolbarNavigationClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    onBackPressed()
                }
            })
        }
        toggle!!.syncState()
    }

    fun showMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    /**
     *
     * @param serviceClass name of the service class you want to check
     * @return true if the service is running
     */
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.Companion.MAX_VALUE)) {
            if (serviceClass.getName() == service.service.getClassName()) {
                return true
            }
        }
        return false
    }

    companion object {
        // delay to launch nav drawer item, to allow close animation to play
        const val NAVDRAWER_LAUNCH_DELAY: Int = 250

        // fade in and fade out durations for the main content when switching between
        // different Activities of the app through the Nav Drawer
        const val MAIN_CONTENT_FADEOUT_DURATION: Int = 150
        const val MAIN_CONTENT_FADEIN_DURATION: Int = 250
    }
}
