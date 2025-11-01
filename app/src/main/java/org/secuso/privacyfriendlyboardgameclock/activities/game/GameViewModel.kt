package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.secuso.privacyfriendlyboardgameclock.room.BoardGameClockDatabase
import org.secuso.privacyfriendlyboardgameclock.room.model.Game
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    var newGame: Game? = null
        set(value) {
            field = value
            if (value == null) {
                return
            }
            viewModelScope.launch {
                repository.gameDao().apply {
                    if (getGame(value.id) == null) {
                      addGame(value)
                    }
                }
            }
        }
    private var _game: GameWithPlayer? = null

    var game: GameWithPlayer
        get() = _game ?: throw IllegalStateException("Set game first")
        set(value) {
            _game = value
        }

    var players: List<Player> = listOf()

    fun getLastGame() = repository.gameDao().getLastGame()

    fun addGame(game: Game) = repository.gameDao().addGame(game)
    fun saveGame() = repository.gameDao().updateGame(game)

    fun getAllPlayers() = repository.playerDao().allPlayers()

    fun addPlayersToGame(players: List<Player>) {
        players.map { PlayerGameData(newGame!!.id, it.id, 1, newGame!!.roundTime) }.apply {
            viewModelScope.launch {
                repository.gameDao().addPlayersToGame(this@apply)
            }
            game = GameWithPlayer(newGame!!, this)
            newGame = null
        }
    }

    fun startGame() {
        game = repository.gameDao().getGame(newGame!!.id)!!
    }

    fun getAllSavedGames() = repository.gameDao().allSavedGames()

}