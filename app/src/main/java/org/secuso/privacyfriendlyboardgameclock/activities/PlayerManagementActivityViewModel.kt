package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class PlayerManagementActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllPlayersSync() = repository.playerDao().allPlayersSync()

    fun addPlayer(player: Player) = repository.playerDao().addPlayer(player.name, player.icon)

    fun updatePlayer(player: Player) = repository.playerDao().updatePlayer(player)
}