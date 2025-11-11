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

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.await
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.dialog.ValueSelectionDialog
import org.secuso.pfacore.model.permission.PFAPermission
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.pfacore.ui.declareUsage
import org.secuso.pfacore.ui.dialog.ShowSelectOptionDialog
import org.secuso.pfacore.ui.dialog.ShowValueSelectionDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.databinding.FragmentPlayerManagementNewplayerBinding
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementChooseModeFragment
import org.secuso.privacyfriendlyboardgameclock.fragments.PlayerManagementContactListFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import java.util.Random
import java.util.concurrent.Executors
import androidx.core.graphics.scale
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

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

    private fun createColorSelectionDialog(onColorChosen: (Int) -> Unit) = ColorPickerDialogBuilder
        .with(this@ChoosePlayersActivity)
        .setTitle("Choose color")
        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
        .density(12)
        .setOnColorSelectedListener { }
        .setPositiveButton("OK") { dialog, selectedColor, allColors -> onColorChosen(selectedColor)}
        .setNegativeButton("Cancel", null)
        .build()

    private lateinit var pictureConsumer: (Image) -> Unit

    private lateinit var useCamera: () -> Unit

    private val createNewPlayerDialog: ShowValueSelectionDialog<Player, FragmentPlayerManagementNewplayerBinding> by lazy {
        val valid = MutableLiveData(false)
        var icon: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_android)

        val bindingSupplier: () -> FragmentPlayerManagementNewplayerBinding = {
            val binding = FragmentPlayerManagementNewplayerBinding.inflate(layoutInflater)

            binding.editName.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    valid.postValue(s?.isNotEmpty() ?: false)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            val colorDialog = createColorSelectionDialog {
                binding.picture.setImageBitmap(icon)
                binding.picture.setColorFilter(it, PorterDuff.Mode.DST_OVER)
                binding.color.setBackgroundColor(it)
            }
            binding.color.setOnClickListener { colorDialog.show() }

            pictureConsumer = {
                val bitmap = it.planes[0].buffer.let { buffer ->
                    val bytes = ByteArray(buffer.capacity())
                    buffer[bytes]
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
                }
                runOnUiThread {
                    val icon = cutSquareBitmap(bitmap).scale(
                        binding.picture.width,
                        binding.picture.height,
                        false
                    )
                    binding.picture.setImageBitmap(icon)
                    binding.picture.colorFilter = null
                }

            }
            binding.picture.setOnClickListener { useCamera() }
            binding
        }

        val dialog = ValueSelectionDialog.build<Player>(this@ChoosePlayersActivity) {
            title = { ContextCompat.getString(this@ChoosePlayersActivity, R.string.editPlayer) }
            acceptLabel = ContextCompat.getString(this@ChoosePlayersActivity, R.string.confirm)
            lifecycleOwner = this@ChoosePlayersActivity
            isValid = { valid }
            onConfirmation = {
                viewModel.addPlayer(it)
            }
        }
        ShowValueSelectionDialog(
            bindingSupplier = bindingSupplier,
            dialog = dialog,
            extraction = { Player(
                name = it.editName.text.toString(),
                icon = it.picture.drawToBitmap()
            ) }
        )
    }

    private val choosePlayerCreationMethod by lazy {
        ShowSelectOptionDialog(this) {
            title = { ContextCompat.getString(this@ChoosePlayersActivity, R.string.dialog_choose_new_player) }
            entry {
                title = ContextCompat.getString(this@ChoosePlayersActivity, R.string.new_player)
                onClick = {
                    createNewPlayerDialog.show()
                }
            }
            entry {
                title = ContextCompat.getString(this@ChoosePlayersActivity, R.string.contact)
                onClick = {
                    if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this@ChoosePlayersActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                            this@ChoosePlayersActivity,
                            arrayOf<String>(Manifest.permission.READ_CONTACTS),
                            TAGHelper.REQUEST_READ_CONTACT_CODE
                        )
                    }
                    else if (ContextCompat.checkSelfPermission(this@ChoosePlayersActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//                        addPlayerFromContacts()
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_players)

        useCamera = PFAPermission.Camera.declareUsage(this) {
            onDenied = { Log.d("Camera", "denied") }
            showRationale = {
                rationaleTitle = "Feature: Custom Image"
                rationaleText = "This is needed"
            }
            onGranted = {
                lifecycleScope.launch {
                    val cameraProvider =
                        ProcessCameraProvider.getInstance(this@ChoosePlayersActivity).await()
                    val imageCapture = ImageCapture.Builder()
                        .setTargetRotation(display!!.rotation)
                        .build()
                    cameraProvider.bindToLifecycle(
                        this@ChoosePlayersActivity,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        imageCapture
                    )

                    imageCapture.takePicture(
                        Executors.newSingleThreadExecutor(),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onError(exception: ImageCaptureException) {
                                Log.d("Camera", "error: $exception")
                            }
                            override fun onCaptureSuccess(image: ImageProxy) {
                                pictureConsumer(image.image!!)
                            }
                        })
                }
            }
        }

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

private fun cutSquareBitmap(b: Bitmap): Bitmap {
    val bHeight = b.getHeight()
    val bWidth = b.getWidth()
    var longEdge = bHeight
    var shortEdge = bWidth

    if (bWidth > bHeight) {
        longEdge = bWidth
        shortEdge = bHeight
    }

    val diff = longEdge - shortEdge

    if (bWidth <= bHeight) return Bitmap.createBitmap(b, 0, diff / 2, shortEdge, shortEdge)
    else return Bitmap.createBitmap(b, diff / 2, 0, shortEdge, shortEdge)
}