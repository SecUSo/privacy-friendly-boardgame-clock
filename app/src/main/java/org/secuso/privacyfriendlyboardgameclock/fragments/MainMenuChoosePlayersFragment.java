package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Quang Anh Dang on 15.12.2017.
 *
 * @author Quang Anh Dang
 */
// TODO #4 Fragment backbutton not possible
public class MainMenuChoosePlayersFragment extends Fragment implements ItemClickListener{
    private MainActivity activity;
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
    private MainMenuChoosePlayersFragment.ActionModeCallback actionModeCallback = new MainMenuChoosePlayersFragment.ActionModeCallback();
    private ActionMode actionMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fabActive = getResources().getColor(R.color.fabActive);
        fabInactive = getResources().getColor(R.color.fabInactive);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) this.getActivity();
        gds = GamesDataSourceSingleton.getInstance(activity);
        pds = PlayersDataSourceSingleton.getInstance(activity);

        View rootView = inflater.inflate(R.layout.fragment_main_menu_choose_players, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.choose_players);
        container.removeAllViews();

        listPlayers = pds.getAllPlayers();

        // Set the plus icon in toolbar to add more players
        setHasOptionsMenu(true);

        // FAB Listener
        fabStartGame = (FloatingActionButton) rootView.findViewById(R.id.fab_start_game);
        fabStartGame.setBackgroundColor(R.drawable.button_disabled);
        fabStartGame.setOnClickListener(selectPlayerToast());

        fabDelete = (FloatingActionButton) rootView.findViewById(R.id.fab_delete_player);
        fabDelete.setOnClickListener(onFABDeleteListenter());

        // Lookup the recyclerview in fragment layout
        layoutManager = new LinearLayoutManager(activity);
        playersRecycleView = rootView.findViewById(R.id.player_list);
        playersRecycleView.setHasFixedSize(true);
        playerListAdapter = new PlayerListAdapter(activity, listPlayers, this);
        playersRecycleView.setAdapter(playerListAdapter);
        playersRecycleView.setLayoutManager(layoutManager);

        // disable NavigationDrawer
        ((MainActivity)activity).setDrawerEnabled(false);
        return rootView;
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

            }
        };
    }

    /**
     *
     * @return OnClickListener
     */
    private View.OnClickListener createNewGame() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Game game = ((MainActivity) activity).getGame();
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
                if (game.getGame_mode() == 0 || game.getGame_mode() == 3) {
                    game.setStartPlayerIndex(0);
                    game.setNextPlayerIndex(1);
                } else if (game.getGame_mode() == 1) {
                    game.setStartPlayerIndex(0);
                    game.setNextPlayerIndex(selectedPlayers.size() - 1);
                } else if (game.getGame_mode() == 2) {
                    game.setStartPlayerIndex(0);

                    int randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                    while (randomPlayerIndex == game.getStartPlayerIndex())
                        randomPlayerIndex = new Random().nextInt(selectedPlayers.size());
                    game.setNextPlayerIndex(randomPlayerIndex);
                }

                game.setPlayers(selectedPlayers);
                game.setPlayer_round_times(player_round_times);
                game.setPlayer_rounds(players_rounds);
                ((MainActivity) activity).setGame(game);

                // if game is finally created and game time is infinite, set game time to -1
                if (game.getGame_time_infinite() == 1) {
                    game.setGame_time(0);
                    game.setCurrentGameTime(TAGHelper.DEFAULT_VALUE_LONG);
                }

                // store game number
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
                int gameNumber = settings.getInt("gameNumber", 1);
                gameNumber++;
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("gameNumber", gameNumber);
                editor.commit();

                startNewGame();
            }
        };
    }

    private View.OnClickListener selectPlayerToast(){
        return new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                activity.showToast(getResources().getString(R.string.selectAtLeast2Players));
        }
        };
    }

    private void clearListSelections() {
        playerListAdapter.clearSelection();
    }

    public void startNewGame() {
        /*final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuGameFragment());
        fragmentTransaction.addToBackStack(getString(R.string.gameFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        clearListSelections();
        fragmentTransaction.commit();*/
    }

    /**
     * Infalte the Actionicons on Toolbar, in this case the plus icon
     * @param menu
     * @return
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_add_newplayer:
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag(TAGHelper.DIALOG_FRAGMENT);
                if(prev != null) ft.remove(prev);
                ft.addToBackStack(null);

                // Create and show the dialog
                PlayerManagementChooseModeFragment chooseDialogFragment = PlayerManagementChooseModeFragment.newInstance("Choose how to create new player:");
                chooseDialogFragment.show(ft,TAGHelper.DIALOG_FRAGMENT);
        }
        return true;
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
            actionMode = activity.startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);
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
            }
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
        private final String TAG = MainMenuChoosePlayersFragment.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            playerListAdapter.setSimpleClickedSelected(false);
            playerListAdapter.setLongClickedSelected(true);
            mode.getMenuInflater().inflate (R.menu.selected_menu, menu);
            switchVisibilityOf2FABs();
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
        }
    }
}
