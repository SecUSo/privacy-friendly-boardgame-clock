package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.model.dialog.InfoDialog
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.helpers.TimeTrackingPlayerAdapter

/**
 * Created by Quang Anh Dang on 26.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the activity for the Game Time Tracking Mode
 */
class GameTimeTrackingModeActivity : BaseActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this@GameTimeTrackingModeActivity.viewModelStore,
            factory = GameViewModel.Factory,
            defaultCreationExtras = MutableCreationExtras().apply {
                set(APPLICATION_KEY, application)
                set(GameViewModel.GAME_ID_KEY, this@GameTimeTrackingModeActivity.intent.extras!!.getLong(GameViewModel.EXTRA_GAME_ID))
            }
        )[GameViewModel::class.java]
    }
    private lateinit var timeTrackingAdapter: TimeTrackingPlayerAdapter
    private var alreadySaved = false

    // Views and Buttons Definitions
    private val currentGameTimeTV: TextView by lazy {  findViewById(R.id.game_timer) }
    private val saveGameButton: ImageButton by lazy {  findViewById(R.id.saveGameButton) }
    private val playPauseButton: Button by lazy {  findViewById(R.id.gamePlayPauseButton) }
    private val finishGameButton: ImageButton by lazy {  findViewById(R.id.finishGameButton) }

    private val showGameResultsDialog by lazy {
        buildGameResultDialog(viewModel)
    }

    val saveGameAndQuitDialog by lazy {
        buildSaveGameAndQuitDialog(
            viewModel,
            { playPauseButton.text = ContextCompat.getString(this,  R.string.resume) },
            {
                if (!alreadySaved) {
                    saveGameToDb(1)
                }
            }
        )
    }

    val saveGameDialog by lazy {
        AbortElseDialog.build(this) {
            title = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.saveGame) }
            content = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.sureToSaveGameQuestion) }
            icon = R.drawable.ic_menu_help
            onShow = { viewModel.pauseTimer() }

            onElse = {
                saveGameToDb(1)
                alreadySaved = true
                Toast.makeText(
                    this@GameTimeTrackingModeActivity,
                    R.string.gameSavedSuccess,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resumeTimer()
            }
            onAbort = {
                viewModel.resumeTimer()
            }
            handleDismiss = true

        }
    }

    private val finishGameDialog by lazy {
        AbortElseDialog.build(this) {
            title = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.finishGame) }
            content = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.finishGameQuestion) }
            icon = android.R.drawable.ic_menu_help
            acceptLabel = ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.yes)
            abortLabel = ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.no)

            onElse = { finishGame() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_time_tracking_mode)

        // prevent phone from sleeping while game is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        viewModel.prepareGame()
        viewModel.initTimeTrackingGame()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.tick.collect {
                    updateGUI()
                    viewModel.players.forEachIndexed { index, _ -> timeTrackingAdapter.notifyItemChanged(index) }
                }
            }
        }

        saveGameButton.setOnClickListener {
            saveGameDialog.show()
        }
        var resumedTimers = false
        playPauseButton.setOnClickListener {
            if (viewModel.activeTimers.isNotEmpty()) {
                resumedTimers = true
                playPauseButton.setText(R.string.resume)
                viewModel.pauseAllTimers()
                playPauseButton.background = ContextCompat.getDrawable(
                    this@GameTimeTrackingModeActivity,
                    R.drawable.button_fullwidth
                )
            } else {
                if (!resumedTimers) {
                    Toast.makeText(
                        this@GameTimeTrackingModeActivity,
                        getResources().getString(R.string.alreadyPaused),
                        Toast.LENGTH_SHORT
                    ).show()
                    playPauseButton.background = ContextCompat.getDrawable(
                        this@GameTimeTrackingModeActivity,
                        R.drawable.button_disabled
                    )
                } else {
                    playPauseButton.setText(R.string.pause_capslock)
                    viewModel.resumeAllTimers()
                    resumedTimers = false
                }
            }
        }
        playPauseButton.visibility = View.VISIBLE
        finishGameButton.setOnClickListener {
            finishGameDialog.show()
        }
        timeTrackingAdapter = TimeTrackingPlayerAdapter(
            layoutInflater,
            viewModel.players,
            {
                viewModel.toggleTimer(it)
                viewModel.stopAllPausedTimers()
                if (viewModel.activeTimers.isNotEmpty()) {
                    playPauseButton.setText(R.string.pause_capslock)
                } else {
                    playPauseButton.setText(R.string.resume)
                }
            },
            { id -> viewModel.elapsedTime(viewModel.players.withIndex().find { it.value.id == id}!!.index) }
        )

        findViewById<RecyclerView>(R.id.player_list).apply {
            setHasFixedSize(true)
            adapter = timeTrackingAdapter
            layoutManager = LinearLayoutManager(this@GameTimeTrackingModeActivity)
            itemAnimator = null
        }

        playPauseButton.setText(R.string.pause_capslock)
        if (viewModel.activeTimers.isNotEmpty()) {
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_fullwidth
            )
        } else {
            playPauseButton.background = ContextCompat.getDrawable(
                this@GameTimeTrackingModeActivity,
                R.drawable.button_disabled
            )
        }
    }

    /**
     * save Game to DB is called when game is finished or save button is clicked
     * is called by finishGame, or when click saveGame Button
     * (2)
     * @param save 0 if game is finished, 1 if game is still on going
     */
    private fun saveGameToDb(save: Int) {
        viewModel.game.saved = save
        viewModel.saveGame()
    }

    /**
     * game is finished, set finish flag, update buttons and textview
     * and call save game to DB
     * (1)
     */
    private fun finishGame() {
        viewModel.game.finished = 1

        saveGameButton.visibility = View.GONE
        finishGameButton.visibility = View.GONE
        playPauseButton.setText(R.string.showResults)
        playPauseButton.setOnClickListener {
            showGameResultsDialog.show()
        }

        saveGameToDb(0)
        playPauseButton.performClick()
    }

    private fun updateGUI() {
        val gameTimeInString = GameViewModel.getTimeComponentStrings(viewModel.remainingGameTime)
        val game_time_hh = gameTimeInString[0]
        val game_time_mm = gameTimeInString[1]
        val game_time_ss = gameTimeInString[2]
        currentGameTimeTV.text = "$game_time_hh:$game_time_mm:$game_time_ss"
    }

    @Deprecated("Deprecated in Java")
    public override fun onBackPressed() {
        viewModel.pauseTimer()
        if (viewModel.game.finished == 1) {
            super.onBackPressed()
        } else {
            saveGameAndQuitDialog.show()
        }
    }

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
        when (item.itemId) {
            android.R.id.home -> saveGameAndQuitDialog.show()
            R.id.action_info -> InfoDialog.build(this) {
                title = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.instruction) }
                content = { ContextCompat.getString(this@GameTimeTrackingModeActivity, R.string.instruction_details) }
            }.show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
}