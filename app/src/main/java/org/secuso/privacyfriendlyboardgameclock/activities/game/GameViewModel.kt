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
    val currentPlayerData
        get() = game.players[currentIndex]
    val currentPlayer
        get() = players.getOrNull(currentIndex) ?: throw java.lang.IllegalStateException("Set game first")
    var isNewGame = false

    private var timers: List<ITimer> = listOf()
    private var currentIndex: Int = 0

    var selectPlayers: SelectNewPlayerOrder? = null
    var manualSequence: MutableList<Int> = mutableListOf()

    private var _tick = MutableSharedFlow<Unit>()
    val tick = _tick.asSharedFlow()

    private var ticker: Job? = null

    private var gameTimer: ITimer = CountdownTimer(0) {}
    val remainingGameTime
        get() = gameTimer.currentElapsedTime

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
            isNewGame = true
        }
    }

    fun initTimeTrackingGame() {
        timers = game.players.map { player -> Timer(player.roundTimes) }
    }
    fun initCountdownGame(onFinish: (Int, PlayerGameData) -> Unit) {
        timers = game.players.mapIndexed { index, player -> CountdownTimer(player.roundTimes) { onFinish(index, player) } }
    }

    /**
     * prepares the view-model for a new game.
     * Also start the tick-flow.
     */
    fun prepareGame() {
        currentIndex = game.startPlayerIndex
        gameTimer = CountdownTimer(game.gameTime) {}

        ticker = viewModelScope.launch {
            while (game.finished == 0) {
                _tick.emit(Unit)
                delay(50)
            }
        }
    }
    fun stopTick() = ticker?.cancel()
    fun stopTimer(index: Int = currentIndex) {
        timers[index].stop()
        if (!timers.any { it.isRunning }) {
            gameTimer.stop()
        }
    }
    fun startTimer(index: Int = currentIndex) {
        gameTimer.start()
        timers[index].start()
    }
    fun pauseTimer(index: Int = currentIndex) = timers[index].pause()
    fun resumeTimer(index: Int = currentIndex) = timers[index].resume()
    fun isTimerRunning(index: Int = currentIndex) = timers[index].isRunning

    fun elapsedTime(index: Int = currentIndex) = timers[index].currentElapsedTime
    fun toggleTimer(index: Int = currentIndex) {
        if (isTimerRunning(index)) {
            timers[index].stop()
        } else {
            timers[index].start()
        }
    }
    fun endPlayerRound() {
        stopTimer(currentIndex)
        game.players[currentIndex].apply {
            roundTimes = timers[currentIndex].measuredTime
            rounds += 1
        }
        viewModelScope.launch {
            currentIndex = nextPlayerIndex()
            if (game.chessMode == 1) {
                startTimer(currentIndex)
            }
        }
    }
    fun finishGame() {
        for (index in 0..players.size) {
            if (timers[index].isRunning) {
                timers[index].stop()
                game.players[index].apply {
                    roundTimes = timers[index].measuredTime
                }
            }
        }
    }

    suspend fun nextPlayerIndex(): Int {
        return when(game.gameMode) {
            TAGHelper.CLOCKWISE -> (currentIndex + 1) % players.size
            TAGHelper.COUNTER_CLOCKWISE -> (currentIndex - 1) % players.size
            TAGHelper.RANDOM -> game.players.maxOf { it.rounds }.let { maxRound ->
                game.players.filter { it.rounds < maxRound }.mapIndexed { index, _ -> index }.random()
            }
            TAGHelper.MANUAL_SEQUENCE -> {
                    if (manualSequence.isEmpty()) {
                        val defer = CompletableDeferred<List<Int>>()
                        (selectPlayers!!).select(game.players.mapIndexed { index, player -> index to player }, defer)
                        manualSequence = defer.await().toMutableList()
                    }
                    val next = manualSequence.first()
                    manualSequence.remove(0)
                    next
            }
            else -> (currentIndex + 1) % players.size
        }
    }

    fun getAllSavedGames() = repository.gameDao().allSavedGames()

    fun interface SelectNewPlayerOrder {
        fun select(players: List<Pair<Int, PlayerGameData>>, defer: CompletableDeferred<List<Int>>): Unit
    }
}