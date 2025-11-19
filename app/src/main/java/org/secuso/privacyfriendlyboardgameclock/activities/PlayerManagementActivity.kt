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

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.secuso.pfacore.model.DrawerElement
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementContactListFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.activities.game.buildChooseNewPlayerCreationMethodDialog
import org.secuso.privacyfriendlyboardgameclock.activities.game.buildCreateNewPlayerDialog
import org.secuso.privacyfriendlyboardgameclock.activities.game.useCameraForPlayerPicture

/**
 * Actionbar Tutorial
 * http://www.vogella.com/tutorials/AndroidActionBar/article.html#exercise-using-the-contextual-action-mode
 * Selection State tutorial
 * https://enoent.fr/blog/2015/01/18/recyclerview-basics/
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the Player Management Page
 */
class PlayerManagementActivity : BaseActivity(), ItemClickListener {
    private val viewModel by lazy { ViewModelProvider(this)[PlayerManagementActivityViewModel::class.java] }
    private val playersRecycleView by lazy { findViewById<RecyclerView>(R.id.player_list) }
    private lateinit var playerListAdapter: PlayerListAdapter
    private var listPlayers: MutableList<Player> = mutableListOf()
    private val fabAdd by lazy { findViewById<FloatingActionButton>(R.id.fab_add_new_player) }
    private val fabDelete by lazy { findViewById<FloatingActionButton>(R.id.fab_delete_player) }
    private val fm by lazy { supportFragmentManager }

    // To toggle selection mode
    private val actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    @JvmField
    var playerToEdit: Player? = null
    var playerToEditIndex: Int? = null
    private val insertAlert by lazy { findViewById<View>(R.id.insert_alert) }
    private val emptyListLayout by lazy { findViewById<View>(R.id.emptyListLayout) }

    override fun isActiveDrawerElement(element: DrawerElement) = element.icon == R.drawable.ic_menu_player_management

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

    private val choosePlayerCreationMethod by lazy {
        buildChooseNewPlayerCreationMethodDialog(
            createNewPlayerDialog
        )
    }

    private val editPlayerDialog by lazy {
        buildCreateNewPlayerDialog(
            onPlayerCreated = {
                viewModel.updatePlayer(it)
                playerListAdapter.playersList[playerToEditIndex!!] = it
                playerListAdapter.notifyItemChanged(playerToEditIndex!!)
                playerToEditIndex = null
                playerToEdit = null
            },
            useCamera = {
                pictureConsumer = it
                useCamera()
            },
            playerSupplier = { playerToEdit!! }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_management)
        // pds already opened in MainActivity

        listPlayers = viewModel.getAllPlayersSync().toMutableList()

        // FAB Listener
        fabAdd.setOnClickListener {
            choosePlayerCreationMethod.show()
        }
        fabDelete.setOnClickListener(onFABDeleteListenter())

        playerListAdapter = PlayerListAdapter(this, listPlayers, this)
        playersRecycleView.apply {
            setHasFixedSize(true)
            adapter = playerListAdapter
            layoutManager = LinearLayoutManager(this@PlayerManagementActivity)
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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onItemClick(view: View?, position: Int) {
        if (actionMode != null) {
            toggleSelection(position)
        } else {
            playerToEdit = playerListAdapter.getPlayer(position)
            playerToEditIndex = position
            editPlayerDialog.show()
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
        playerListAdapter.toggleSelection(position)
        val count = playerListAdapter.selectedItemCount

        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
        }
    }


    /**
     * remove all the selected players
     * @return
     */
    private fun onFABDeleteListenter() = View.OnClickListener {
            AlertDialog.Builder(this@PlayerManagementActivity)
                .setTitle(R.string.warning)
                .setMessage(R.string.playerDeleteWarning)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes) { dialog, whichButton ->
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
                .setNegativeButton(R.string.no, null)
                .show()
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
        } else {
            return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun switchVisibilityOf2FABs() {
        if (fabAdd.visibility != View.GONE && fabDelete.isGone) {
            fabAdd.visibility = View.GONE
            fabDelete.visibility = View.VISIBLE
        } else {
            fabAdd.visibility = View.VISIBLE
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
            playerListAdapter.setLongClickedSelected(true)
            playerListAdapter.setSimpleClickedSelected(false)
            mode.menuInflater.inflate(R.menu.selected_menu, menu)
            switchVisibilityOf2FABs()
            // so all check box are visible
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
            playerListAdapter.setLongClickedSelected(false)
            playerListAdapter.clearSelection()
            actionMode = null
            switchVisibilityOf2FABs()
            // so all check box are visible
            playerListAdapter.clearSelection()
            playerListAdapter.notifyDataSetChanged()
        }
    }

    companion object {
        @Suppress("unused")
        private val TAG: String = MainActivity::class.java.simpleName
    }
}