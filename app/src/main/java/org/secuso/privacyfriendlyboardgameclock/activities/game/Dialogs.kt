package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.secuso.pfacore.ui.dialog.ShowCustomInfoDialog
import org.secuso.pfacore.ui.dialog.ShowSelectOptionDialog
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity
import org.secuso.privacyfriendlyboardgameclock.databinding.FragmentGameResultsBinding
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerResultsListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper

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
            list.adapter = PlayerResultsListAdapter(this@buildGameResultDialog, R.id.list, viewModel.players.zip(viewModel.game.players))
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