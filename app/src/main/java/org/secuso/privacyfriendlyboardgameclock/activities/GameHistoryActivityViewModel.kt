package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class GameHistoryActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllGames() = repository.gameDao().allGames()

}