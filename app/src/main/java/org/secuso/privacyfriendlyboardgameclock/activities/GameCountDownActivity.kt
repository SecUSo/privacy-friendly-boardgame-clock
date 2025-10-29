/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.activities

import android.app.ActivityManager
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.fragments.GameResultDialogFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.OnSwipeTouchListener
import org.secuso.privacyfriendlyboardgameclock.helpers.SelectPlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.model.Game
import org.secuso.privacyfriendlyboardgameclock.model.Player
import org.secuso.privacyfriendlyboardgameclock.services.CountdownTimerService
import java.util.Random

/**
 * Created by Quang Anh Dang on 03.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the actual Game Countdown Mode
 */
class GameCountDownActivity : BaseActivity() {
    private var br: BroadcastReceiver? = null
    private val gds by lazy { GamesDataSourceSingleton.getInstance(this) }
    var game: Game? = null
        private set
    private var gameTime: Long = 0
    private var playerRoundTimes: HashMap<Long?, Long?>? = null

    // which round number will the player be in the next time he has turn
    private var playerRounds: HashMap<Long?, Long?>? = null
    private var players: MutableList<Player>? = null

    private var currentPlayer: Player? = null

    private val playPauseButton: Button by lazy { findViewById(R.id.gamePlayPauseButton) }
    private val nextPlayerButton: Button by lazy { findViewById(R.id.nextPlayerButton) }
    private val finishGameButton: ImageButton by lazy { findViewById(R.id.finishGameButton) }
    private val saveGameButton: ImageButton by lazy { findViewById(R.id.saveGameButton) }

    private var currentRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var nextPlayerIndex = 0
    private var currentPlayerIndex = 0

    private var roundTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG
    private var gameTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG

    private val currentPlayerTv: TextView by lazy { findViewById(R.id.game_current_player_name) }
    private val currentPlayerRound: TextView by lazy { findViewById(R.id.game_current_player_round) }
    private val currentPlayerIcon: ImageView by lazy { findViewById(R.id.imageViewIcon) }

    private val gameTimerTv: TextView by lazy { findViewById(R.id.game_timer) }
    private val roundTimerTv: TextView by lazy { findViewById(R.id.round_timer) }

    private var alreadySaved = true
    private var isFinished = 0
    private var isLastRound = 0
    private var isPaused = true

    private var playersQueue: MutableList<Player> = mutableListOf()

