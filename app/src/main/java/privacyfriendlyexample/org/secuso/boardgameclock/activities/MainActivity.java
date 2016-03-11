package privacyfriendlyexample.org.secuso.boardgameclock.activities;

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

import java.util.Locale;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.AboutFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.GameFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.HelpFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.LoadGameFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.MainMenuFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.NewGameFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.PlayerManagementFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.fragments.SettingsFragment;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.view.ObjectDrawerItem;

public class MainActivity extends AppCompatActivity {

    private ListView drawerList;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    boolean drawerOpened = false;
    private String activityTitle;
    protected Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.app_name));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#024265")));

        drawerList = (ListView) findViewById(R.id.navList);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        activityTitle = getTitle().toString();
        game = new Game();

        addDrawerItems();
        setupDrawer();

        // load language
        SharedPreferences settings = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String lang = settings.getString("Language", "EN");
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        ObjectDrawerItem[] drawerItem = new ObjectDrawerItem[3];

        drawerItem[0] = new ObjectDrawerItem(R.drawable.ic_tutorial, getString(R.string.action_main), "");
        drawerItem[1] = new ObjectDrawerItem(R.drawable.ic_action_help, getString(R.string.action_help), "");
        drawerItem[2] = new ObjectDrawerItem(R.drawable.ic_action_about, getString(R.string.action_about), "");

        DrawerItemCustomAdapter adapter = new DrawerItemCustomAdapter(this, R.layout.listview_item_row, drawerItem);
        drawerList.setAdapter(adapter);

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainMenuFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuFragment));
        fragmentTransaction.commit();

    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void newGameButton(View v) {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new NewGameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.newGameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }

    public void resumeGameButton(View v) {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new LoadGameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.loadGameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();

    }

    public void playerManagementButton(View v) {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new PlayerManagementFragment());
        fragmentTransaction.addToBackStack(getString(R.string.playerManagementFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();

    }

    public void settingsButton(View v){
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new SettingsFragment());
        fragmentTransaction.addToBackStack(getString(R.string.settingsFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }

    public void addContactSelectionButton(View v) {
    }

    public void languageButton(View v){

    }

    public void themeButton(View v){

    }

    public void importBackupButton(View v){

    }

    public void exportBackupButton(View v){

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

    public void saveGameButton(View v){

    }

    public void loadGameButton(View v){

    }

    public void finishGameButton(View v){

    }

    public void exitApplicationButton(View v){

    }


    @Override
    public void onBackPressed() {

        if (drawerOpened) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void addDrawerItems() {
        String[] mNavigationDrawerItemTitles = {getString(R.string.action_main), getString(R.string.action_help), getString(R.string.action_about)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mNavigationDrawerItemTitles);
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


    private void selectItem(int position) {

        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new MainMenuFragment();
                break;
            case 1:
                fragment = new HelpFragment();
                break;
            case 2:
                fragment = new AboutFragment();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();

            if (position == 0) {
                getFragmentManager().popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(getString(R.string.mainMenuFragment)).commit();
            }
            else
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();

            drawerList.setItemChecked(position, true);
            drawerList.setSelection(position);
            drawerLayout.closeDrawer(drawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
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
