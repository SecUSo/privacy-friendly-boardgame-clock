package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.media.MediaPlayer
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import kotlinx.coroutines.launch
import org.secuso.pfacore.model.dialog.AbortElseDialog
import org.secuso.pfacore.model.dialog.InfoDialog
import org.secuso.pfacore.model.dialog.ValueSelectionDialog
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.pfacore.ui.dialog.ShowValueSelectionDialog
import org.secuso.pfacore.ui.dialog.show
import org.secuso.privacyfriendlyboardgameclock.PFApplicationData
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.databinding.DialogSetPlayerSequenceBinding
import org.secuso.privacyfriendlyboardgameclock.helpers.OnSwipeTouchListener
import org.secuso.privacyfriendlyboardgameclock.helpers.SelectPlayerListAdapter
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import kotlin.math.min

/**
 * Created by Quang Anh Dang on 03.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the actual Game Countdown Mode
 */
class GameCountDownActivity : BaseActivity() {
    private val viewModel by lazy {
        ViewModelProvider(
            this@GameCountDownActivity.viewModelStore,
            factory = GameViewModel.Factory,
            defaultCreationExtras = MutableCreationExtras().apply {
                set(APPLICATION_KEY, application)
                set(GameViewModel.GAME_ID_KEY, this@GameCountDownActivity.intent.extras!!.getLong(GameViewModel.EXTRA_GAME_ID))
            }
        )[GameViewModel::class.java]
    }

    val colorNormal by lazy {
        TypedValue().apply {
            theme.resolveAttribute(org.secuso.pfacore.R.attr.colorOnSurface, this, true)
        }.data
    }
    val colorOvertime by lazy {
        ContextCompat.getColor(this, R.color.red)
    }

    private var gameTime: Long = 0

    // which round number will the player be in the next time he has turn

    private val playPauseButton: Button by lazy { findViewById(R.id.gamePlayPauseButton) }
    private val nextPlayerButton: Button by lazy { findViewById(R.id.nextPlayerButton) }
    private val finishGameButton: ImageButton by lazy { findViewById(R.id.finishGameButton) }
    private val saveGameButton: ImageButton by lazy { findViewById(R.id.saveGameButton) }

    private var currentRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG
    private var currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG

    private var roundTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG
    private var gameTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG

    private val currentPlayerTv: TextView by lazy { findViewById(R.id.game_current_player_name) }
    private val currentPlayerRound: TextView by lazy { findViewById(R.id.game_current_player_round) }
    private val currentPlayerIcon: ImageView by lazy { findViewById(R.id.imageViewIcon) }

    private val gameTimerTv: TextView by lazy { findViewById(R.id.game_timer) }
    private val roundTimerTv: TextView by lazy { findViewById(R.id.round_timer) }

    private var alreadySaved = true

    private val playSoundOnRoundEnd by lazy { PFApplicationData.instance(this).playSoundOnRoundEnd.value }
    private val playSoundOnGameEnd by lazy { PFApplicationData.instance(this).playSoundOnGamedEnd.value }

    var mediaPlayerRoundEnd: MediaPlayer? = null
    var mediaPlayerGameEnd: MediaPlayer? = null

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
            title = { ContextCompat.getString(this@GameCountDownActivity, R.string.saveGame) }
            content = { ContextCompat.getString(this@GameCountDownActivity, R.string.sureToSaveGameQuestion) }
            icon = R.drawable.ic_menu_help
            onShow = { viewModel.pauseTimer() }

