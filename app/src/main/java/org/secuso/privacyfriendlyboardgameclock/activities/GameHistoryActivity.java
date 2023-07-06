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
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.GameHistoryInfoDialogFragment;
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.List;

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the activity for the Game History
 */

public class GameHistoryActivity extends BaseActivity implements ItemClickListener{

    private String selectedGameId = "-1";
    private Game selectedGame;
    private GamesDataSourceSingleton gds;
    private List<Game> gamesList;
    private GameListAdapter gameListAdapter;
    private FloatingActionButton fabDeleteButton;
    private android.app.FragmentManager fm;
    private GameHistoryActivity.ActionModeCallback actionModeCallback = new GameHistoryActivity.ActionModeCallback();
    private ActionMode actionMode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);
        fm = getFragmentManager();
        gds = GamesDataSourceSingleton.getInstance(this);
        gamesList = gds.getAllGames();

        // RecycleView for Game List
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        RecyclerView gamesRecycleView = findViewById(R.id.game_list);
        gamesRecycleView.setHasFixedSize(true);
        gameListAdapter = new GameListAdapter(this,gamesList,this);
        gamesRecycleView.setAdapter(gameListAdapter);
        gamesRecycleView.setLayoutManager(layoutManager);
        gamesRecycleView.setItemAnimator(null);

        // Delete FAB
        fabDeleteButton = findViewById(R.id.fab_delete_game);
        fabDeleteButton.setOnClickListener(onFABDeleteListenter());
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_game_history;
    }

    @Override
    public void onItemClick(View view, int position) {
        if(actionMode != null){
            toggleSelection(position);
        } else{
            selectedGame = gamesList.get(position);
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT);
            if(prev != null) ft.remove(prev);
            ft.addToBackStack(null);

            // Create and show the dialog
            GameHistoryInfoDialogFragment showGameInfo = new GameHistoryInfoDialogFragment();
            showGameInfo.show(ft, TAGHelper.DIALOG_FRAGMENT);
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
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        gameListAdapter.toggleSelection(position);
        int count = gameListAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    /**
     * remove all the selected games
     * @return
     */
    private View.OnClickListener onFABDeleteListenter() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gameListAdapter.removeItems(gameListAdapter.getSelectedItems());
                actionMode.finish();
                actionMode = null;

            }
        };
    }

    public Game getSelectedGame() {
        return selectedGame;
    }

    public void setSelectedGame(Game selectedGame) {
        this.selectedGame = selectedGame;
    }

    /**
     * Infalte the Actionicons on Toolbar, in this case the delete icon
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_management_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                if(actionMode == null)
                    actionMode = startSupportActionMode(actionModeCallback);
        }
        return true;
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = GameHistoryActivity.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            gameListAdapter.setLongClickedSelected(true);
            gameListAdapter.setSimpleClickedSelected(false);
            mode.getMenuInflater().inflate (R.menu.selected_menu, menu);
            fabDeleteButton.setVisibility(View.VISIBLE);
            // so all check box are visible
            gameListAdapter.clearSelection();
            gameListAdapter.notifyDataSetChanged();
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
            gameListAdapter.setLongClickedSelected(false);
            gameListAdapter.clearSelection();
            actionMode = null;
            fabDeleteButton.setVisibility(View.GONE);
            // so all check box are gone
            gameListAdapter.clearSelection();
            gameListAdapter.notifyDataSetChanged();
        }
    }
}
