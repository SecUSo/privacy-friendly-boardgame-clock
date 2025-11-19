package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.Image
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.work.await
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.dialog.ValueSelectionDialog
import org.secuso.pfacore.model.permission.PFAPermission
import org.secuso.pfacore.model.permission.PFAPermissionOwner
import org.secuso.pfacore.ui.declareUsage
import org.secuso.pfacore.ui.dialog.ShowCustomInfoDialog
import org.secuso.pfacore.ui.dialog.ShowSelectOptionDialog
import org.secuso.pfacore.ui.dialog.ShowValueSelectionDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity
import org.secuso.privacyfriendlyboardgameclock.databinding.FragmentGameResultsBinding
import org.secuso.privacyfriendlyboardgameclock.databinding.FragmentPlayerManagementNewplayerBinding
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerResultsListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import java.util.concurrent.Executors

fun AppCompatActivity.buildGameResultDialog(viewModel: GameViewModel): ShowCustomInfoDialog<FragmentGameResultsBinding> {
    val bindingSupplier = {
        FragmentGameResultsBinding.inflate(layoutInflater).apply {
            timePlayedText.text = viewModel.remainingGameTimeString
            roundsPlayedText.text = viewModel.game.isLastRound.toString()
            val gameModes = resources.getStringArray(R.array.game_modes)
            gameModeText.text = when (viewModel.game.gameMode) {
                TAGHelper.CLOCKWISE -> gameModes[TAGHelper.CLOCKWISE]
                TAGHelper.COUNTER_CLOCKWISE -> gameModes[TAGHelper.COUNTER_CLOCKWISE]
                TAGHelper.RANDOM -> gameModes[TAGHelper.RANDOM]
                TAGHelper.MANUAL_SEQUENCE -> gameModes[TAGHelper.MANUAL_SEQUENCE]
                TAGHelper.TIME_TRACKING -> gameModes[TAGHelper.TIME_TRACKING]
                else -> null
            }

            val data = viewModel.players.map { it to viewModel.game.players.find { data -> data.playerId == it.id }!! }
            list.adapter = PlayerResultsListAdapter(this@buildGameResultDialog, R.id.list, data)
        }
    }
    return ShowCustomInfoDialog(this, bindingSupplier) {
        title = {
            viewModel.game.game.name ?: ContextCompat.getString(
                this@buildGameResultDialog,
                R.string.gameResults
            )
        }
        acceptLabel = ContextCompat.getString(context, R.string.backToMainMenu)
        onClose = {
            Intent(this@buildGameResultDialog, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                this@buildGameResultDialog.startActivity(this)
            }
        }
    }
}

fun AppCompatActivity.buildSaveGameAndQuitDialog(viewModel: GameViewModel, _onShow: () -> Unit, save: () -> Unit): ShowSelectOptionDialog {
    return ShowSelectOptionDialog(
        this,
        ContextCompat.getString(this, R.string.leaveGameQuestion)
    ) {
        title = { ContextCompat.getString(this@buildSaveGameAndQuitDialog, R.string.quitGame) }
        icon = R.drawable.ic_menu_help
        onShow = {
            viewModel.pauseTimer()
            _onShow()
        }

        entry {
            title = ContextCompat.getString(this@buildSaveGameAndQuitDialog, R.string.saveGame)
            onClick = {
                save()
                Intent(this@buildSaveGameAndQuitDialog, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(this)
                }
            }
        }
        entry {
            title = ContextCompat.getString(this@buildSaveGameAndQuitDialog, R.string.withoutSave)
            onClick = {
                Intent(this@buildSaveGameAndQuitDialog, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(this)
                }
            }
        }
    }
}

