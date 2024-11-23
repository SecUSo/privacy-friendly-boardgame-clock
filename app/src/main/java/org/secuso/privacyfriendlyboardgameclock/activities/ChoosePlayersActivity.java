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

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementChooseModeFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementContactListFragment;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Quang Anh Dang on 06.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Choose Player Activity after creating a new game
 */

public class ChoosePlayersActivity extends BaseActivity implements ItemClickListener {
    private List<Player> listPlayers;
    private GamesDataSourceSingleton gds;
    private PlayersDataSourceSingleton pds;
    private RecyclerView playersRecycleView;
    private PlayerListAdapter playerListAdapter;
    private LinearLayoutManager layoutManager;
    private FloatingActionButton fabStartGame;
    private FloatingActionButton fabDelete;
    private int fabActive;
    private int fabInactive;
    // To toggle selection mode
    private ChoosePlayersActivity.ActionModeCallback actionModeCallback = new ChoosePlayersActivity.ActionModeCallback();
    private ActionMode actionMode;
    private View insertAlert;
    private View emptyListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if(checkIfSingletonDataIsCorrupt()) return;

        setContentView(R.layout.activity_choose_players);
        fabActive = getResources().getColor(R.color.fabActive);
        fabInactive = getResources().getColor(R.color.fabInactive);
        gds = GamesDataSourceSingleton.getInstance(this);
        pds = PlayersDataSourceSingleton.getInstance(this);
        listPlayers = pds.getAllPlayers();

        // FAB Listener
        fabStartGame = findViewById(R.id.fab_start_game);
        fabStartGame.setBackgroundColor(R.drawable.button_disabled);
        fabStartGame.setOnClickListener(selectPlayerToast());

        fabDelete = findViewById(R.id.fab_delete_player);
        fabDelete.setOnClickListener(onFABDeleteListenter());

        // Lookup the recyclerview in fragment layout
        layoutManager = new LinearLayoutManager(this);
        playersRecycleView = findViewById(R.id.player_list);
        playersRecycleView.setHasFixedSize(true);
        playerListAdapter = new PlayerListAdapter(this, listPlayers, this);
        playersRecycleView.setAdapter(playerListAdapter);
        playersRecycleView.setLayoutManager(layoutManager);
        playersRecycleView.setItemAnimator(null);