            onElse = {
                saveGameToDb(1)
                alreadySaved = true
                Toast.makeText(
                    this@GameCountDownActivity,
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

    private val showGameResultsDialog by lazy {
        buildGameResultDialog(viewModel)
    }

    private val finishGameDialog by lazy {
        AbortElseDialog.build(this) {
            title = { ContextCompat.getString(this@GameCountDownActivity, R.string.finishGame) }
            content = { ContextCompat.getString(this@GameCountDownActivity, R.string.finishGameQuestion) }
            icon = android.R.drawable.ic_menu_help
            acceptLabel = ContextCompat.getString(this@GameCountDownActivity, R.string.yes)
            abortLabel = ContextCompat.getString(this@GameCountDownActivity, R.string.no)

            onElse = { finishGame() }
            onShow = {
                mediaPlayerGameEnd?.start()
            }
        }
    }

    private val excludePlayerDialog by lazy {
        AbortElseDialog.build(this@GameCountDownActivity) {
            title = { ContextCompat.getString(this@GameCountDownActivity, R.string.roundTimeOverDialogHeading) }
            content = { ContextCompat.getString(this@GameCountDownActivity, R.string.roundTimeOverDialogQuestion) }
            icon = android.R.drawable.ic_menu_help
            acceptLabel = ContextCompat.getString(this@GameCountDownActivity, R.string.finishGame)
            abortLabel = ContextCompat.getString(this@GameCountDownActivity, R.string.resume)

            handleDismiss = true
            onShow = { viewModel.pauseTimer() }
            onAbort = { viewModel.excludePlayer() }
            onElse = {
                finishGame()
            }
        }
    }

    val selectPlayerOrder = GameViewModel.SelectNewPlayerOrder { players, defer ->
        val _isValid = MutableLiveData<Boolean>(false)
        val selectedPlayers = mutableListOf<Int>()
        val binding = {
            val binding = DialogSetPlayerSequenceBinding.inflate(layoutInflater)
            binding.setPlayerSequenceList.apply {
                adapter = SelectPlayerListAdapter(this@GameCountDownActivity, R.id.set_player_sequence_list, viewModel.players)
                choiceMode = ListView.CHOICE_MODE_MULTIPLE
                onItemClickListener = AdapterView.OnItemClickListener { adapter, v, position, id ->
                    val tv = v.findViewById<TextView>(R.id.textViewNumber)
                    if (tv.text.isEmpty() && checkedItemCount > 0) {
                        selectedPlayers.add(position)
                        tv.text = (selectedPlayers.indexOf(position) + 1).toString() + "."
                        _isValid.postValue(selectedPlayers.size == adapter.size)
                    } else {
                        // TODO: Improve this legacy code
                        _isValid.postValue(false)
                        val deletedNumber =
                            selectedPlayers.indexOf(position) + 1
                        selectedPlayers.remove(position)
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
            }
            binding
        }

        val dialog = ValueSelectionDialog.build<List<Int>>(this) {
            title = { ContextCompat.getString(this@GameCountDownActivity, R.string.manualChoiceHeading) }
            acceptLabel = ContextCompat.getString(this@GameCountDownActivity, R.string.confirm)
            onConfirmation = { defer.complete(it) }
            lifecycleOwner = this@GameCountDownActivity
            required = true
            isValid = { _isValid }
        }
        ShowValueSelectionDialog(binding, { selectedPlayers }, dialog).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // prevent phone from sleeping while game is running
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_game_countdown)

        lifecycleScope.launch {
            if (playSoundOnRoundEnd) {
                mediaPlayerRoundEnd = MediaPlayer.create(this@GameCountDownActivity, R.raw.roundend)
            }
            if (playSoundOnGameEnd) {
                mediaPlayerGameEnd = MediaPlayer.create(this@GameCountDownActivity, R.raw.gameend)
            }
        }

        // show swipe dialog
        val firstStart = PFApplicationData.instance(this).showSwipeDialog
        if (firstStart.value) {
            InfoDialog.build(this) {
                title = { ContextCompat.getString(this@GameCountDownActivity, R.string.swipeDialogQuestion) }
                content = { ContextCompat.getString(this@GameCountDownActivity, R.string.swipeDialogAnswer) }
                icon = android.R.drawable.ic_menu_info_details
            }.show()

            firstStart.value = false
        }

        viewModel.selectPlayers = selectPlayerOrder

        findViewById<ImageButton>(R.id.saveGameButton).setOnClickListener {
            saveGameDialog.show()
        }
        findViewById<ImageButton>(R.id.finishGameButton).setOnClickListener {
            finishGameDialog.show()
        }
        findViewById<Button>(R.id.nextPlayerButton).setOnClickListener {
            viewModel.endPlayerRound()
            mediaPlayerRoundEnd?.start()
        }
        findViewById<Button>(R.id.gamePlayPauseButton).setOnClickListener {
            saveGameButton.visibility = View.VISIBLE
            finishGameButton.visibility = View.VISIBLE
            nextPlayerButton.visibility = View.VISIBLE
            viewModel.toggleTimer()
            (it as Button).text = ContextCompat.getString(this, if (viewModel.isTimerRunning()) { R.string.pause_capslock } else { R.string.resume })
        }
        findViewById<View>(R.id.main_content).setOnTouchListener(object : OnSwipeTouchListener(baseContext) {
            override fun onSwipeLeft() {
                viewModel.endPlayerRound()
            }

            override fun onSwipeRight() {
                viewModel.endPlayerRound()
            }
        })

        if (!viewModel.isNewGame) {
            viewModel.initCountdownGame { _, player ->
                if (viewModel.players.size > 1) {
                    if (viewModel.game.chessMode == 1) {
                        viewModel.players.withIndex().find { it.value.id == player.playerId }?.apply {
                            viewModel.excludePlayer(index)
                        }
                    } else {
                        excludePlayerDialog.show()
                    }
                }
                if (viewModel.isGameFinished()) {
                    finishGame()
                }
            }
        } else {
            alreadySaved = viewModel.game.saved == 1
            nextPlayerButton.visibility = View.VISIBLE
        }

        if (viewModel.game.gameTimeInfinite == 1) {
            gameTimerTv.text = getString(R.string.infinite)
        }

        updateTimerTextViews()
        nextPlayerButton.setOnClickListener {
            viewModel.endPlayerRound()
        }

        viewModel.prepareGame()
        currentPlayerTv.text = viewModel.currentPlayer.name
        currentPlayerRound.text = viewModel.currentPlayerData.rounds.toString()
        currentPlayerIcon.setImageBitmap(viewModel.currentPlayer.icon)
        currentRoundTimeMs = viewModel.currentPlayerData.roundTimes
        currentGameTimeMs = viewModel.game.currentGameTime
        roundTimeOriTenPercent = (currentRoundTimeMs * 0.1).toLong()
        gameTimeOriTenPercent = (currentGameTimeMs * 0.1).toLong()
        gameTime = currentGameTimeMs

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.tick.collect {
                    updateGUI()
                    updateViews()
                }
            }
        }
    }

    private fun updateViews() {
        // update view
        currentPlayerTv.text = viewModel.currentPlayer.name
        currentPlayerRound.text = viewModel.currentPlayerData.rounds.toString()
        currentPlayerIcon.setImageBitmap(viewModel.currentPlayer.icon)
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

        val (roundTimeH, roundTimeM, roundTimeS, roundTimeMS) = GameViewModel.getTimeComponentStrings(roundTimeToUse)
        round_time_result =
            "$round_time_result$roundTimeH:$roundTimeM:$roundTimeS'$roundTimeMS"
        roundTimerTv.text = round_time_result

        // highlight low timers red colored
        if (currentRoundTimeMs <= roundTimeOriTenPercent) roundTimerTv.setTextColor(colorOvertime)
        else roundTimerTv.setTextColor(colorNormal)

        // if game time is not infinite
        if (viewModel.game.gameTimeInfinite == 0) {
            val (gameTimeH, gameTimeM, gameTimeS) = GameViewModel.getTimeComponentStrings(gameTimeToUse)
            game_time_result =
                "$game_time_result$gameTimeH:$gameTimeM:$gameTimeS"
            gameTimerTv.text = game_time_result

            if (viewModel.game.gameTimeInfinite == 0 && currentGameTimeMs <= gameTimeOriTenPercent) gameTimerTv.setTextColor(
                colorOvertime
            )
            else gameTimerTv.setTextColor(colorNormal)
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
        viewModel.finishGame()

        saveGameButton.visibility = View.GONE
        finishGameButton.visibility = View.GONE
        nextPlayerButton.visibility = View.GONE
        playPauseButton.setText(R.string.showResults)
        playPauseButton.setOnClickListener {
            showGameResultsDialog.show()
        }
        showGameResultsDialog.show()

        saveGameToDb(0)
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> saveGameAndQuitDialog.show()
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    /**
     * receive Broadcast Information, interpret this and update GUI and every variable based on information received
     * @param intent
     */
    private fun updateGUI() {

        val gameMsTillFinished: Long = viewModel.remainingGameTime
        val gameMsExceeded: Long = -min(0, viewModel.remainingGameTime)
        val roundMsTillFinished: Long = viewModel.elapsedTime()
        val roundMsExceeded: Long = -min(0, viewModel.elapsedTime())

        if (viewModel.game.chessMode == 1 && gameMsExceeded > 0) {
            finishGame()
        }
        if (viewModel.game.chessMode == 1 && roundMsExceeded > 0) {
            viewModel.endPlayerRound()
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
        updateTimerTextViews()
    }
}