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
import java.util.Random

class ChoosePlayersActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)

    fun getAllPlayers() = repository.playerDao().allPlayers()
    fun getAllPlayersSync() = repository.playerDao().allPlayersSync()

    fun createGame(gameId: Long, players: List<Player>): Game {
        val game = repository.gameDao().getUnfinishedGame(gameId)!!
        game.apply {
            //start player index
            when (gameMode) {
                TAGHelper.CLOCKWISE, TAGHelper.MANUAL_SEQUENCE, TAGHelper.TIME_TRACKING -> {
                    startPlayerIndex = 0
                    nextPlayerIndex = 1
                }
                TAGHelper.COUNTER_CLOCKWISE -> {
                    startPlayerIndex = 0
                    nextPlayerIndex = players.size - 1
                }
                TAGHelper.RANDOM -> {
                    startPlayerIndex = 0

                    var randomPlayerIndex = Random().nextInt(players.size)
                    while (randomPlayerIndex == startPlayerIndex) {
                        randomPlayerIndex = Random().nextInt(players.size)
                    }
                    nextPlayerIndex = randomPlayerIndex
                }
            }
        }

        addPlayersToGame(game, players)

        // if game is finally created and game time is infinite, set game time to -1
        if (game.gameTimeInfinite == 1) {
            game.gameTime = 0
            game.currentGameTime = TAGHelper.DEFAULT_VALUE_LONG
        }

        repository.gameDao().updateGame(game)
        return game
    }

    private fun addPlayersToGame(game: Game, players: List<Player>) {
        viewModelScope.launch {
            players.map { PlayerGameData(game.id, it.id, 1, game.roundTime) }.apply {
                    repository.gameDao().addPlayersToGame(this@apply)
            }
        }
    }


    fun addPlayer(player: Player) = repository.playerDao().addPlayer(player.name, player.icon)

}