        insertAlert = findViewById(R.id.insert_alert);
        emptyListLayout = findViewById(R.id.emptyListLayout);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(1500); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        insertAlert.startAnimation(anim);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(playerListAdapter.getPlayersList().size() == 0){
            insertAlert.setVisibility(View.VISIBLE);
            emptyListLayout.setVisibility(View.VISIBLE);
        } else {
            insertAlert.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.choose_players);
        // disable NavigationDrawer
        setDrawerEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case TAGHelper.REQUEST_READ_CONTACT_CODE:{
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Permission granted
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    Fragment prev = fm.findFragmentByTag("dialog");
                    if(prev != null) ft.remove(prev);
                    ft.addToBackStack(null);

                    // Create and show the dialog
                    PlayerManagementContactListFragment contactListFragment = new PlayerManagementContactListFragment();
                    contactListFragment.show(ft, "dialog");
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private View.OnClickListener selectPlayerToast(){
        return new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                showToast(getResources().getString(R.string.selectAtLeast2Players));
            }
        };
    }

    /**
     * remove all the selected players
     * @return
     */
    private View.OnClickListener onFABDeleteListenter() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerListAdapter.removeItems(playerListAdapter.getSelectedItems());
                actionMode.finish();
                actionMode = null;
                if(playerListAdapter.getPlayersList().size() == 0){
                    insertAlert.setVisibility(View.VISIBLE);
                    emptyListLayout.setVisibility(View.VISIBLE);
                } else {
                    insertAlert.setVisibility(View.GONE);
                    emptyListLayout.setVisibility(View.GONE);
                }
            }
        };
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }

    @Override
    public void onItemClick(View view, int position) {
        if(playerListAdapter.isLongClickedSelected() && !playerListAdapter.isSimpleClickedSelected()){
            toggleSelection(position);
        }
        else{
            playerListAdapter.setSimpleClickedSelected(true);
            playerListAdapter.setLongClickedSelected(false);
            toggleSelection(position);
            if (playerListAdapter.getSelectedItemCount() >= 2 && playerListAdapter.isSimpleClickedSelected()) {
                fabStartGame.setBackgroundTintList(ColorStateList.valueOf(fabActive));
                fabStartGame.setOnClickListener(createNewGame());
            }
            else{
                fabStartGame.setBackgroundTintList(ColorStateList.valueOf(fabInactive));
                fabStartGame.setOnClickListener(selectPlayerToast());
            }
        }
    }

    @Override
    public boolean onItemLongClicked(View view, int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
        return true;
    }

    /**
     *
     * @return OnClickListener
     */
    private View.OnClickListener createNewGame() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Game game = gds.getGame();
                if(game == null){
                    showToast("Data Corruppted! You have been returned to Main Menu.");
                    showMainMenu();
                    return;
                }
                List<Player> selectedPlayers = playerListAdapter.getOrderdSelectedPlayers();
                HashMap<Long, Long> player_round_times = new HashMap<>();
                for (Player p : selectedPlayers) {
                    player_round_times.put(p.getId(), Long.valueOf(game.getRound_time()));
                }

                HashMap<Long, Long> players_rounds = new HashMap<>();
                for (Player p : selectedPlayers) {
                    players_rounds.put(p.getId(), Long.valueOf(1));
                }

                long dateMs = System.currentTimeMillis();

                game = gds.createGame(dateMs, selectedPlayers, player_round_times, players_rounds, game.getName(), game.getRound_time(),
                        game.getGame_time(), game.getReset_round_time(), game.getGame_mode(), game.getRound_time_delta(), game.getGame_time(), 0, 0, game.getSaved(), 0, game.getGame_time_infinite(),
                        game.getChess_mode(), 0);

                //start player index
                if (game.getGame_mode() == TAGHelper.CLOCKWISE || game.getGame_mode() == TAGHelper.MANUAL_SEQUENCE || game.getGame_mode() == TAGHelper.TIME_TRACKING) {
                    game.setStartPlayerIndex(0);
                    game.setNextPlayerIndex(1);
                } else if (game.getGame_mode() == TAGHelper.COUNTER_CLOCKWISE) {
                    game.setStartPlayerIndex(0);
                    game.setNextPlayerIndex(selectedPlayers.size() - 1);
                } else if (game.getGame_mode() == TAGHelper.RANDOM) {
                    game.setStartPlayerIndex(0);

                    int randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                    while (randomPlayerIndex == game.getStartPlayerIndex())
                        randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                    game.setNextPlayerIndex(randomPlayerIndex);
                }

                game.setPlayers(selectedPlayers);
                game.setPlayer_round_times(player_round_times);
                game.setPlayer_rounds(players_rounds);
                gds.setGame(game);

                // if game is finally created and game time is infinite, set game time to -1
                if (game.getGame_time_infinite() == 1) {
                    game.setGame_time(0);
                    game.setCurrentGameTime(TAGHelper.DEFAULT_VALUE_LONG);
                }

                // store game number
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ChoosePlayersActivity.this);
                int gameNumber = settings.getInt("gameNumber", 1);
                gameNumber++;
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("gameNumber", gameNumber);
                editor.commit();

                startNewGame();
            }
        };
    }

    private void startNewGame() {
        Intent intent;
        if(gds.getGame().getGame_mode() == TAGHelper.TIME_TRACKING)
            intent = new Intent(ChoosePlayersActivity.this, GameTimeTrackingModeActivity.class);
        else
            intent = new Intent(ChoosePlayersActivity.this, GameCountDownActivity.class);
        startActivity(intent);
    }

    /**
     * Infalte the Actionicons on Toolbar, in this case the plus icon
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_newplayer) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag(TAGHelper.DIALOG_FRAGMENT);
            if (prev != null) ft.remove(prev);
            ft.addToBackStack(null);

            // Create and show the dialog
            PlayerManagementChooseModeFragment chooseDialogFragment = PlayerManagementChooseModeFragment.newInstance("Choose how to create new player:");
            chooseDialogFragment.show(ft, TAGHelper.DIALOG_FRAGMENT);
        }
        return true;
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        playerListAdapter.toggleSelection(position);
        int count = playerListAdapter.getSelectedItemCount();
        if(playerListAdapter.isSimpleClickedSelected()){
            if (count == 0) {
                playerListAdapter.setSimpleClickedSelected(false);
                playerListAdapter.setLongClickedSelected(false);
                playerListAdapter.notifyDataSetChanged();
            }
            if(count == 1) playerListAdapter.notifyDataSetChanged();
        }
        else if(playerListAdapter.isLongClickedSelected()){
            if (count == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle(String.valueOf(count));
                actionMode.invalidate();
            }
        }
    }

    private void switchVisibilityOf2FABs(){
        if(fabStartGame.getVisibility() != View.GONE && fabDelete.getVisibility() == View.GONE){
            fabStartGame.setVisibility(View.GONE);
            fabDelete.setVisibility(View.VISIBLE);
        }else{
            fabStartGame.setVisibility(View.VISIBLE);
            fabDelete.setVisibility(View.GONE);
        }
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ChoosePlayersActivity.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            playerListAdapter.setSimpleClickedSelected(false);
            playerListAdapter.setLongClickedSelected(true);
            mode.getMenuInflater().inflate (R.menu.selected_menu, menu);
            switchVisibilityOf2FABs();
            playerListAdapter.clearSelection();
            playerListAdapter.notifyDataSetChanged();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            playerListAdapter.setSimpleClickedSelected(false);
            playerListAdapter.setLongClickedSelected(false);
            playerListAdapter.clearSelection();
            actionMode = null;
            switchVisibilityOf2FABs();
            playerListAdapter.clearSelection();
            playerListAdapter.notifyDataSetChanged();
        }
    }
}
