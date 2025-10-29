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

import android.app.FragmentManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.drawerlayout.widget.DrawerLayout
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.model.Game

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Christopher Beckmann, Karola Marky, Quang Anh Dang
 * @version 20171016
 * Last changed on 18.03.18
 * This is the Activity for the Main Page
 */
class MainActivity : BaseActivity() {
    private var fm: FragmentManager? = null
    private var game: Game? = null
    private var pds: PlayersDataSourceSingleton? = null
    private var gds: GamesDataSourceSingleton? = null
    private var continueGameButton: Button? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pds = PlayersDataSourceSingleton.getInstance(this.getApplicationContext())
        gds = GamesDataSourceSingleton.getInstance(this.getApplicationContext())
        fm = getFragmentManager()

        // New Game Button
        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                val intent = Intent(this@MainActivity, NewGameActivity::class.java)
                startActivity(intent)
            }
        })

        // Continue Game Button
        continueGameButton = findViewById<Button>(R.id.resumeGameButton)

        overridePendingTransition(0, 0)
        if (getIntent().getExtras() != null && getIntent().getExtras()!!
                .getBoolean("EXIT", false)
        ) {
            finish()
        }

        //        GoodbyeGoogleHelperKt.checkGoodbyeGoogle(this, getLayoutInflater());
    }

    override fun onResume() {
        super.onResume()
        if (gds.getSavedGames().size == 0) {
            continueGameButton!!.setBackground(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.button_disabled
                )
            )
            continueGameButton!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    showToast(getString(R.string.resumeGameErrorToast))
                }
            })
        } else {
            continueGameButton!!.setBackground(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.button_fullwidth
                )
            )
            continueGameButton!!.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    val intent = Intent(this@MainActivity, ResumeGameActivity::class.java)
                    startActivity(intent)
                }
            })
        }
    }

    val navigationDrawerID: Int
        /**
         * This method connects the Activity to the menu item
         * @return ID of the menu item it belongs to
         */
        get() = R.id.nav_example

    fun onClick(view: View) {
        when (view.getId()) {
            else -> {}
        }
    }

    public override fun onBackPressed() {
        val drawer = findViewById<View?>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            try {
                gds.close()
                pds.close()
            } catch (e: Exception) {
            }
        }
        super.onBackPressed()
    }

    fun setDrawerEnabled(enabled: Boolean) {
        val lockMode =
            if (enabled) DrawerLayout.LOCK_MODE_UNLOCKED else DrawerLayout.LOCK_MODE_LOCKED_CLOSED

        mDrawerLayout.setDrawerLockMode(lockMode)

        toggle.setDrawerIndicatorEnabled(enabled)

        val actionBar = getSupportActionBar()
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!enabled)
            actionBar.setDisplayShowHomeEnabled(enabled)
            actionBar.setHomeButtonEnabled(enabled)
        }

        toggle.syncState()
    }

    fun getGame(): Game? {
        return game
    }

    fun setGame(game: Game?) {
        this.game = game
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
