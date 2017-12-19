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

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.MainMenuWelcomeFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Create Database, only once
        PlayersDataSourceSingleton pds = PlayersDataSourceSingleton.getInstance(this.getApplicationContext());
        pds.open();
        GamesDataSourceSingleton gds = GamesDataSourceSingleton.getInstance(this.getApplicationContext());
        gds.open();

        fm = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuWelcomeFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuWelcomeFragment));
        fragmentTransaction.commit();

        overridePendingTransition(0, 0);
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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
