package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun hasSavedGames() = repository.gameDao().allSavedGames().isEmpty()

}