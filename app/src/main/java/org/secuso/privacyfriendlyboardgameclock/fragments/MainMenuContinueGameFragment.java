package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.ColorStateList;
import android.os.Bundle;
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
import org.secuso.privacyfriendlyboardgameclock.activities.GameHistoryActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.List;

/**
 * Created by Quang Anh Dang on 15.12.2017.
 */

public class MainMenuContinueGameFragment extends Fragment implements ItemClickListener{
    // TODO Recycle View, FAB, ACtionbar, GameListAdapter with Viewholder, implements on click and onlongclick
    private MainActivity activity;
    private FloatingActionButton loadGameFAB, deleteGameFAB;
    private RecyclerView gameListRecyleView;
    private GameListAdapter gameListAdapter;
    private List<Game> savedGamesList;
    private GamesDataSourceSingleton gds;
    private Game chosenGame;
    private MainMenuContinueGameFragment.ActionModeCallback actionModeCallback = new MainMenuContinueGameFragment.ActionModeCallback();
    private ActionMode actionMode;
    private int fabActive;
    private int fabInactive;
    private View.OnClickListener selectAGameToast = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            activity.showToast(getString(R.string.pleaseChooseAGame));
        }
    };
    private View.OnClickListener resumeGame = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(chosenGame != null){
                activity.setGame(chosenGame);
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuGameFragment());
                fragmentTransaction.addToBackStack(getString(R.string.gameFragment));
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                fragmentTransaction.commit();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        gds = GamesDataSourceSingleton.getInstance(activity);
        savedGamesList = gds.getSavedGames();
        fabActive = getResources().getColor(R.color.fabActive);
        fabInactive = getResources().getColor(R.color.fabInactive);

        final View rootView = inflater.inflate(R.layout.fragment_main_menu_resume,null);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.loadGame);

        loadGameFAB = rootView.findViewById(R.id.fab_start_game);
        loadGameFAB.setOnClickListener(selectAGameToast);
        deleteGameFAB = rootView.findViewById(R.id.fab_delete_game);
        deleteGameFAB.setOnClickListener(onFABDeleteListenter());

        // Set the plus icon in toolbar to add more players
        setHasOptionsMenu(true);

        // Recycle View for Game List
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity);
        gameListRecyleView = rootView.findViewById(R.id.savedGamesList);
        gameListRecyleView.setHasFixedSize(true);
        gameListAdapter = new GameListAdapter(activity, savedGamesList, this);
        gameListRecyleView.setAdapter(gameListAdapter);
        gameListRecyleView.setLayoutManager(layoutManager);

        return rootView;
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
                loadGameFAB.setOnClickListener(resumeGame);
            }
            else{
                // If the same game has been selected again, deselect this
                if(gameListAdapter.isSelected(position)){
                    gameListAdapter.setSimpleClickedSelected(false);
                    gameListAdapter.setLongClickedSelected(false);
                    chosenGame = null;
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
                    loadGameFAB.setOnClickListener(resumeGame);
                }
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

    /**
     * Infalte the Actionicons on Toolbar, in this case the plus icon
     * @param menu
     * @return
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.player_management_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_delete:
                if(actionMode == null)
                    actionMode = activity.startSupportActionMode(actionModeCallback);
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
        private final String TAG = MainMenuContinueGameFragment.ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            gameListAdapter.setLongClickedSelected(true);
            gameListAdapter.setSimpleClickedSelected(false);
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
            gameListAdapter.setLongClickedSelected(false);
            gameListAdapter.clearSelection();
            actionMode = null;
            switchVisibilityOf2FABs();
        }
    }
}