fun <A> A.useCameraForPlayerPicture(
    pictureConsumer: (Image) -> Unit
) where A: AppCompatActivity, A: PFAPermissionOwner
        = PFAPermission.Camera.declareUsage(this) {
    onDenied = { Log.d("Camera", "denied") }
    showRationale = {
        rationaleTitle = "Feature: Custom Image"
        rationaleText = "This is needed"
    }
    onGranted = {
        lifecycleScope.launch {
            val cameraProvider =
                ProcessCameraProvider.getInstance(this@useCameraForPlayerPicture).await()
            val imageCapture = ImageCapture.Builder()
                .setTargetRotation(display!!.rotation)
                .build()
            cameraProvider.bindToLifecycle(
                this@useCameraForPlayerPicture,
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

private fun AppCompatActivity.createColorSelectionDialog(onColorChosen: (Int) -> Unit) = ColorPickerDialogBuilder
    .with(this@createColorSelectionDialog)
    .setTitle("Choose color")
    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
    .density(12)
    .setOnColorSelectedListener { }
    .setPositiveButton("OK") { dialog, selectedColor, allColors -> onColorChosen(selectedColor)}
    .setNegativeButton("Cancel", null)
    .build()

fun AppCompatActivity.buildCreateNewPlayerDialog(
    onPlayerCreated: (Player) -> Unit,
    useCamera: ((Bitmap) -> Unit) -> Unit,
    playerSupplier: (() -> Player)? = null
): ShowValueSelectionDialog<Player, FragmentPlayerManagementNewplayerBinding> {
    val valid = MutableLiveData(false)

    val bindingSupplier: () -> FragmentPlayerManagementNewplayerBinding = {
        val binding = FragmentPlayerManagementNewplayerBinding.inflate(layoutInflater)
        var icon: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_android)

        if (playerSupplier != null) {
            val player = playerSupplier()
            binding.editName.setText(player.name)
            if (player.icon != null) {
                binding.picture.setImageBitmap(player.icon)
            }
        }
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

        binding.picture.setOnClickListener {
            useCamera {
                runOnUiThread {
                    val icon = cutSquareBitmap(it).scale(
                        binding.picture.width,
                        binding.picture.height,
                        false
                    )
                    binding.picture.setImageBitmap(icon)
                    binding.picture.colorFilter = null
                }
            }
        }
        binding
    }

    val dialog = ValueSelectionDialog.build<Player>(this) {
        title = { ContextCompat.getString(this@buildCreateNewPlayerDialog, R.string.editPlayer) }
        acceptLabel = ContextCompat.getString(this@buildCreateNewPlayerDialog, R.string.confirm)
        lifecycleOwner = this@buildCreateNewPlayerDialog
        isValid = { valid }
        onConfirmation = {
            onPlayerCreated(it)
        }
    }
    return ShowValueSelectionDialog(
        bindingSupplier = bindingSupplier,
        dialog = dialog,
        extraction = { Player(
            name = it.editName.text.toString(),
            icon = it.picture.drawToBitmap()
        ) }
    )
}

fun AppCompatActivity.buildChooseNewPlayerCreationMethodDialog(
    createNewPlayerDialog: ShowValueSelectionDialog<Player, FragmentPlayerManagementNewplayerBinding>
) = ShowSelectOptionDialog(this) {
        title = { ContextCompat.getString(this@buildChooseNewPlayerCreationMethodDialog, R.string.dialog_choose_new_player) }
        entry {
            title = ContextCompat.getString(this@buildChooseNewPlayerCreationMethodDialog, R.string.new_player)
            onClick = {
                createNewPlayerDialog.show()
            }
        }
        entry {
            title = ContextCompat.getString(this@buildChooseNewPlayerCreationMethodDialog, R.string.contact)
            onClick = {
                if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this@buildChooseNewPlayerCreationMethodDialog, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this@buildChooseNewPlayerCreationMethodDialog,
                        arrayOf<String>(Manifest.permission.READ_CONTACTS),
                        TAGHelper.REQUEST_READ_CONTACT_CODE
                    )
                }
                else if (ContextCompat.checkSelfPermission(this@buildChooseNewPlayerCreationMethodDialog, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//                        addPlayerFromContacts()
                }
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