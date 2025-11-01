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

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.activities.game.GameViewModel
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.fragments.GameHistoryInfoDialogFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the activity for the Game History
 */
class GameHistoryActivity : BaseActivity(), ItemClickListener {

    private val viewModel by lazy { ViewModelProvider(this)[GameHistoryActivityViewModel::class.java] }
    @JvmField
    var selectedGame: GameWithPlayer? = null
    private val gamesList: MutableList<GameWithPlayer> by lazy { viewModel.getAllGames().toMutableList() }
    private lateinit var gameListAdapter: GameListAdapter
    private val fabDeleteButton: FloatingActionButton by lazy { findViewById(R.id.fab_delete_game) }
    private val actionModeCallback: ActionModeCallback by lazy { ActionModeCallback() }
    private var actionMode: ActionMode? = null

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_game_history

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_history)

        // RecycleView for Game List
        gameListAdapter = GameListAdapter(this, gamesList, this)
        findViewById<RecyclerView>(R.id.game_list).apply { 
            setHasFixedSize(true)
            adapter = gameListAdapter
            layoutManager = LinearLayoutManager(this@GameHistoryActivity)
            itemAnimator = null
        }

        // Delete FAB
        fabDeleteButton.setOnClickListener(onFABDeleteListenter())
    }

    override fun onItemClick(view: View?, position: Int) {
        if (actionMode != null) {
            toggleSelection(position)
        } else {
            selectedGame = gamesList[position]
            val fm = supportFragmentManager
            val ft = fm.beginTransaction()
            val prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT)
            if (prev != null) ft.remove(prev)
            ft.addToBackStack(null)

            // Create and show the dialog
            val showGameInfo = GameHistoryInfoDialogFragment()
            showGameInfo.show(ft, TAGHelper.DIALOG_FRAGMENT)
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
        gameListAdapter.toggleSelection(position)
        val count = gameListAdapter.selectedItemCount

        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
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

    /**
     * Infalte the Actionicons on Toolbar, in this case the delete icon
     * @param menu
     * @return
     */
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

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/
    private inner class ActionModeCallback : ActionMode.Callback {
        @Suppress("unused")
        private val TAG: String = ActionModeCallback::class.java.simpleName

        override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
            gameListAdapter.setLongClickedSelected(true)
            gameListAdapter.setSimpleClickedSelected(false)
            mode.menuInflater.inflate(R.menu.selected_menu, menu)
            fabDeleteButton.visibility = View.VISIBLE
            // so all check box are visible
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
            fabDeleteButton.visibility = View.GONE
            // so all check box are gone
            gameListAdapter.clearSelection()
            gameListAdapter.notifyDataSetChanged()
        }
    }
}
