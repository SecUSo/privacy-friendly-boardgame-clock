package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.db.GamesDataSource;
import org.secuso.privacyfriendlyboardgameclock.db.PlayersDataSource;
import org.secuso.privacyfriendlyboardgameclock.fragments.AboutFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.BackupDialog;
import org.secuso.privacyfriendlyboardgameclock.fragments.GameHistoryFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.HelpFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.MainMenuFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.NewGameFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.WelcomeDialog;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.view.ObjectDrawerItem;

public class MainActivity extends AppCompatActivity {

    boolean drawerOpened = false;
    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private String activityTitle;

    private Game game;
    private Game historyGame;

    private GamesDataSource gds;
    private PlayersDataSource pds;

    private Player playerForEditing;
    private SharedPreferences settings;
    private FragmentManager fm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getFragmentManager();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstStart = settings.getBoolean("showWelcomeDialog", true);
        if (firstStart) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(fm, getString(R.string.welcomeDialog));

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("showWelcomeDialog", false);
            editor.commit();
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#024265")));

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean("EXIT", false)) {
            finish();
        }

        drawerList = (ListView) findViewById(R.id.navList);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        activityTitle = getTitle().toString();
        game = new Game();


        addDrawerItems();
        setupDrawer();

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[6];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_menu_grey_600_48dp, getString(R.string.action_main), "");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_supervisor_account_grey_600_48dp, getString(R.string.playerManagement), "");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_playlist_add_check_grey_600_48dp, getString(R.string.gameHistory), "");
        drawerItem[3] = new ObjectDrawerItem(R.drawable.ic_settings_backup_restore_grey_600_48dp, getString(R.string.backup), "");
        drawerItem[4] = new ObjectDrawerItem(R.drawable.ic_action_help, getString(R.string.action_help), "");
        drawerItem[5] = new ObjectDrawerItem(R.drawable.ic_action_about, getString(R.string.action_about), "");

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.navdrawer_item_row, drawerItem);
        drawerList.setAdapter(adapter);

        final FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainMenuFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuFragment));
        fragmentTransaction.commit();

        pds = new PlayersDataSource(this);
        pds.open();

        gds = new GamesDataSource(this);
        gds.open();
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void newGameButton(View v) {
        final FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new NewGameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.newGameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    public void resumeGameButton(View v) {

    }

    public void newGamePlayerManagementButton(View v) {

    }

    public void addContactSelectionButton(View v) {
    }

    public void importBackupButton(View v) {

    }

    public void exportBackupButton(View v) {

    }

    public void choosePlayersButton(View v) {
    }

    public void startNewGameButton(View v) {
    }

    public void createNewPlayerButton(View v) {
    }

    public void addPlayerContactsButton(View v) {
    }

    public void gamePlayPauseButton(View v) {
    }

    public void nextPlayerButton(View v) {
    }

    public void saveGameButton(View v) {

    }

    public void loadGameButton(View v) {

    }

    public void finishGameButton(View v) {

    }

    public void confirmNewPlayerButton(View v) {

    }

    public void confirmEditPlayerButton(View v) {

    }

    public void deleteGameButton(View v) {

    }

    public void removeEntryButton(View v) {

    }

    public void showResultsButton(View v) {

    }

    public void setPlayerSequenceButton(View v) {

    }

    public void showPlayerStatistics(View v) {

    }

    @Override
    public void onBackPressed() {

        if (drawerOpened) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void addDrawerItems() {
        String[] mNavigationDrawerItemTitles = {getString(R.string.action_main), getString(R.string.playerManagement), getString(R.string.gameHistory), getString(R.string.backup), getString(R.string.action_help), getString(R.string.action_about)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.navdrawer_item_row, mNavigationDrawerItemTitles);
        drawerList.setAdapter(adapter);

        drawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private void setupDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerOpened = true;
                getSupportActionBar().setTitle(R.string.action_navigation);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                drawerOpened = false;

                getSupportActionBar().setTitle(activityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //Remove comment in case menu on the right is needed
        // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public Player getPlayerForEditing() {
        return playerForEditing;
    }

    public void setPlayerForEditing(Player p) {
        this.playerForEditing = p;
    }

    private void selectItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new MainMenuFragment();
                break;
            case 1:
                fragment = new PlayerManagementFragment();
                break;
            case 2:
                fragment = new GameHistoryFragment();
                break;
            case 3:
                fragment = new BackupDialog();
                break;
            case 4:
                fragment = new HelpFragment();
                break;
            case 5:
                fragment = new AboutFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {

            String topElement = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();

            if (position == 0 && topElement.equals(getString(R.string.gameFragment))) {

                String dialogTitle;
                String dialogQuestion;
                final Activity activity = this;

                if (game.getFinished() == 1) {
                    dialogTitle = getString(R.string.backToMainMenu);
                    dialogQuestion = getString(R.string.backToMainMenuQuestion);
                } else {
                    dialogTitle = getString(R.string.quitGame);
                    dialogQuestion = getString(R.string.leaveGameQuestion);
                }

                new AlertDialog.Builder(activity)
                        .setTitle(dialogTitle)
                        .setMessage(dialogQuestion)
                        .setIcon(android.R.drawable.ic_menu_help)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if ((game.getFinished() == 0))
                                    new AlertDialog.Builder(activity)
                                            .setTitle(R.string.quitGame)
                                            .setMessage(R.string.quitGameQuestion)
                                            .setIcon(android.R.drawable.ic_menu_help)
                                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    saveGameToDb(1);
                                                    fm.popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                    fm.beginTransaction().replace(R.id.content_frame, new MainMenuFragment()).addToBackStack(getString(R.string.mainMenuFragment)).commit();
                                                }
                                            })
                                            .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int whichButton) {
                                                    fm.popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                    fm.beginTransaction().replace(R.id.content_frame, new MainMenuFragment()).addToBackStack(getString(R.string.mainMenuFragment)).commit();
                                                }
                                            })
                                            .show();
                                else {
                                    fm.popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    fm.beginTransaction().replace(R.id.content_frame, new MainMenuFragment()).addToBackStack(getString(R.string.mainMenuFragment)).commit();
                                }

                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
            } else if (position == 0) {
                fm.popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fm.beginTransaction().replace(R.id.content_frame, new MainMenuFragment()).addToBackStack(getString(R.string.mainMenuFragment)).commit();
            } else if (position == 1) {
                fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.playerManagementFragment)).commit();
            } else if (position == 2) {
                fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.gameHistoryFragment)).commit();
            } else if (position == 3) {
                if (!topElement.equals(getString(R.string.gameFragment))) {
                    BackupDialog backupDialog = new BackupDialog();
                    backupDialog.show(fm, getString(R.string.backupDialog));
                } else new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.backup))
                        .setMessage(R.string.backupDuringGame)
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
            } else if (position == 4) {
                fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.helpFragment)).commit();
            } else if (position == 5) {
                fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.aboutFragment)).commit();
            }

            drawerList.setItemChecked(position, true);
            drawerList.setSelection(position);
            drawerLayout.closeDrawer(drawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    private void saveGameToDb(int save) {
        game.setSaved(save);
        gds.saveGame(game);
    }

    public void onDestroy() {
        super.onDestroy();

        gds.close();
        pds.close();
    }

    public GamesDataSource getGamesDataSource() {
        return gds;
    }

    public PlayersDataSource getPlayersDataSource() {
        return pds;
    }

    public boolean isDrawerOpened() {
        return drawerOpened;
    }

    public Game getHistoryGame() {
        return historyGame;
    }

    public void setHistoryGame(Game prevGame) {
        this.historyGame = prevGame;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

    }

    public class DrawerItemCustomAdapter extends ArrayAdapter<ObjectDrawerItem> {

        Context mContext;
        int layoutResourceId;
        ObjectDrawerItem data[] = null;

        public DrawerItemCustomAdapter(Context mContext, int layoutResourceId, ObjectDrawerItem[] data) {

            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);

            ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.imageViewIcon);
            TextView textViewName = (TextView) convertView.findViewById(R.id.textViewName);
            TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);

            ObjectDrawerItem folder = data[position];

            imageViewIcon.setImageResource(folder.icon);
            textViewName.setText(folder.name);
            textViewDescription.setText(folder.description);

            return convertView;
        }

    }


}
