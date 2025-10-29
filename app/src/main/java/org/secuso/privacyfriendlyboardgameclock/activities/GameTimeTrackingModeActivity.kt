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
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.fragments.GameResultDialogFragment
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.helpers.TimeTrackingPlayerAdapter
import org.secuso.privacyfriendlyboardgameclock.model.Game
import org.secuso.privacyfriendlyboardgameclock.model.Player
import org.secuso.privacyfriendlyboardgameclock.services.TimeTrackingService

/**
 * Created by Quang Anh Dang on 26.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the activity for the Game Time Tracking Mode
 */
class GameTimeTrackingModeActivity : BaseActivity(), ItemClickListener {
    private val pds by lazy { PlayersDataSourceSingleton.getInstance(this) }
    private val gds by lazy { GamesDataSourceSingleton.getInstance(this) }
    var game: Game? = null
        private set
    private var players: MutableList<Player> = mutableListOf()
    private var playersTime: java.util.HashMap<Long, Long> = hashMapOf()
    private val playerIDs: MutableList<Long> = ArrayList<Long>()
    private var currentGameTimeMs: Long = 0
    private var br: BroadcastReceiver? = null
    private lateinit var timeTrackingAdapter: TimeTrackingPlayerAdapter
    private var isFinished = 0
    private var alreadySaved = false

    // Views and Buttons Definitions
    private val currentGameTimeTV: TextView by lazy {  findViewById(R.id.game_current_game_time) }
    private val saveGameButton: ImageButton by lazy {  findViewById(R.id.saveGameButton) }
    private val playPauseButton: Button by lazy {  findViewById(R.id.gamePlayPauseButton) }
    private val finishGameButton: Button by lazy {  findViewById(R.id.finishGameButton) }

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

    private val saveGame: View.OnClickListener = View.OnClickListener {
        pauseTimeTrackers()
        AlertDialog.Builder(this@GameTimeTrackingModeActivity)
            .setTitle(R.string.saveGame)
            .setMessage(R.string.sureToSaveGameQuestion)
            .setIcon(android.R.drawable.ic_menu_help)
            .setPositiveButton(R.string.yes) { dialog, whichButton ->
                saveGameToDb(1)
                alreadySaved = true
                Toast.makeText(
                    this@GameTimeTrackingModeActivity,
                    R.string.gameSavedSuccess,
                    Toast.LENGTH_LONG
                ).show()
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }
    

    /**
     * TODO OLD maybe removed later if not needed
     */
    private val pauseAllActiveTrackers: View.OnClickListener = View.OnClickListener {
        pauseTimeTrackers()

        saveGameButton.visibility = View.VISIBLE
        finishGameButton.visibility = View.VISIBLE

        playPauseButton.setOnClickListener { Toast.makeText(this@GameTimeTrackingModeActivity, getResources().getString(R.string.alreadyPaused), Toast.LENGTH_SHORT).show() }
        playPauseButton.background = ContextCompat.getDrawable(
            this@GameTimeTrackingModeActivity,
            R.drawable.button_disabled
        )
    }

    private val pauseAll: View.OnClickListener = View.OnClickListener {
        pauseTimeTrackers()
        playPauseButton.setText(R.string.resume)
        playPauseButton.setOnClickListener {
            Toast.makeText(
                this@GameTimeTrackingModeActivity,
                getResources().getString(R.string.alreadyPaused),
                Toast.LENGTH_SHORT
            ).show()
        }
        playPauseButton.setBackground(
            ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_disabled
            )
        )
    }

    private val finishGame: View.OnClickListener = View.OnClickListener {
        pauseTimeTrackers()
        AlertDialog.Builder(this@GameTimeTrackingModeActivity)
            .setTitle(R.string.finishGame)
            .setMessage(R.string.finishGameQuestion)
            .setIcon(android.R.drawable.ic_menu_help)
            .setPositiveButton(
                getString(R.string.yes)
            ) { dialog, whichButton -> finishGame() }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }


