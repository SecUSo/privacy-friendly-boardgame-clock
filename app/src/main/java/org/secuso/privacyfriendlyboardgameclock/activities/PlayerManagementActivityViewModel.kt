package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class PlayerManagementActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllPlayers() = repository.playerDao().allPlayers()

}