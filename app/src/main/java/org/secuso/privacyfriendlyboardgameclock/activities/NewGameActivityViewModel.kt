package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase
import org.secuso.privacyfriendlyboardgameclock.room.model.Game

class NewGameActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getLastGame() = repository.gameDao().getLastGame()

    fun addGame(game: Game) = repository.gameDao().addGame(game)

}