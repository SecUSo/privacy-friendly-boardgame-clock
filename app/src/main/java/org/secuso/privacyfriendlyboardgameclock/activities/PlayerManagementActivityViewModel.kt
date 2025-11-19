package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase

class PlayerManagementActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllPlayers() = repository.playerDao().allPlayers()
    fun getAllPlayersSync() = repository.playerDao().allPlayersSync()

    fun addPlayer(player: Player) = repository.playerDao().addPlayer(player.name, player.icon)
    fun addPlayer(name: String, icon: Bitmap) = repository.playerDao().addPlayer(name, icon)

    fun updatePlayer(player: Player) = repository.playerDao().updatePlayer(player)

    fun removePlayer(player: Player) = repository.playerDao().deletePlayer(player)
}