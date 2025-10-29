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

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Christopher Beckmann, Karola Marky, Quang Anh Dang
 * @version 20171016
 * Last changed on 18.03.18
 * This is the Activity for the Main Page
 */
class MainActivity : BaseActivity() {
    private val pds: PlayersDataSourceSingleton by lazy { PlayersDataSourceSingleton.getInstance(this.applicationContext) }
    private val gds: GamesDataSourceSingleton by lazy { GamesDataSourceSingleton.getInstance(this.applicationContext) }
    private val continueGameButton by lazy { findViewById<Button>(R.id.resumeGameButton) }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_home

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.newGameButton).setOnClickListener {
            startActivity(Intent(this@MainActivity, NewGameActivity::class.java))
        }

        if (intent.extras?.getBoolean("EXIT", false) == true) {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        continueGameButton.apply {
            if (gds.savedGames.isEmpty()) {
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_disabled)
                setOnClickListener { showToast(getString(R.string.resumeGameErrorToast)) }
            } else {
                background = ContextCompat.getDrawable(this@MainActivity, R.drawable.button_fullwidth)
                setOnClickListener {
                    val intent = Intent(this@MainActivity, ResumeGameActivity::class.java)
                    startActivity(intent)
                }
            }
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
}
