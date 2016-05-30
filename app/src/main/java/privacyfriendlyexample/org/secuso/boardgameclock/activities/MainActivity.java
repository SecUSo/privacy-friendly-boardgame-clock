package privacyfriendlyexample.org.secuso.boardgameclock.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
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

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.db.PlayersDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.AboutFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.GameHistoryFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.HelpFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.MainMenuFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.NewGameFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.PlayerManagementFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.BackupDialog;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.WelcomeDialog;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.ObjectDrawerItem;

public class MainActivity extends AppCompatActivity {

    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    boolean drawerOpened = false;
    private String activityTitle;

    private Game game;
    private Game historyGame;

    private GamesDataSource gds;
    private PlayersDataSource pds;

    private Player playerForEditing;
    private SharedPreferences settings;
    private FragmentManager fm;

    private String[] PERMISSIONS = {Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fm = getFragmentManager();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstStart = settings.getBoolean("firstStart", true);
        if (firstStart) {
            // Android 6 Permission Requests
            if (Build.VERSION.SDK_INT >= 23) {
                if (!hasPermissions(this, PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, PERMISSIONS, 0);
                }
            }

            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.show(fm, getString(R.string.welcomeDialog));

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("firstStart", false);
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

        gds = new GamesDataSource(this);
        pds = new PlayersDataSource(this);

        gds.open();
        pds.open();
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
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

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {

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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }

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
                // Android 6 Permission Requests
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!hasPermissions(this, new String[]{Manifest.permission.READ_CONTACTS})) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 5);
                    } else
                        fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.playerManagementFragment)).commit();
                } else
                    fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.playerManagementFragment)).commit();

            } else if (position == 2) {
                fm.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.gameHistoryFragment)).commit();
            } else if (position == 3) {
                // Android 6 Permission Requests
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!hasPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 4);
                    } else {
                        BackupDialog backupDialog = new BackupDialog();
                        backupDialog.show(fm, getString(R.string.backupDialog));
                    }
                } else {
                    BackupDialog backupDialog = new BackupDialog();
                    backupDialog.show(fm, getString(R.string.backupDialog));
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 4: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    BackupDialog backupDialog = new BackupDialog();
                    backupDialog.show(fm, getString(R.string.backupDialog));

                } else {

                    AlertDialog.Builder dialog;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dialog = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    } else
                        dialog = new AlertDialog.Builder(this);

                    dialog.setTitle(R.string.error)
                            .setMessage(R.string.dialog_no_permissions)
                            .setPositiveButton(R.string.ok, null)
                            .setIcon(android.R.drawable.ic_menu_info_details)
                            .show();
                }
                return;
            }
            case 5:{
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new PlayerManagementFragment());
                fragmentTransaction.addToBackStack(getString(R.string.playerManagementFragment));
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                fragmentTransaction.commit();
                return;
            }
        }
    }

    private void saveGameToDb(int save) {
        game.setSaved(save);

        gds.saveGame(game);
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


}
