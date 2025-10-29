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


package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import android.view.View;
import android.widget.Button;

import org.secuso.privacyfriendlyboardgameclock.GoodbyeGoogleHelperKt;
import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Christopher Beckmann, Karola Marky, Quang Anh Dang
 * @version 20171016
 * Last changed on 18.03.18
 * This is the Activity for the Main Page
 */

public class MainActivity extends BaseActivity {
    private FragmentManager fm;
    private Game game;
    private PlayersDataSourceSingleton pds;
    private GamesDataSourceSingleton gds;
    private Button continueGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pds = PlayersDataSourceSingleton.getInstance(this.getApplicationContext());
        gds = GamesDataSourceSingleton.getInstance(this.getApplicationContext());
        fm = getFragmentManager();

        // New Game Button
        final Button newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewGameActivity.class);
                startActivity(intent);
            }
        });

        // Continue Game Button
        continueGameButton = findViewById(R.id.resumeGameButton);

        overridePendingTransition(0, 0);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

//        GoodbyeGoogleHelperKt.checkGoodbyeGoogle(this, getLayoutInflater());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gds.getSavedGames().size() == 0) {
            continueGameButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_disabled));
            continueGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showToast(getString(R.string.resumeGameErrorToast));
                }
            });
        } else {
            continueGameButton.setBackground(ContextCompat.getDrawable(this, R.drawable.button_fullwidth));
            continueGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ResumeGameActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    /**
     * This method connects the Activity to the menu item
     * @return ID of the menu item it belongs to
     */
    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_example;
    }

    public void onClick(View view) {
        switch(view.getId()) {
            // do something with all these buttons?
            default:
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            try{
                gds.close();
                pds.close();
            }catch (Exception e){

            }
        }
        super.onBackPressed();
    }

    public void setDrawerEnabled(final boolean enabled) {

        int lockMode = enabled ?
                DrawerLayout.LOCK_MODE_UNLOCKED :
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED;

        mDrawerLayout.setDrawerLockMode(lockMode);

        toggle.setDrawerIndicatorEnabled(enabled);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(!enabled);
            actionBar.setDisplayShowHomeEnabled(enabled);
            actionBar.setHomeButtonEnabled(enabled);
        }

        toggle.syncState();
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
