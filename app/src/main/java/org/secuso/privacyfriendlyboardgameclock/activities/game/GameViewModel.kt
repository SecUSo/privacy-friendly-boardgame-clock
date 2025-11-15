package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
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
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData

class GameViewModel(application: Application, gameId: Long) : AndroidViewModel(application) {

    private val repository = BoardGameClockDatabase.getInstance(application)
    val game = repository.gameDao().getGame(gameId)!!
    var players: List<Player> = repository.playerDao().getPlayers(game.players.map { it.playerId }.toList())
    val currentPlayerData
        get() = game.players[currentIndex]
    val currentPlayer
        get() = players.getOrNull(currentIndex) ?: throw java.lang.IllegalStateException("Set game first")
    var isNewGame = false

    private var timers: MutableList<ITimer> = mutableListOf()
    private var currentIndex: Int = 0

    var selectPlayers: SelectNewPlayerOrder? = null
    var manualSequence: MutableList<Int> = mutableListOf()

    private var _tick = MutableSharedFlow<Unit>()
    val tick = _tick.asSharedFlow()
    
    private var ticker: Job? = null

    private var gameTimer: ITimer = CountdownTimer(0) {}
    val remainingGameTime
        get() = gameTimer.currentElapsedTime

    val remainingGameTimeString: String
        get() {
            if (game.gameTimeInfinite == 1) {
                return getApplication<Application>().getString(org.secuso.privacyfriendlyboardgameclock.R.string.infinite)
            }

            val time = TimeComponents(remainingGameTime)
            val (h, m, s) = time.toStringComponents()

            return when {
                time.hour == 0 && time.minute == 0 -> "${s}s"
                time.hour == 0 -> "${m}m ${s}s"
                else -> "${h}h ${m}m ${s}s"
            }
        }

    val activeTimers
        get() = timers.withIndex().filter { it.value.isRunning }

    fun saveGame() {
        game.saved = 1
        game.currentGameTime = gameTimer.currentElapsedTime
        game.nextPlayerIndex = currentIndex
        if (game.gameMode == TAGHelper.MANUAL_SEQUENCE) {
            game.customPlayerOrder = manualSequence
        }
        repository.gameDao().updateGame(game.game)
        game.players = game.players.mapIndexed { index, player ->
            player.roundTimes = timers[index].currentElapsedTime
            player
        }
        repository.gameDao().updatePlayerData(game.players)
    }

    fun initTimeTrackingGame() {
        timers = game.players.map { player -> Timer(player.roundTimes) }.toMutableList()
    }
    fun initCountdownGame(onFinish: (Int, PlayerGameData) -> Unit) {
        timers = game.players.mapIndexed { index, player ->
            CountdownTimer(player.roundTimes) {
                onFinish(index, player)
            }
        }.toMutableList()
    }

