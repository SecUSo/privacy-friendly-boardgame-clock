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
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementChooseModeFragment
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementContactListFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.model.Player
import java.util.Random

/**
 * Created by Quang Anh Dang on 06.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Choose Player Activity after creating a new game
 */
class ChoosePlayersActivity : BaseActivity(), ItemClickListener {
    private val listPlayers: MutableList<Player> by lazy { pds.getAllPlayers() }
    private val gds: GamesDataSourceSingleton by lazy { GamesDataSourceSingleton.getInstance(this) }
    private val pds: PlayersDataSourceSingleton by lazy { PlayersDataSourceSingleton.getInstance(this) }
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

    /**
     * before starting to work, check if Database and Singleton Object (use to save some objects
     * and transferring objects between activity), if any attribute is null --> move to main activity
     * and remove all other activities --> start new
     */
    fun checkIfSingletonDataIsCorrupt(): Boolean {
        if (!(GamesDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull()
                    && PlayersDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull())
        ) {
            val intent = Intent(this, MainActivity::class.java)
            // clear all other activities
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        } else return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if (checkIfSingletonDataIsCorrupt()) return

        setContentView(R.layout.activity_choose_players)

        // FAB Listener
        fabStartGame.setBackgroundColor(R.drawable.button_disabled)
        fabStartGame.setOnClickListener(selectPlayerToast())

        fabDelete.setOnClickListener(onFABDeleteListenter())

        playerListAdapter = PlayerListAdapter(this, listPlayers, this)
        findViewById<RecyclerView>(R.id.player_list).apply {
            setHasFixedSize(true)
            adapter = playerListAdapter
            layoutManager = LinearLayoutManager(this@ChoosePlayersActivity)
            itemAnimator = null
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            TAGHelper.REQUEST_READ_CONTACT_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    val fm = supportFragmentManager
                    val ft = fm.beginTransaction()
                    val prev = fm.findFragmentByTag("dialog")
                    if (prev != null) ft.remove(prev)
                    ft.addToBackStack(null)

                    // Create and show the dialog
                    val contactListFragment = PlayerManagementContactListFragment()
                    contactListFragment.show(ft, "dialog")
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
            var game = gds.getGame()
            if (game == null) {
                Toast.makeText(this@ChoosePlayersActivity, "Data Corruppted! You have been returned to Main Menu.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ChoosePlayersActivity, MainActivity::class.java))
                finish()
            }
            val selectedPlayers = playerListAdapter.getOrderdSelectedPlayers()
            val player_round_times = HashMap<Long?, Long?>()
            for (p in selectedPlayers) {
                player_round_times[p.id] = game.round_time
            }

            val players_rounds = HashMap<Long?, Long?>()
            for (p in selectedPlayers) {
                players_rounds[p.id] = 1
            }

            val dateMs = System.currentTimeMillis()

            game = gds.createGame(
                dateMs,
                selectedPlayers,
                player_round_times,
                players_rounds,
                game.name,
                game.round_time,
                game.game_time,
                game.reset_round_time,
                game.game_mode,
                game.round_time_delta,
                game.game_time,
                0,
                0,
                game.saved,
                0,
                game.game_time_infinite,
                game.chess_mode,
                0
            )

            //start player index
            if (game.game_mode == TAGHelper.CLOCKWISE || game.game_mode == TAGHelper.MANUAL_SEQUENCE || game.game_mode == TAGHelper.TIME_TRACKING) {
                game.startPlayerIndex = 0
                game.nextPlayerIndex = 1
            } else if (game.game_mode == TAGHelper.COUNTER_CLOCKWISE) {
                game.startPlayerIndex = 0
                game.nextPlayerIndex = selectedPlayers.size - 1
            } else if (game.game_mode == TAGHelper.RANDOM) {
                game.startPlayerIndex = 0

                var randomPlayerIndex = Random().nextInt(selectedPlayers.size)
                while (randomPlayerIndex == game.startPlayerIndex) {
                    randomPlayerIndex = Random().nextInt(selectedPlayers.size)
                }
                game.nextPlayerIndex = randomPlayerIndex
            }

        game.players = selectedPlayers
        game.player_round_times = player_round_times
        game.player_rounds = players_rounds
        gds.game = game

        // if game is finally created and game time is infinite, set game time to -1
        if (game.game_time_infinite == 1) {
            game.game_time = 0
            game.currentGameTime = TAGHelper.DEFAULT_VALUE_LONG
        }

        // store game number
        val settings =
            PreferenceManager.getDefaultSharedPreferences(this@ChoosePlayersActivity)
        var gameNumber = settings.getInt("gameNumber", 1)
        gameNumber++
        val editor = settings.edit()
        editor.putInt("gameNumber", gameNumber)
        editor.commit()

        startNewGame()
        }

    private fun startNewGame() {
        val intent = if (gds.game.game_mode == TAGHelper.TIME_TRACKING) {
            Intent(this@ChoosePlayersActivity, GameTimeTrackingModeActivity::class.java)
        } else Intent(this@ChoosePlayersActivity, GameCountDownActivity::class.java)
        startActivity(intent)
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
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT)
            if (prev != null) ft.remove(prev)
            ft.addToBackStack(null)

            // Create and show the dialog
            val chooseDialogFragment =
                PlayerManagementChooseModeFragment.newInstance("Choose how to create new player:")
            chooseDialogFragment.show(ft, TAGHelper.DIALOG_FRAGMENT)
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
        if (fabStartGame.visibility != View.GONE && fabDelete.visibility == View.GONE) {
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
