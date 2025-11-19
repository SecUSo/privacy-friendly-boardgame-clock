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
package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import androidx.core.view.isGone

/**
 * Created by Quang Anh Dang on 06.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Choose Player Activity after creating a new game
 */
class ChoosePlayersActivity : BaseActivity(), ItemClickListener {
    private val viewModel by lazy { ViewModelProvider(this)[ChoosePlayersActivityViewModel::class.java] }
    private lateinit var playerListAdapter: PlayerListAdapter
    private val fabStartGame: FloatingActionButton by lazy { findViewById(R.id.fab_start_game) }
    private val fabDelete: FloatingActionButton by lazy { findViewById(R.id.fab_delete_player) }
    private val fabActive by lazy { getResources().getColor(R.color.fabActive) }
    private val fabInactive by lazy { getResources().getColor(R.color.fabInactive) }

    // To toggle selection mode
    private val actionModeCallback by lazy { ActionModeCallback() } 
    private var actionMode: ActionMode? = null
    private val insertAlert: View by lazy { findViewById(R.id.insert_alert) }
    private val emptyListLayout: View by lazy { findViewById(R.id.emptyListLayout) }

    private lateinit var pictureConsumer: (Bitmap) -> Unit
    private val useCamera = useCameraForPlayerPicture {
        val bitmap = it.planes[0].buffer.let { buffer ->
            val bytes = ByteArray(buffer.capacity())
            buffer[bytes]
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        }
        pictureConsumer(bitmap)
    }

    private val createNewPlayerDialog by lazy {
        buildCreateNewPlayerDialog(
            onPlayerCreated = {
                viewModel.addPlayer(it)
                playerListAdapter.playersList.add(it)
                playerListAdapter.notifyItemInserted(playerListAdapter.itemCount - 1)
            },
            useCamera = {
                pictureConsumer = it
                useCamera()
            }
        )
    }

    private var data: Cursor? = null
    private val createNewPlayerFromContactDialog by lazy {
        buildCreatePlayerFromContactDialog({ data }) { name, icon ->
            viewModel.addPlayer(name, icon)
        }
    }
    private val useContacts = useContactsForAddingPlayers {
        data = it
        createNewPlayerFromContactDialog.show()
    }

    private val choosePlayerCreationMethod by lazy {
        buildChooseNewPlayerCreationMethodDialog(
            useContacts,
            createNewPlayerDialog
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_players)

        // FAB Listener
        fabStartGame.setBackgroundColor(R.drawable.button_disabled)
        fabStartGame.setOnClickListener(selectPlayerToast())

        fabDelete.setOnClickListener(onFABDeleteListenter())

        playerListAdapter = PlayerListAdapter(this, viewModel.getAllPlayersSync(), this)
        findViewById<RecyclerView>(R.id.player_list).apply {
            setHasFixedSize(true)
            adapter = playerListAdapter
            layoutManager = LinearLayoutManager(this@ChoosePlayersActivity)
            itemAnimator = null
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getAllPlayers().collect {
                    playerListAdapter.playersList = it
                    playerListAdapter.notifyDataSetChanged()
                }
            }
        }

