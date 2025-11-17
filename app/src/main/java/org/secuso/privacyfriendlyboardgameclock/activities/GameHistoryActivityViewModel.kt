package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase
import org.secuso.privacyfriendlyboardgameclock.room.model.Game

class GameHistoryActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllGames() = repository.gameDao().allGames()
    fun getPlayers(ids: List<Long>) = repository.playerDao().getPlayers(ids)

    fun deleteGames(games: List<Game>) = repository.gameDao().deleteGames(games)

}