    private var mBoundService: CountdownTimerService? = null
    private var mIsBound = false

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = (service as CountdownTimerService.LocalBinder).service
            prepareAll()
            Log.i("GameCountDownActivity", "Service Connected.")
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null
            Log.i("GameCountDownActivity", "Service Disconnected.")
        }
    }

    fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(
            Intent(
                this@GameCountDownActivity,
                CountdownTimerService::class.java
            ), mConnection, BIND_AUTO_CREATE
        )
        mIsBound = true
    }

    fun doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection)
            mIsBound = false
        }
    }

    var saveGame: View.OnClickListener = View.OnClickListener {
        playPauseButton!!.setOnClickListener(pause)
        playPauseButton!!.performClick()
        AlertDialog.Builder(this@GameCountDownActivity)
            .setTitle(R.string.saveGame)
            .setMessage(R.string.sureToSaveGameQuestion)
            .setIcon(android.R.drawable.ic_menu_help)
            .setPositiveButton(R.string.yes) { dialog, whichButton ->
                saveGameToDb(1)
                alreadySaved = true
                Toast.makeText(
                    this@GameCountDownActivity,
                    R.string.gameSavedSuccess,
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    private val showGameResults: View.OnClickListener = View.OnClickListener {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        val prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT)
        if (prev != null) ft.remove(prev)
        ft.addToBackStack(null)

        // Create and show the dialog
        val showGameInfo = GameResultDialogFragment()
        showGameInfo.show(ft, TAGHelper.DIALOG_FRAGMENT)
    }

    private val run: View.OnClickListener = View.OnClickListener {
        alreadySaved = false

        saveGameButton!!.visibility = View.GONE
        finishGameButton!!.visibility = View.GONE

        isPaused = false

        updateAndResumeTimer()

        playPauseButton!!.setText(R.string.pause_capslock)
        playPauseButton!!.setOnClickListener(pause)
    }

    private val pause: View.OnClickListener = View.OnClickListener {
        isPaused = true

        mBoundService!!.pauseTimer()
        gameTime = currentGameTimeMs

        saveGameButton!!.visibility = View.VISIBLE
        finishGameButton!!.visibility = View.VISIBLE

        playPauseButton!!.setText(R.string.resume)
        playPauseButton!!.setOnClickListener(run)

        updateTimerTextViews()
    }

    private val pauseFinishedGame: View.OnClickListener = View.OnClickListener {
        isPaused = true

        mBoundService!!.pauseTimer()
        gameTime = currentGameTimeMs

        playPauseButton!!.setText(R.string.resume)
        playPauseButton!!.setOnClickListener(continueFinishedGame)

        updateTimerTextViews()
    }

    private val continueFinishedGame: View.OnClickListener = View.OnClickListener {
        alreadySaved = false

        saveGameButton!!.visibility = View.GONE
        finishGameButton!!.visibility = View.GONE

        isPaused = false

        updateAndResumeTimer()

        playPauseButton!!.setText(R.string.pause_capslock)
        playPauseButton!!.setOnClickListener(pauseFinishedGame)
    }

    private val nextPlayer: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View?) {
            currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG
            // save current player data
            val currPlayerId = currentPlayer!!.id
            var nextPlayerRound = playerRounds!!.get(currPlayerId)!! + 1

            // just put the time current player has left in to the list
            playerRoundTimes!![currPlayerId] = currentRoundTimeMs
            // update the round number for current player
            playerRounds!![currPlayerId] = nextPlayerRound

            // in case of chess mode and the time is not reseted each round
            if (game!!.chess_mode == 1 && isFinished == 0 && game!!.reset_round_time == 0) {
                var isAllRoundTimeZero = true
                for (playerID in playerRoundTimes!!.keys) {
                    isAllRoundTimeZero =
                        isAllRoundTimeZero && (playerRoundTimes!!.get(playerID) == 0L)
                }
                if (isAllRoundTimeZero) {
                    finishGame()
                    return
                }
            }

            if (game!!.isLastRound == 1 && getPlayersNotInRound(nextPlayerRound).isEmpty()) {
                nextPlayerRound -= 1
                playerRounds!![currPlayerId] = nextPlayerRound
                finishGame()
                return
            } else if (!isPaused) {
                if (game!!.chess_mode == 1) mBoundService!!.pauseTimer()
                else  // update view by clicking pause button
                    playPauseButton!!.performClick()

                gameTime = currentGameTimeMs
            }


            // determine next player
            if (game!!.game_mode == 0) {
                nextPlayerIndex = (currentPlayerIndex + 1) % players!!.size
            } else if (game!!.game_mode == 1) {
                if (currentPlayerIndex == 0) nextPlayerIndex = players!!.size - 1
                else nextPlayerIndex = currentPlayerIndex - 1
            } else if (game!!.game_mode == 2) {
                playersQueue = getPlayersNotInRound(nextPlayerRound)
                playersQueue!!.remove(currentPlayer)

                if (playersQueue!!.isEmpty()) for (p in players!!) playersQueue!!.add(p)

                playersQueue!!.remove(currentPlayer)

                val r = Random().nextInt(playersQueue!!.size)

                nextPlayerIndex = players!!.indexOf(playersQueue!![r]!!)
            } else if (game!!.game_mode == 3) {
                if (getPlayersNotInRound(nextPlayerRound).isEmpty()) {
                    // hide player
                    currentPlayerTv!!.visibility = View.INVISIBLE
                    currentPlayerRound!!.visibility = View.INVISIBLE
                    currentPlayerIcon!!.visibility = View.INVISIBLE
                    roundTimerTv!!.visibility = View.INVISIBLE

                    val selectedPlayers = ArrayList<Player>()

                    val builder = AlertDialog.Builder(this@GameCountDownActivity)
                    val dialogView =
                        layoutInflater.inflate(R.layout.dialog_set_player_sequence, null)
                    builder.setView(dialogView)
                    builder.setTitle(R.string.manualChoiceHeading)
                    builder.setPositiveButton(R.string.confirm, null)
                    builder.setCancelable(false)

                    val myListView =
                        dialogView.findViewById<View?>(R.id.set_player_sequence_list) as ListView
                    val listAdapter = SelectPlayerListAdapter(
                        this@GameCountDownActivity,
                        R.id.set_player_sequence_list,
                        players
                    )
                    myListView.adapter = listAdapter
                    myListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                    myListView.onItemClickListener =
                        AdapterView.OnItemClickListener { adapter, v, position, id ->
                            val tv = v.findViewById<View?>(R.id.textViewNumber) as TextView
                            if (tv.text === "" && myListView.checkedItemCount > 0) {
                                selectedPlayers.add((adapter.getItemAtPosition(position) as Player?)!!)
                                tv.text = (selectedPlayers.indexOf(
                                    adapter.getItemAtPosition(
                                        position
                                    ) as Player?
                                ) + 1).toString() + "."
                            } else {
                                val deletedNumber =
                                    selectedPlayers.indexOf(adapter.getItemAtPosition(position)) + 1
                                selectedPlayers.remove(adapter.getItemAtPosition(position))
                                tv.text = ""

                                val playersList = v.parent as ListView
                                val checked = playersList.checkedItemPositions
                                val size = checked.size()
                                for (i in 0..<size) {
                                    val key = checked.keyAt(i)
                                    val checkedValue = checked.get(key)
                                    if (checkedValue) {
                                        val number =
                                            playersList.getChildAt(key).findViewById<View?>(
                                                R.id.textViewNumber
                                            ) as TextView
                                        val numberText = number.text.toString()
                                        val indexDot = numberText.indexOf(".")
                                        if (indexDot != -1) {
                                            var value = numberText.take(indexDot).toInt()
                                            if (value > deletedNumber) {
                                                value--
                                                number.text = "$value."
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    val ad = builder.show()
                    val positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        if (players!!.size != selectedPlayers.size) {
                            Toast.makeText(this@GameCountDownActivity, R.string.manualChoiceError, Toast.LENGTH_SHORT).show()
                        } else {
                            players = selectedPlayers
                            game!!.players = players

                            // unhide player
                            currentPlayerTv!!.visibility = View.VISIBLE
                            currentPlayerRound!!.visibility = View.VISIBLE
                            currentPlayerIcon!!.visibility = View.VISIBLE
                            roundTimerTv!!.visibility = View.VISIBLE

                            nextPlayerIndex = 0
                            currentPlayerIndex = nextPlayerIndex
                            currentPlayer = players!![nextPlayerIndex]
                            restorePlayerData(currentPlayer!!.id)
                            updateViews()

                            if (!isPaused && game!!.chess_mode == 1) {
                                updateAndResumeTimer()
                            }

                            updateGame()

                            ad.dismiss()
                        }
                    }
                } else {
                    nextPlayerIndex = (currentPlayerIndex + 1) % players!!.size

                    // set next player to current player
                    currentPlayerIndex = nextPlayerIndex
                    currentPlayer = players!![nextPlayerIndex]
                    restorePlayerData(currentPlayer!!.id)
                    updateViews()

                    if (!isPaused && game!!.chess_mode == 1) {
                        updateAndResumeTimer()
                    }

                    updateGame()
                }
            }

            if (game!!.game_mode != 3) {
                // set next player to current player
                currentPlayerIndex = nextPlayerIndex
                currentPlayer = players!![nextPlayerIndex]
                restorePlayerData(currentPlayer!!.id)
                updateViews()

                if (!isPaused && game!!.chess_mode == 1) {
                    updateAndResumeTimer()
                }

                updateGame()
            }
        }
    }
    private val finishGame: View.OnClickListener = View.OnClickListener {
        playPauseButton!!.setOnClickListener(pause)
        playPauseButton!!.performClick()
        AlertDialog.Builder(this@GameCountDownActivity)
            .setTitle(R.string.finishGame)
            .setMessage(R.string.finishGameQuestion)
            .setIcon(android.R.drawable.ic_menu_help)
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, whichButton -> finishGame() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    private val wantToFinish: View.OnClickListener = View.OnClickListener {
        // Option not reset Round Time, each player has only certain amount of time
        // if one player runs out of time --> ask if game ends or continue with other
        // remaining players
        if (getPlayersNotInRound(playerRounds!!.get(currentPlayer!!.id)!! + 1).size - 1 > 0) {
            playPauseButton!!.setOnClickListener(pause)
            playPauseButton!!.performClick()
            AlertDialog.Builder(this@GameCountDownActivity)
                .setTitle(R.string.roundTimeOverDialogHeading)
                .setMessage(R.string.roundTimeOverDialogQuestion)
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(
                    R.string.finishGame
                ) { dialog, whichButton ->
                    isFinished = 1
                    finishGame()
                }
                .setNegativeButton(R.string.resume
                ) { dialog, whichButton ->
                    //isLastRound = 1;
                    //game.setIsLastRound(1);
                    playPauseButton!!.setOnClickListener(run)
                    nextPlayerButton!!.setOnClickListener(nextPlayer)
                    //nextPlayerButton.performClick();
                }
                .show()
        } else finishGame()
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if (checkIfSingletonDataIsCorrupt()) return

        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                updateGUI(intent)
            }
        }

        // prevent phone from sleeping while game is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_game_countdown)

        // show swipe dialog
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val firstStart = settings.getBoolean("showSwipeDialog", true)
        if (firstStart) {
            AlertDialog.Builder(this)
                .setTitle(R.string.swipeDialogQuestion)
                .setMessage(R.string.swipeDialogAnswer)
                .setIcon(android.R.drawable.ic_menu_info_details)
                .setPositiveButton(R.string.ok, null)
                .show()

            val editor = settings.edit()
            editor.putBoolean("showSwipeDialog", false)
            editor.commit()
        }

        val saveGameButton = findViewById<ImageButton>(R.id.saveGameButton)
        saveGameButton.setOnClickListener(saveGame)
        val finishGameButton = findViewById<ImageButton>(R.id.finishGameButton)
        finishGameButton.setOnClickListener(finishGame)

        // If service already exists
        if (isMyServiceRunning(CountdownTimerService::class.java)) {
            game = null
        } else {
            // get game from SingleTon Class, if null, show MainMenu
            if (gds!!.game != null) {
                game = gds!!.game
            } else showMainMenu()
        }
        startTimerService()
    }

    /**
     * before starting, prepare everything incl. views based on
     * if the service already exists and running or this is a
     * complete new game
     */
    private fun prepareAll() {
        // if the service already exists
        val isNewGame = game != null
        if (game == null) game = mBoundService!!.game


        players = game!!.players
        playerRoundTimes = game!!.player_round_times
        playerRounds = game!!.player_rounds
        currentPlayerIndex = game!!.startPlayerIndex
        nextPlayerIndex = game!!.nextPlayerIndex
        currentPlayer = players!![game!!.startPlayerIndex]
        playersQueue = getPlayersNotInRound(playerRounds!![currentPlayer!!.id]!!)
        currentPlayerTv!!.text = currentPlayer!!.name
        currentPlayerRound!!.text = playerRounds!!.get(currentPlayer!!.id).toString()
        currentPlayerIcon!!.setImageBitmap(currentPlayer!!.icon)
        currentRoundTimeMs = playerRoundTimes!!.get(currentPlayer!!.id)!!
        currentGameTimeMs = game!!.currentGameTime
        roundTimeOriTenPercent = (currentRoundTimeMs * 0.1).toLong()
        gameTimeOriTenPercent = (currentGameTimeMs * 0.1).toLong()
        gameTime = currentGameTimeMs
        isLastRound = game!!.isLastRound
        if (game!!.game_time_infinite == 1) {
            gameTimerTv!!.text = getString(R.string.infinite)
        }
        updateTimerTextViews()
        nextPlayerButton!!.setOnClickListener(nextPlayer)
        findViewById<View>(R.id.main_content).setOnTouchListener(object :
            OnSwipeTouchListener(baseContext) {
            override fun onSwipeLeft() {
                nextPlayerButton!!.callOnClick()
            }

            override fun onSwipeRight() {
                nextPlayerButton!!.callOnClick()
            }
        })

        if (isNewGame) {
            playPauseButton!!.setOnClickListener {
                alreadySaved = false
                isPaused = false
                mBoundService!!.showNotification()
                mBoundService!!.initRoundCountdownTimer(currentRoundTimeMs)
                // if game time not infinit, init game timer
                if (game!!.game_time_infinite == 0) {
                    mBoundService!!.initGameCountdownTimer(currentGameTimeMs)
                    mBoundService!!.startGameTimer()
                }
                mBoundService!!.startRoundTimer()

                playPauseButton!!.setText(R.string.pause_capslock)
                playPauseButton!!.setOnClickListener(pause)
                nextPlayerButton!!.visibility = View.VISIBLE
            }
            mBoundService!!.game = game
        } else {
            alreadySaved = game!!.saved == 1
            isPaused = mBoundService!!.isPaused
            if (isPaused) {
                playPauseButton!!.setText(R.string.resume)
                playPauseButton!!.setOnClickListener(run)
            } else {
                playPauseButton!!.setText(R.string.pause_capslock)
                playPauseButton!!.setOnClickListener(pause)
            }
            nextPlayerButton!!.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(br, IntentFilter(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG))
        Log.i("GameCountDownActivity", "Registered Broadcast Receiver.")
    }

    public override fun onPause() {
        super.onPause()
        unregisterRegister()
    }

    public override fun onStop() {
        unregisterRegister()
        super.onStop()
    }

    public override fun onDestroy() {
        unregisterRegister()
        doUnbindService()
        super.onDestroy()
    }


    private fun restorePlayerData(currPlayerId: Long) {
        // restore player data
        if (game!!.reset_round_time == 1) {
            currentRoundTimeMs = game!!.round_time

            if ((game!!.round_time_delta != -1L) && (playerRounds!!.get(currPlayerId)!! > 1)) currentRoundTimeMs += game!!.round_time_delta * (playerRounds!!.get(
                currPlayerId
            )!! - 1)
        } else {
            currentRoundTimeMs = playerRoundTimes!!.get(currPlayerId)!!

            if ((game!!.round_time_delta != -1L) && (playerRounds!!.get(currPlayerId)!! > 1)) currentRoundTimeMs += game!!.round_time_delta
        }
    }

    private fun updateViews() {
        updateTimer()

        // update view
        currentPlayerTv!!.text = currentPlayer!!.name
        currentPlayerRound!!.text = playerRounds!!.get(currentPlayer!!.id).toString()
        currentPlayerIcon!!.setImageBitmap(currentPlayer!!.icon)
        updateTimerTextViews()
    }

    private fun updateTimerTextViews() {
        val roundTimeToUse: Long
        val gameTimeToUse: Long
        var round_time_result = ""
        var game_time_result = ""

        if (currentExceedRoundTimeMs >= 0) {
            roundTimeToUse = currentExceedRoundTimeMs
            round_time_result = "-"
        } else roundTimeToUse = currentRoundTimeMs

        if (currentExceedGameTimeMs >= 0) {
            gameTimeToUse = currentExceedGameTimeMs
            game_time_result = "-"
        } else gameTimeToUse = currentGameTimeMs

        val round_time_hh = getTimeStrings(roundTimeToUse)[0]
        val round_time_mm = getTimeStrings(roundTimeToUse)[1]
        val round_time_ss = getTimeStrings(roundTimeToUse)[2]
        val round_time_ms = getTimeStrings(roundTimeToUse)[3]
        round_time_result =
            "$round_time_result$round_time_hh:$round_time_mm:$round_time_ss'$round_time_ms"
        roundTimerTv!!.text = round_time_result

        // highlight low timers red colored
        if (currentRoundTimeMs <= roundTimeOriTenPercent) roundTimerTv!!.setTextColor(Color.RED)
        else roundTimerTv!!.setTextColor(Color.BLACK)

        // if game time is not infinite
        if (game!!.game_time_infinite == 0) {
            val game_time_hh = getTimeStrings(gameTimeToUse)[0]
            val game_time_mm = getTimeStrings(gameTimeToUse)[1]
            val game_time_ss = getTimeStrings(gameTimeToUse)[2]
            game_time_result =
                "$game_time_result$game_time_hh:$game_time_mm:$game_time_ss"
            gameTimerTv!!.text = game_time_result

            if (game!!.game_time_infinite == 0 && currentGameTimeMs <= gameTimeOriTenPercent) gameTimerTv!!.setTextColor(
                Color.RED
            )
            else gameTimerTv!!.setTextColor(Color.BLACK)
        }
    }

    private fun getPlayersNotInRound(round: Long): MutableList<Player> {
        val retPlayers: MutableList<Player> = ArrayList<Player>()

        for (i in players!!.indices) if (playerRounds!!.get(
                players!![i].id
            ) != round
        ) retPlayers.add(players!![i])

        return retPlayers
    }

    /**
     * update the current game object
     * called by saveGameToDb, by nextPlayer button,
     * (3)
     */
    fun updateGame() {
        playerRoundTimes!![currentPlayer!!.id] = currentRoundTimeMs
        playerRounds!![currentPlayer!!.id] = playerRounds!!.get(currentPlayer!!.id)

        game!!.player_round_times = playerRoundTimes
        game!!.player_rounds = playerRounds
        game!!.nextPlayerIndex = nextPlayerIndex
        game!!.startPlayerIndex = currentPlayerIndex
        if (game!!.game_time_infinite == 0) game!!.currentGameTime = currentGameTimeMs
        game!!.finished = isFinished
        game!!.isLastRound = isLastRound
    }

    /**
     * save Game to DB is called when game is finished or save button is clicked
     * is called by finishGame, or when click saveGame Button
     * (2)
     * @param save 0 if game is finished, 1 if game is still on going
     */
    private fun saveGameToDb(save: Int) {
        updateGame()
        game!!.saved = save
        gds!!.saveGame(game)
    }

    /**
     * game is finished, set finish flag, update buttons and textview
     * and call save game to DB
     * (1)
     */
    private fun finishGame() {
        isFinished = 1
        gds!!.game = game
        game!!.finished = isFinished
        updateGame()
        stopTimerService()

        saveGameButton!!.visibility = View.GONE
        finishGameButton!!.visibility = View.GONE
        nextPlayerButton!!.visibility = View.GONE
        playPauseButton!!.setText(R.string.showResults)
        playPauseButton!!.setOnClickListener(showGameResults)
        playPauseButton!!.performClick()

        saveGameToDb(0)
    }

    /**
     *
     * @param serviceClass name of the service class you want to check
     * @return true if the service is running
     */
    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.Companion.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun showMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    public override fun onBackPressed() {
        if (!isPaused && (isFinished == 0)) playPauseButton!!.performClick()

        if (isFinished == 1) {
            showMainMenu()
        } else {
            playPauseButton!!.setOnClickListener(pause)
            playPauseButton!!.performClick()
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.quitGame))
                .setMessage(getString(R.string.leaveGameQuestion))
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(
                    getString(R.string.saveGame)
                ) { dialog, whichButton ->
                    if (!alreadySaved) {
                        saveGameToDb(1)
                        stopTimerService()
                        showMainMenu()
                    } else showMainMenu()
                }
                .setNeutralButton(R.string.withoutSave
                ) { dialogInterface, i ->
                    stopTimerService()
                    showMainMenu()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    /**
     *
     * @param time_ms time in milliseconds
     * @return a String Array list of 4 elements, hour, minutes, seconds and milliseconds
     */
    private fun getTimeStrings(time_ms: Long): Array<String> {
        val h = (time_ms / 3600000).toInt()
        val m = (time_ms - h * 3600000).toInt() / 60000
        val s = (time_ms - h * 3600000 - m * 60000).toInt() / 1000

        var ms = "0"
        try {
            ms = time_ms.toString().get(time_ms.toString().length - 3).toString()
        } catch (e: StringIndexOutOfBoundsException) {
        }
        val hh = if (h < 10) "0$h" else h.toString() + ""
        val mm = if (m < 10) "0$m" else m.toString() + ""
        val ss = if (s < 10) "0$s" else s.toString() + ""

        return arrayOf<String>(hh, mm, ss, ms)
    }

    /**
     * @return true if service has already been running and started
     */
    private fun startTimerService() {
        startService(Intent(this, CountdownTimerService::class.java))
        doBindService()
    }

    private fun stopTimerService() {
        unregisterRegister()
        doUnbindService()
        stopService(Intent(this, CountdownTimerService::class.java))
    }

    private fun unregisterRegister() {
        try {
            unregisterReceiver(br)
        } catch (e: Exception) {
        }
        Log.i("GameCountDownActivity", "Unregistered Broadcast Receiver.")
    }

    private fun updateAndResumeTimer() {
        mBoundService!!.currentRoundTimeMs = currentRoundTimeMs
        if (game!!.game_time_infinite == 0) {
            mBoundService!!.currentGameTimeMs = currentGameTimeMs
        }
        mBoundService!!.resumeTimer()
    }

    private fun updateTimer() {
        mBoundService!!.currentRoundTimeMs = currentRoundTimeMs
        if (game!!.game_time_infinite == 0) {
            mBoundService!!.currentGameTimeMs = currentGameTimeMs
        }
    }

    /**
     * receive Broadcast Information, interpret this and update GUI and every variable based on information received
     * @param intent
     */
    private fun updateGUI(intent: Intent) {
        var gameMsTillFinished: Long
        var gameMsExceeded: Long
        var roundMsTillFinished: Long
        var roundMsExceeded: Long
        var gameFinishedSignal = false
        var roundFinishedSignal = false
        if (intent.extras != null) {
            // retrieved all possible value
            gameMsTillFinished =
                intent.getLongExtra(TAGHelper.GAME_COUNT_DOWN_TAG, TAGHelper.DEFAULT_VALUE_LONG)
            gameMsExceeded = intent.getLongExtra(
                TAGHelper.GAME_COUNT_IN_NEGATIVE_TAG,
                TAGHelper.DEFAULT_VALUE_LONG
            )
            gameFinishedSignal = intent.getBooleanExtra(TAGHelper.GAME_FINISHED_SIGNAL, false)
            roundMsTillFinished =
                intent.getLongExtra(TAGHelper.ROUND_COUNT_DOWN_TAG, TAGHelper.DEFAULT_VALUE_LONG)
            roundMsExceeded = intent.getLongExtra(
                TAGHelper.ROUND_COUNT_IN_NEGATIVE_TAG,
                TAGHelper.DEFAULT_VALUE_LONG
            )
            roundFinishedSignal = intent.getBooleanExtra(TAGHelper.ROUND_FINISHED_SIGNAL, false)

            // handle game timer
            // apparently the 2 signals don't always match => combine signal from intent and signal from mBoundService.getBroadcastIntent()
            // for only one time signal
            if (gameFinishedSignal && mBoundService!!.broadcastIntent
                    .getBooleanExtra(TAGHelper.GAME_FINISHED_SIGNAL, false)
            ) {
                // Remove finish signal after reading
                mBoundService!!.broadcastIntent.removeExtra(TAGHelper.GAME_FINISHED_SIGNAL)
                currentGameTimeMs = 0
                if (game!!.chess_mode == 1) {
                    finishGame()
                }
                /*isFinished = 1;*/
            }

            // handle round timer
            // apparently the 2 signals don't always match => combine signal from intent and signal from mBoundService.getBroadcastIntent()
            // for only one time signal
            if (roundFinishedSignal && mBoundService!!.broadcastIntent
                    .getBooleanExtra(TAGHelper.ROUND_FINISHED_SIGNAL, false)
            ) {
                // Remove finish signal after reading
                mBoundService!!.broadcastIntent.removeExtra(TAGHelper.ROUND_FINISHED_SIGNAL)
                intent.removeExtra(TAGHelper.ROUND_FINISHED_SIGNAL)
                currentRoundTimeMs = 0
                if (game!!.chess_mode == 1) {
                    if (isFinished == 0 && game!!.reset_round_time == 0) {
                        mBoundService!!.pauseTimer()
                        nextPlayerButton!!.performClick()
                        // TODO if chess mode => last player then automatically finish
                        //wantToFinish.onClick(findViewById(R.id.main_content));
                    } else nextPlayerButton!!.performClick()
                }
            }

            if (gameMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG) {
                currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG
                currentGameTimeMs = gameMsTillFinished
            } else if (gameMsExceeded != TAGHelper.DEFAULT_VALUE_LONG) {
                currentExceedGameTimeMs = gameMsExceeded
            }

            if (roundMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG) {
                currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG
                currentRoundTimeMs = roundMsTillFinished
            } else if (roundMsExceeded != TAGHelper.DEFAULT_VALUE_LONG) {
                currentExceedRoundTimeMs = roundMsExceeded
            }
            gameTime = currentGameTimeMs
            updateGame() //do we need to call this every tick?
            mBoundService!!.game = game
            updateTimerTextViews()
        }
    }

    /**
     * before starting to work, check if Database and Singleton Object (use to save some objects
     * and transferring objects between activity), if any attribute is null --> move to main activity
     * and remove all other activities --> start new
     */
    fun checkIfSingletonDataIsCorrupt(): Boolean {
        if (!(GamesDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull()
                    && PlayersDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull())
        ) {
            val intent = Intent(this, MainActivity::class.java)
            // clear all other activities
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
            return true
        } else return false
    }
}
