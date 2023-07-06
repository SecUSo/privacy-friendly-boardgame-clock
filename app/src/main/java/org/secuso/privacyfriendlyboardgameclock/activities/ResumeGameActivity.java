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

import android.content.Intent;
import android.content.res.ColorStateList;
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
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.List;

/**
 * Created by Quang Anh Dang on 03.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for resuming saved games
 */

public class ResumeGameActivity extends BaseActivity implements ItemClickListener {
    private FloatingActionButton loadGameFAB, deleteGameFAB;
    private RecyclerView gameListRecyleView;
    private GameListAdapter gameListAdapter;
    private List<Game> savedGamesList;
    private GamesDataSourceSingleton gds;
    private Game chosenGame;
    private ResumeGameActivity.ActionModeCallback actionModeCallback = new ResumeGameActivity.ActionModeCallback();
    private ActionMode actionMode;
    private int fabActive;
    private int fabInactive;
    private View.OnClickListener selectAGameToast = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showToast(getString(R.string.pleaseChooseAGame));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gds = GamesDataSourceSingleton.getInstance(this);
        savedGamesList = gds.getSavedGames();
        fabActive = getResources().getColor(R.color.fabActive);
        fabInactive = getResources().getColor(R.color.fabInactive);

        setContentView(R.layout.activity_resume);

        loadGameFAB = findViewById(R.id.fab_start_game);
        loadGameFAB.setOnClickListener(selectAGameToast);
        deleteGameFAB = findViewById(R.id.fab_delete_game);
        deleteGameFAB.setOnClickListener(onFABDeleteListenter());

        // Recycle View for Game List
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        gameListRecyleView = findViewById(R.id.savedGamesList);
        gameListRecyleView.setHasFixedSize(true);
        gameListAdapter = new GameListAdapter(this, savedGamesList, this);
        gameListRecyleView.setAdapter(gameListAdapter);
        gameListRecyleView.setLayoutManager(layoutManager);
        gameListRecyleView.setItemAnimator(null);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.action_resume_game);
        setDrawerEnabled(false);
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }

    private View.OnClickListener resumeGame(){
         return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chosenGame != null){
                    gds.setGame(chosenGame);
                    Intent intent;
                    if(chosenGame.getGame_mode() == TAGHelper.TIME_TRACKING)
                        intent = new Intent(ResumeGameActivity.this, GameTimeTrackingModeActivity.class);
                    else
                        intent = new Intent(ResumeGameActivity.this, GameCountDownActivity.class);

                    startActivity(intent);
                }
                else
                    showToast(getString(R.string.pleaseChooseAGame));
            }
        };
    }

    @Override
    public void onItemClick(View view, int position) {
        if(actionMode != null){
            toggleSelection(position);
        }else{
            // If no game has been selected yet
            if(chosenGame == null){
                gameListAdapter.setSimpleClickedSelected(true);
                gameListAdapter.setLongClickedSelected(false);
                chosenGame = gameListAdapter.getGame(position);
                gameListAdapter.toggleSelection(position);
                loadGameFAB.setBackgroundTintList(ColorStateList.valueOf(fabActive));
                loadGameFAB.setOnClickListener(resumeGame());
            }
            else{
                // If the same game has been selected again, deselect this
                if(gameListAdapter.isSelected(position)){
                    gameListAdapter.setSimpleClickedSelected(false);
                    gameListAdapter.setLongClickedSelected(false);
                    chosenGame = null;
                    // so all the check box disappear
                    gameListAdapter.notifyDataSetChanged();
                    gameListAdapter.clearSelection();
                    loadGameFAB.setBackgroundTintList(ColorStateList.valueOf(fabInactive));
                    loadGameFAB.setOnClickListener(selectAGameToast);
                }
                // If another game has been selected, clear all selection and select this one
                else{
                    gameListAdapter.setSimpleClickedSelected(true);
                    gameListAdapter.setLongClickedSelected(false);
                    chosenGame = gameListAdapter.getGame(position);
                    gameListAdapter.clearSelection();
                    gameListAdapter.toggleSelection(position);
                    loadGameFAB.setBackgroundTintList(ColorStateList.valueOf(fabActive));
                    loadGameFAB.setOnClickListener(resumeGame());
                }
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
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        if(gameListAdapter.isLongClickedSelected()){
            gameListAdapter.toggleSelection(position);
            int count = gameListAdapter.getSelectedItemCount();
            if (count == 0) {
                actionMode.finish();
            } else {
                actionMode.setTitle(String.valueOf(count));
                actionMode.invalidate();
            }
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

    private void switchVisibilityOf2FABs(){
        if(loadGameFAB.getVisibility() != View.GONE && deleteGameFAB.getVisibility() == View.GONE){
            loadGameFAB.setVisibility(View.GONE);
            deleteGameFAB.setVisibility(View.VISIBLE);
        }else{
            loadGameFAB.setVisibility(View.VISIBLE);
            deleteGameFAB.setVisibility(View.GONE);
        }
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ResumeGameActivity.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            gameListAdapter.setLongClickedSelected(true);
            gameListAdapter.setSimpleClickedSelected(false);
            mode.getMenuInflater().inflate (R.menu.selected_menu, menu);
            switchVisibilityOf2FABs();
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
            switchVisibilityOf2FABs();
            gameListAdapter.clearSelection();
            gameListAdapter.notifyDataSetChanged();
        }
    }
}