    /**
     * prepares the view-model for a new game.
     * Also start the tick-flow.
     */
    fun prepareGame() {
        if (game.gameTimeInfinite > 0 || game.gameMode == TAGHelper.TIME_TRACKING) {
            gameTimer = Timer(game.currentGameTime)
        } else {
            gameTimer = CountdownTimer(if (game.currentGameTime == 0L) { game.gameTime } else { game.currentGameTime }) {}
        }

        if (game.gameMode == TAGHelper.MANUAL_SEQUENCE) {
            val min = game.players.minOf { it.rounds }
            manualSequence = game.customPlayerOrder?.filter { game.players[it].rounds == min }?.toMutableList()
                ?: (0 until game.players.size).map{ (it + game.startPlayerIndex) % game.players.size  }.toMutableList()
            currentIndex = manualSequence.first()
            manualSequence.removeAt(0)
        } else {
            currentIndex = game.startPlayerIndex

        }

        startTick()
    }
    fun startTick() {
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

    fun pauseAllTimers() {
        (0 until players.size).forEach { pauseTimer(it) }
        gameTimer.pause()
    }
    fun resumeAllTimers() {
        (0 until players.size).forEach { resumeTimer(it) }
        gameTimer.resume()
    }

    fun stopAllPausedTimers() {
        (0 until players.size).forEach { if (!isTimerRunning(it)) { stopTimer(it) } }
    }

    fun elapsedTime(index: Int = currentIndex) = timers[index].currentElapsedTime
    fun toggleTimer(index: Int = currentIndex) {
        if (isTimerRunning(index)) {
            stopTimer(index)
        } else {
            startTimer(index)
        }
    }
    fun endPlayerRound() {
        stopTimer(currentIndex)
        game.players[currentIndex].apply {
            roundTimes = if (game.resetRoundTime > 0 && game.roundTimeDelta > 0) {
                game.roundTime + rounds * game.roundTimeDelta
            } else if (game.resetRoundTime > 0) {
                game.roundTime
            } else if (game.roundTimeDelta > 0) {
                timers[currentIndex].currentElapsedTime + game.roundTimeDelta
            } else {
                timers[currentIndex].currentElapsedTime
            }
            rounds += 1
        }
        timers[currentIndex] = timers[currentIndex].newInitial(game.players[currentIndex].roundTimes)
        viewModelScope.launch {
            currentIndex = nextPlayerIndex()
            if (game.chessMode == 1) {
                startTimer(currentIndex)
            }
        }
    }
    fun finishGame() {
        for (index in 0 until players.size) {
            if (timers[index].isRunning) {
                timers[index].stop()
                game.players[index].apply {
                    roundTimes = timers[index].measuredTime
                }
            }
        }
    }

    fun isGameFinished(): Boolean {
        return remainingGameTime < 0 || timers.filter { timer ->  timer.currentElapsedTime > 0 }.size <= 1
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
                    manualSequence.removeAt(0)
                    next
            }
            else -> (currentIndex + 1) % players.size
        }
    }

    fun excludePlayer(index: Int = currentIndex) {
        stopTimer(index)
        players = players.filterIndexed { i, _ -> i != index }
        game.players = game.players.filterIndexed { i, _ -> i != index }
        timers = timers.filterIndexed { i, _ -> i != index }.toMutableList()
        stopTick()
        viewModelScope.launch {
            currentIndex = nextPlayerIndex()
            if (game.chessMode == 1) {
                startTimer(currentIndex)
            }
            startTick()
        }
    }

    data class TimeComponents(
        val hour: Int,
        val minute: Int,
        val seconds: Int,
        val millis: Int
    ) {
        constructor(millis: Long): this(
            (millis / 3600000).toInt(),
            ((millis / 60000) % 60000).toInt(),
            ((millis / 1000) % 60).toInt(),
            (millis % 1000).toInt()
        )

        fun toStringComponents(): Array<String> {
            val ms = millis.toString()
            val hh = if (hour < 10) "0$hour" else hour.toString() + ""
            val mm = if (minute < 10) "0$minute" else minute.toString() + ""
            val ss = if (seconds < 10) "0$seconds" else seconds.toString() + ""
            return arrayOf(hh, mm, ss, ms)
        }
        override fun toString() = toStringComponents().let {
            val h = it[0]
            val m = it[1]
            val s = it[2]
            val ms = it[3]
            "${h}h:${m}m:${s}.${m}s"
        }
    }

    fun interface SelectNewPlayerOrder {
        fun select(players: List<Pair<Int, PlayerGameData>>, defer: CompletableDeferred<List<Int>>): Unit
    }

    companion object {
        const val EXTRA_GAME_ID = "game_id"
        val GAME_ID_KEY: CreationExtras.Key<Long> = object : CreationExtras.Key<Long> {}

        fun getTimeComponentStrings(millis: Long) = TimeComponents(millis).toStringComponents()
        fun getTimeComponent(millis: Long) = TimeComponents(millis)
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GameViewModel(application = checkNotNull(this[APPLICATION_KEY]), checkNotNull(this[GAME_ID_KEY]))
            }
        }
    }
}