    private var mBoundService: TimeTrackingService? = null
    private var mIsBound = false

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = (service as TimeTrackingService.LocalBinder).service
            prepareAll()
            // Init only once
            if (!mBoundService!!.isAllTrackerInit) {
                mBoundService!!.initAllTracker(playersTime, currentGameTimeMs)
            }
            Log.i("GameTimeTrackingModeAct", "Service Connected.")
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null
            Log.i("GameTimeTrackingModeAct", "Service Disconnected.")
        }
    }

    fun doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(
            Intent(
                this@GameTimeTrackingModeActivity,
                TimeTrackingService::class.java
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

    fun checkIfSingletonDataIsCorrupt(): Boolean {
        if (!(GamesDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull()
                    && PlayersDataSourceSingleton.getInstance(this).checkIfAllVariableNotNull())
        ) {
            val intent = Intent(this, MainActivity::class.java)
            // clear all other activities
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
            return true
        } else return false
    }

    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.Companion.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun showMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if (checkIfSingletonDataIsCorrupt()) return

        setContentView(R.layout.activity_time_tracking_mode)

        // prevent phone from sleeping while game is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // register broadcast receiver
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                updateGUI(intent)
            }
        }

        // If service already exists
        if (isMyServiceRunning(TimeTrackingService::class.java)) {
            game = null
        } else {
            // get game from SingleTon Class, if null, show MainMenu
            if (gds.game != null) {
                game = gds.game
            } else showMainMenu()
        }

        // start time tracking service
        startTimeTrackerService()
    }

    /**
     * before starting, prepare everything incl. views based on
     * if the service already exists and running or this is a
     * complete new game
     */
    private fun prepareAll() {
        // if the service already exists
        if (game == null) game = mBoundService!!.game
        
        // populate data
        players = game!!.players
        for (p in players) playerIDs.add(p.id)
        playersTime = game!!.player_round_times
        currentGameTimeMs = game!!.currentGameTime
        updateGameTimerTextview()
        saveGameButton.setOnClickListener(saveGame)
        playPauseButton.setText(R.string.pause_capslock)
        playPauseButton.setOnClickListener(pauseAll)
        playPauseButton.visibility = View.VISIBLE
        finishGameButton.setOnClickListener(finishGame)
        timeTrackingAdapter = TimeTrackingPlayerAdapter(this, players, this)

        findViewById<RecyclerView>(R.id.player_list).apply {
            setHasFixedSize(true)
            adapter = timeTrackingAdapter
            layoutManager = LinearLayoutManager(this@GameTimeTrackingModeActivity)
            itemAnimator = null
        }
        
        val activePlayers = mBoundService!!.activePlayersList
        if (activePlayers.isNotEmpty()) {
            playPauseButton.setText(R.string.pause_capslock)
            playPauseButton.setOnClickListener(pauseAllActiveTrackers)
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_fullwidth
            )
        } else {
            playPauseButton.setText(R.string.pause_capslock)
            playPauseButton.setOnClickListener {
                Toast.makeText(
                    this@GameTimeTrackingModeActivity,
                    getResources().getString(R.string.alreadyPaused),
                    Toast.LENGTH_SHORT
                ).show()
            }
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_disabled
            )
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(br, IntentFilter(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG))
        Log.i("GameTimeTrackingModeAct", "Registered Broadcast Receiver.")
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

    /**
     * save Game to DB is called when game is finished or save button is clicked
     * is called by finishGame, or when click saveGame Button
     * (2)
     * @param save 0 if game is finished, 1 if game is still on going
     */
    private fun saveGameToDb(save: Int) {
        updateGame()
        game?.saved = save
        gds.saveGame(game)
    }

    /**
     * game is finished, set finish flag, update buttons and textview
     * and call save game to DB
     * (1)
     */
    private fun finishGame() {
        isFinished = 1
        gds.game = game
        game?.finished = isFinished
        updateGame()
        stopTimeTrackerService()

        saveGameButton.visibility = View.GONE
        finishGameButton.visibility = View.GONE
        playPauseButton.setText(R.string.showResults)
        playPauseButton.setOnClickListener(showGameResults)

        saveGameToDb(0)
        playPauseButton.performClick()
    }

    /**
     * update the current game object
     * called by saveGameToDb, by nextPlayer button,
     * (3)
     */
    fun updateGame() {
        game?.player_round_times = playersTime
        game?.currentGameTime = currentGameTimeMs
        game?.finished = isFinished
    }
    

    /**
     * pause all the active time trackers
     */
    private fun pauseTimeTrackers() {
        val copyActivePlayersList = ArrayList<Long>(mBoundService!!.activePlayersList)
        for (playerID in copyActivePlayersList) {
            // save all to pausingList, if needed to save the state, set this to true
            mBoundService!!.pauseTimeTracker(playerID, false)
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
            ms = time_ms.toString()[time_ms.toString().length - 3].toString()
        } catch (e: StringIndexOutOfBoundsException) {
        }
        val hh = if (h < 10) "0$h" else h.toString() + ""
        val mm = if (m < 10) "0$m" else m.toString() + ""
        val ss = if (s < 10) "0$s" else s.toString() + ""

        return arrayOf<String>(hh, mm, ss, ms)
    }

    private fun updateGUI(intent: Intent) {
        if (intent.extras != null) {
            // UPDATE PLAYER TIME
            for (i in playerIDs.indices) {
                val currentPlayerID = playerIDs[i]
                val currentPlayerTime =
                    intent.getLongExtra(currentPlayerID.toString(), TAGHelper.DEFAULT_VALUE_LONG)
                if (currentPlayerTime != TAGHelper.DEFAULT_VALUE_LONG) {
                    playersTime[currentPlayerID] = currentPlayerTime
                    timeTrackingAdapter.notifyItemChanged(i)
                }
            }
            // UPDATE GAME TIME
            val currentGameTimeNew =
                intent.getLongExtra(TAGHelper.GAME_TIME_TRACKING, TAGHelper.DEFAULT_VALUE_LONG)
            if (currentGameTimeNew != TAGHelper.DEFAULT_VALUE_LONG) {
                currentGameTimeMs = currentGameTimeNew
                updateGameTimerTextview()
            }
        }
        updateGame()
        mBoundService?.game = game
    }

    private fun updateGameTimerTextview() {
        // UDPATE GAME TIME TRACKER
        val gameTimeInString = getTimeStrings(currentGameTimeMs)
        val game_time_hh = gameTimeInString[0]
        val game_time_mm = gameTimeInString[1]
        val game_time_ss = gameTimeInString[2]
        currentGameTimeTV.text = "$game_time_hh:$game_time_mm:$game_time_ss"
    }

    private fun startTimeTrackerService() {
        startService(Intent(this, TimeTrackingService::class.java))
        doBindService()
    }

    private fun stopTimeTrackerService() {
        unregisterRegister()
        doUnbindService()
        stopService(Intent(this, TimeTrackingService::class.java))
    }

    private fun unregisterRegister() {
        try {
            unregisterReceiver(br)
        } catch (e: Exception) {
        }
        Log.i("GameTimeTrackingModeAct", "Unregistered Broadcast Receiver.")
    }

    public override fun onBackPressed() {
        if (!mBoundService!!.isGamePaused) playPauseButton.performClick()

        if (isFinished == 1) {
            showMainMenu()
        } else {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.quitGame))
                .setMessage(getString(R.string.leaveGameQuestion))
                .setIcon(android.R.drawable.ic_menu_help)
                .setPositiveButton(
                    getString(R.string.saveGame)
                ) { dialog, whichButton ->
                    if (!alreadySaved) {
                        saveGameToDb(1)
                        stopTimeTrackerService()
                        showMainMenu()
                    } else showMainMenu()
                }
                .setNeutralButton(R.string.withoutSave
                ) { dialogInterface, i ->
                    stopTimeTrackerService()
                    showMainMenu()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    val playerTime: HashMap<Long, Long>
        get() = playersTime

    override fun onItemClick(view: View?, position: Int) {
        val activePlayers = mBoundService!!.activePlayersList
        val currentPlayer = timeTrackingAdapter.getPlayer(position)

        // if player already running, then pause this player
        if (activePlayers!!.contains(currentPlayer.id)) {
            mBoundService!!.pauseTimeTracker(currentPlayer.id, false)
        } else {
            mBoundService!!.resumeTimeTracker(currentPlayer.id)
        }
        
        if (activePlayers.isNotEmpty()) {
            playPauseButton.setText(R.string.pause_capslock)
            playPauseButton.setOnClickListener(pauseAllActiveTrackers)
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_fullwidth
            )
        } else {
            playPauseButton.setText(R.string.pause_capslock)
            playPauseButton.setOnClickListener {
                Toast.makeText(
                    this@GameTimeTrackingModeActivity,
                    getResources().getString(R.string.alreadyPaused),
                    Toast.LENGTH_SHORT
                ).show()
            }
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_disabled
            )
        }
    }

    override fun onItemLongClicked(view: View?, position: Int) = false

    /**
     * Infalte the Actionicons on Toolbar, in this case the plus icon
     * @param menu
     * @return
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.time_tracking_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_info) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.instruction_details)
                .setTitle(R.string.instruction)
                .setPositiveButton(R.string.okay, null)
                .show()
        }
        return true
    }
}
