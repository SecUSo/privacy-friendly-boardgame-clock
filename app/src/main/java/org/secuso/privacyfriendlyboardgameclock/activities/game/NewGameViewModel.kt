package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlyboardgameclock.helpers.CountdownTimer
import org.secuso.privacyfriendlyboardgameclock.helpers.ITimer
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.helpers.Timer
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase
import org.secuso.privacyfriendlyboardgameclock.room.model.Game
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData

class NewGameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getLastGame() = repository.gameDao().getLastGame().firstOrNull()

    fun addNewGame(game: Game) = repository.gameDao().addGame(game)
}