        val anim: Animation = AlphaAnimation(0.0f, 1.0f).apply {
            duration = 1500
            startOffset = 20
            repeatMode = Animation.REVERSE
            repeatCount = Animation.INFINITE
        }
        insertAlert.startAnimation(anim)
    }

    override fun onResume() {
        super.onResume()
        if (playerListAdapter.playersList.isEmpty()) {
            insertAlert.visibility = View.VISIBLE
            emptyListLayout.visibility = View.VISIBLE
        } else {
            insertAlert.visibility = View.GONE
            emptyListLayout.visibility = View.GONE
        }
    }

    private fun selectPlayerToast() = View.OnClickListener { 
        Toast.makeText(this, getResources().getString(R.string.selectAtLeast2Players), Toast.LENGTH_SHORT).show()
    }

    /**
     * remove all the selected players
     * @return
     */
    private fun onFABDeleteListenter() = View.OnClickListener {
        playerListAdapter.removeItems(playerListAdapter.getSelectedItems())
        actionMode?.finish()
        actionMode = null
        if (playerListAdapter.playersList.isEmpty()) {
            insertAlert.visibility = View.VISIBLE
            emptyListLayout.visibility = View.VISIBLE
        } else {
            insertAlert.visibility = View.GONE
            emptyListLayout.visibility = View.GONE
        }
    }
    
    override fun onItemClick(view: View?, position: Int) {
        if (playerListAdapter.isLongClickedSelected() && !playerListAdapter.isSimpleClickedSelected()) {
            toggleSelection(position)
        } else {
            playerListAdapter.setSimpleClickedSelected(true)
            playerListAdapter.setLongClickedSelected(false)
            toggleSelection(position)
            if (playerListAdapter.selectedItemCount >= 2 && playerListAdapter.isSimpleClickedSelected()) {
                fabStartGame.backgroundTintList = ColorStateList.valueOf(fabActive)
                fabStartGame.setOnClickListener(createNewGame())
            } else {
                fabStartGame.backgroundTintList = ColorStateList.valueOf(fabInactive)
                fabStartGame.setOnClickListener(selectPlayerToast())
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
     *
     * @return OnClickListener
     */
    private fun createNewGame() = View.OnClickListener {
        val selectedPlayers = playerListAdapter.getOrderdSelectedPlayers()
        val gameId = intent.getLongExtra(GameViewModel.EXTRA_GAME_ID, -1)

        if (gameId == -1L) {
            throw IllegalStateException("No gameId was supplied in the intent")
        }

        lifecycleScope.launch {
            val game = viewModel.createGame(gameId, selectedPlayers)
            val intent = if (game.gameMode == TAGHelper.TIME_TRACKING) {
                Intent(this@ChoosePlayersActivity, GameTimeTrackingModeActivity::class.java)
            } else {
                Intent(this@ChoosePlayersActivity, GameCountDownActivity::class.java)
            }
            intent.putExtra(GameViewModel.EXTRA_GAME_ID, gameId)
            startActivity(intent)
        }
    }

    /**
     * Infalte the Actionicons on Toolbar, in this case the plus icon
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_add_newplayer) {
            choosePlayerCreationMethod.show()
        }
        else {
            super.onOptionsItemSelected(item)
        }
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
        playerListAdapter.toggleSelection(position)
        val count = playerListAdapter.selectedItemCount
        if (playerListAdapter.isSimpleClickedSelected()) {
            if (count == 0) {
                playerListAdapter.setSimpleClickedSelected(false)
                playerListAdapter.setLongClickedSelected(false)
                playerListAdapter.notifyDataSetChanged()
            }
            if (count == 1) playerListAdapter.notifyDataSetChanged()
        } else if (playerListAdapter.isLongClickedSelected()) {
            if (count == 0) {
                actionMode?.finish()
            } else {
                actionMode?.title = count.toString()
                actionMode?.invalidate()
            }
        }
    }

    private fun switchVisibilityOf2FABs() {
        if (fabStartGame.visibility != View.GONE && fabDelete.isGone) {
            fabStartGame.visibility = View.GONE
            fabDelete.visibility = View.VISIBLE
        } else {
            fabStartGame.visibility = View.VISIBLE
            fabDelete.visibility = View.GONE
        }
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
            playerListAdapter.setSimpleClickedSelected(false)
            playerListAdapter.setLongClickedSelected(true)
            mode.menuInflater.inflate(R.menu.selected_menu, menu)
            switchVisibilityOf2FABs()
            playerListAdapter.clearSelection()
            playerListAdapter.notifyDataSetChanged()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            playerListAdapter.setSimpleClickedSelected(false)
            playerListAdapter.setLongClickedSelected(false)
            playerListAdapter.clearSelection()
            actionMode = null
            switchVisibilityOf2FABs()
            playerListAdapter.clearSelection()
            playerListAdapter.notifyDataSetChanged()
        }
    }
}