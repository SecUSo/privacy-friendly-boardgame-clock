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
package org.secuso.privacyfriendlyboardgameclock.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.model.Game
import androidx.core.view.isGone

/**
 * Created by Quang Anh Dang on 03.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for resuming saved games
 */
class ResumeGameActivity : BaseActivity(), ItemClickListener {
    private val loadGameFAB by lazy { findViewById<FloatingActionButton>(R.id.fab_start_game) }
    private val deleteGameFAB by lazy { findViewById<FloatingActionButton>(R.id.fab_delete_game) }
    private lateinit var gameListAdapter: GameListAdapter
    private val gds by lazy { GamesDataSourceSingleton.getInstance(this) }
    private var chosenGame: Game? = null
    private val actionModeCallback: ActionModeCallback by lazy { ActionModeCallback() }
    private var actionMode: ActionMode? = null
    private val fabActive by lazy { getResources().getColor(R.color.fabActive)  }
    private val fabInactive by lazy { getResources().getColor(R.color.fabInactive)  }

    private val selectAGameToast: View.OnClickListener = View.OnClickListener {
        Toast.makeText(this, R.string.pleaseChooseAGame, Toast.LENGTH_SHORT).show()
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resume)

        loadGameFAB.setOnClickListener(selectAGameToast)
        deleteGameFAB.setOnClickListener(onFABDeleteListenter())

        // Recycle View for Game List
        gameListAdapter = GameListAdapter(this, gds.getSavedGames(), this)
        findViewById<RecyclerView>(R.id.savedGamesList).apply {
            layoutManager = LinearLayoutManager(this@ResumeGameActivity)
            setHasFixedSize(true)
            adapter = gameListAdapter
            itemAnimator = null
        }
    }

    private fun resumeGame() = View.OnClickListener {
        if (chosenGame != null) {
            gds.game = chosenGame
            val intent = if (chosenGame!!.game_mode == TAGHelper.TIME_TRACKING) {
                Intent(this@ResumeGameActivity, GameTimeTrackingModeActivity::class.java)
            } else {
                Intent(this@ResumeGameActivity, GameCountDownActivity::class.java)
            }

            startActivity(intent)
        } else {
            Toast.makeText(this@ResumeGameActivity, R.string.pleaseChooseAGame, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemClick(view: View?, position: Int) {
        if (actionMode != null) {
            toggleSelection(position)
        } else {
            // If no game has been selected yet
            if (chosenGame == null) {
                gameListAdapter.setSimpleClickedSelected(true)
                gameListAdapter.setLongClickedSelected(false)
                chosenGame = gameListAdapter.getGame(position)
                gameListAdapter.toggleSelection(position)
                loadGameFAB.backgroundTintList = ColorStateList.valueOf(fabActive)
                loadGameFAB.setOnClickListener(resumeGame())
            } else {
                // If the same game has been selected again, deselect this
                if (gameListAdapter.isSelected(position)) {
                    gameListAdapter.setSimpleClickedSelected(false)
                    gameListAdapter.setLongClickedSelected(false)
                    chosenGame = null
                    // so all the check box disappear
                    gameListAdapter.notifyDataSetChanged()
                    gameListAdapter.clearSelection()
                    loadGameFAB.backgroundTintList = ColorStateList.valueOf(fabInactive)
                    loadGameFAB.setOnClickListener(selectAGameToast)
                } else {
                    gameListAdapter.setSimpleClickedSelected(true)
                    gameListAdapter.setLongClickedSelected(false)
                    chosenGame = gameListAdapter.getGame(position)
                    gameListAdapter.clearSelection()
                    gameListAdapter.toggleSelection(position)
                    loadGameFAB.backgroundTintList = ColorStateList.valueOf(fabActive)
                    loadGameFAB.setOnClickListener(resumeGame())
                }
            }
        }
    }

    override fun onItemLongClicked(view: View?, position: Int): Boolean {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
        return true
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private fun toggleSelection(position: Int) {
        if (gameListAdapter.isLongClickedSelected()) {
            gameListAdapter.toggleSelection(position)
            val count = gameListAdapter.selectedItemCount
            if (count == 0) {
                actionMode?.finish()
            } else {
                actionMode?.title = count.toString()
                actionMode?.invalidate()
            }
        }
    }

    /**
     * remove all the selected games
     * @return
     */
    private fun onFABDeleteListenter() = View.OnClickListener {
        gameListAdapter.removeItems(gameListAdapter.getSelectedItems())
        actionMode?.finish()
        actionMode = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.player_management_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            if (actionMode == null) actionMode = startSupportActionMode(actionModeCallback)
        }
        return true
    }

    private fun switchVisibilityOf2FABs() {
        if (loadGameFAB.visibility != View.GONE && deleteGameFAB.isGone) {
            loadGameFAB.visibility = View.GONE
            deleteGameFAB.visibility = View.VISIBLE
        } else {
            loadGameFAB.visibility = View.VISIBLE
            deleteGameFAB.visibility = View.GONE
        }
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/
    private inner class ActionModeCallback : ActionMode.Callback {
        @Suppress("unused")
        private val TAG: String = ActionModeCallback::class.java.getSimpleName()

        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            gameListAdapter.setLongClickedSelected(true)
            gameListAdapter.setSimpleClickedSelected(false)
            mode.menuInflater.inflate(R.menu.selected_menu, menu)
            switchVisibilityOf2FABs()
            gameListAdapter.clearSelection()
            gameListAdapter.notifyDataSetChanged()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            gameListAdapter.setLongClickedSelected(false)
            gameListAdapter.clearSelection()
            actionMode = null
            switchVisibilityOf2FABs()
            gameListAdapter.clearSelection()
            gameListAdapter.notifyDataSetChanged()
        }
    }
}
