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
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import org.secuso.pfacore.model.DrawerMenu
import org.secuso.pfacore.ui.activities.DrawerActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.services.DetectTaskClearedService

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171017
 * Last changed on 18.03.18
 * This class is a parent class of all activities that can be accessed from the
 * Navigation Drawer (example see MainActivity.java)
 */
abstract class BaseActivity : DrawerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for every activity, create/open database if necessary
        PlayersDataSourceSingleton.getInstance(this.applicationContext).open()
        GamesDataSourceSingleton.getInstance(this.applicationContext).open()

        // start service to close database when app is killed
        startService(Intent(baseContext, DetectTaskClearedService::class.java))

    }

    override fun drawer() = DrawerMenu.build {
        name = ContextCompat.getString(this@BaseActivity, R.string.app_name)
        icon = R.mipmap.icon
        section {
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_main)
                icon = R.drawable.ic_menu_home
                clazz = MainActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_player_management)
                icon = R.drawable.ic_menu_player_management
                clazz = PlayerManagementActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_game_history)
                icon = R.drawable.ic_menu_game_history
                clazz = GameHistoryActivity::class.java
            }
            activity {
                name = ContextCompat.getString(this@BaseActivity, R.string.action_backup)
                icon = R.drawable.ic_menu_backup
                clazz = BackUpActivity::class.java
            }
        }
        defaultDrawerSection(this)
    }

    /**
     * show a toast with certain text as message
     * @param text
     */
    fun showToast(text: String?) {
        val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
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
            if (serviceClass.getName() == service.service.className) {
                return true
            }
        }
        return false
    }
}
