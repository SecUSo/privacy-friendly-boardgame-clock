/*
 This file is part of Privacy Friendly App Example.

 Privacy Friendly App Example is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.MainMenuChoosePlayersFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.MainMenuNewGameFragment;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.tutorial.PrefManager;
import org.secuso.privacyfriendlyboardgameclock.tutorial.TutorialActivity;

/**
 * @author Christopher Beckmann, Karola Marky
 * @version 20171016
 */

public class MainActivity extends BaseActivity {
    private FragmentManager fm;
    private Game game;
    private PlayersDataSourceSingleton pds;
    private GamesDataSourceSingleton gds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create Database, only once
        pds = PlayersDataSourceSingleton.getInstance(this.getApplicationContext());
        pds.open();
        gds = GamesDataSourceSingleton.getInstance(this.getApplicationContext());
        gds.open();
        fm = getFragmentManager();

        // New Game Button
        final Button newGameButton = findViewById(R.id.newGameButton);
        newGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuNewGameFragment());
                fragmentTransaction.addToBackStack(getString(R.string.newGameFragment));
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();

            }
        });

        // Continue Game Button
        final Button continueGameButton = findViewById(R.id.resumeGameButton);
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

        // Use this a button to display the tutorial screen
        final Button tutorialButton = findViewById(R.id.button_welcomedialog);
        if(tutorialButton != null) {
            tutorialButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PrefManager prefManager = new PrefManager(getBaseContext());
                    prefManager.setFirstTimeLaunch(true);
                    Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        }

        overridePendingTransition(0, 0);
        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
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
        if (!drawer.isDrawerOpen(GravityCompat.START) && fm.getBackStackEntryCount() > 0) {
            Fragment currentFragment = fm.findFragmentById(R.id.MainActivity_fragment_container);
            if (currentFragment instanceof MainMenuChoosePlayersFragment) {
                // Pop all the DialogFragment in BackStack
                int backstackCount =  fm.getBackStackEntryCount();
                int i =  backstackCount -1;
                String backstackEntryName = fm.getBackStackEntryAt(i).getName();

                // if no dialog fragment open, call super onBackPressed
                if(backstackEntryName == getString(R.string.choosePlayersFragment)){
                    super.onBackPressed();
                    return;
                }
                while(i >= 0 && backstackEntryName == null){
                    if(backstackEntryName != null){
                        if(backstackEntryName.equals(getString(R.string.choosePlayersFragment))) break;
                    }
                    fm.popBackStack();
                    i--;
                    backstackEntryName = fm.getBackStackEntryAt(i).getName();
                }
                // restart ChoosePlayerFragment
                FragmentTransaction fragTransaction = fm.beginTransaction();
                fragTransaction.detach(currentFragment);
                fragTransaction.attach(currentFragment);
                fragTransaction.commit();
            }
            else{
                super.onBackPressed();
            }
        }
        else if(fm.getBackStackEntryCount() > 1){
            fm.popBackStack();
    }
        else super.onBackPressed();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(fm.getBackStackEntryCount() > 1)
                    fm.popBackStack();
                else
                    finish();
                Log.i("MainActivity", "Home Action Bar selected");
                return true;
            default:
                Log.i("MainActivity", "Default option selected?");
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * when ever user chooses to navigate up within app activity hierachy from the action bar
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        // TODO if Fragmentmanager > 1 then...
        onBackPressed();
        return true;
    }


    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    @Override
    protected void onDestroy() {
        try{
            gds.close();
            pds.close();
        }catch (Exception e){

        }
        super.onDestroy();
    }
}
