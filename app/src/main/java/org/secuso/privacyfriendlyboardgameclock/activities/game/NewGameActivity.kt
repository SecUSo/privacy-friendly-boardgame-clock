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
package org.secuso.privacyfriendlyboardgameclock.activities.game

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import org.secuso.pfacore.ui.activities.BaseActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import androidx.core.view.size
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModelProvider
import org.secuso.privacyfriendlyboardgameclock.room.model.Game

/**
 * Created by Quang Anh Dang on 04.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the New Game Page, after touching New Game Button on the main page
 */
class NewGameActivity : BaseActivity() {
    private val viewModel by lazy { ViewModelProvider(this)[GameViewModel::class.java] }
    private val round_time_s by lazy { findViewById<NumberPicker>(R.id.seconds_new_round_time) }
    private val round_time_m by lazy { findViewById<NumberPicker>(R.id.minutes_new_round_time) }
    private val round_time_h by lazy { findViewById<NumberPicker>(R.id.hours_new_round_time) }
    private val delta_seconds by lazy { findViewById<NumberPicker>(R.id.seconds_new_game_delta) }
    private val delta_minutes by lazy { findViewById<NumberPicker>(R.id.minutes_new_game_delta) }
    private val delta_hours by lazy { findViewById<NumberPicker>(R.id.hours_new_game_delta) }
    private val game_time_s by lazy { findViewById<NumberPicker>(R.id.seconds_new_game_time) }
    private val game_time_m by lazy { findViewById<NumberPicker>(R.id.minutes_new_game_time) }
    private val game_time_h by lazy { findViewById<NumberPicker>(R.id.hours_new_game_time) }
    private val check_new_game_delta: CheckBox by lazy { findViewById(R.id.check_new_game_delta) }
    private val check_new_game_reset_time: CheckBox by lazy { findViewById(R.id.check_new_game_reset_time) }
    private val check_game_time_infinite: CheckBox by lazy { findViewById(R.id.check_game_time_infinite) }
    private val chess_mode: CheckBox by lazy { findViewById(R.id.check_chess_mode) }
    private val game_mode: Spinner by lazy { findViewById(R.id.spinner_new_game_mode) }
    private val game_name: EditText by lazy { findViewById<EditText>(R.id.input_new_game_name) }
    private var nameEntered = true
    private var roundTimeEntered = true
    private var gameTimeEntered = true
    private var round_total_time_in_s = 0
    private var game_total_time_in_s = 0
    private val choosePlayersButtonBlue: Button by lazy { findViewById(R.id.choosePlayersButtonBlue) }
    private val choosePlayersButtonGrey: Button by lazy { findViewById(R.id.choosePlayersButtonGrey) }

    var gameValueChangedListener: NumberPicker.OnValueChangeListener =
        NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
            setGameTime()
            checkIfGameTimeEntered()
        }

    private fun checkIfGameTimeEntered() {
        gameTimeEntered = game_total_time_in_s > 0

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite.isChecked())) {
            choosePlayersButtonBlue.visibility = View.VISIBLE
            choosePlayersButtonGrey.visibility = View.GONE
        } else {
            choosePlayersButtonBlue.visibility = View.GONE
            choosePlayersButtonGrey.visibility = View.VISIBLE
        }
    }

    var roundValueChangedListener: NumberPicker.OnValueChangeListener =
        NumberPicker.OnValueChangeListener { picker, oldVal, newVal ->
            setRoundTime()
            checkIfRoundTimeEntered()
        }

    private fun checkIfRoundTimeEntered() {
        roundTimeEntered = round_total_time_in_s > 0

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite.isChecked)) {
            choosePlayersButtonBlue.visibility = View.VISIBLE
            choosePlayersButtonGrey.visibility = View.GONE
        } else {
            choosePlayersButtonBlue.visibility = View.GONE
            choosePlayersButtonGrey.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)

        val dividerColor = "#024265"
        for (entry in listOf(game_time_s, game_time_m, round_time_s, round_time_m, delta_seconds, delta_minutes)) {
            entry.minValue = 0
            entry.maxValue = 59
            setDividerColor(entry, dividerColor)
        }
        for (entry in listOf(game_time_h, round_time_h, delta_hours)) {
            entry.minValue = 0
            entry.maxValue = 99
            setDividerColor(entry, dividerColor)
        }

        game_time_h.setOnValueChangedListener(gameValueChangedListener)
        game_time_m.setOnValueChangedListener(gameValueChangedListener)
        game_time_s.setOnValueChangedListener(gameValueChangedListener)

        round_time_h.setOnValueChangedListener(roundValueChangedListener)
        round_time_m.setOnValueChangedListener(roundValueChangedListener)
        round_time_s.setOnValueChangedListener(roundValueChangedListener)

        check_new_game_delta.setOnCheckedChangeListener { buttonView, isChecked ->
            findViewById<LinearLayout>(R.id.timer_new_game_delta).apply {
                visibility = if (isChecked) View.VISIBLE else View.GONE
            }
        }

        // complicated looking way to switch text color in nested custom number pickers
        // game timer number pickers should be greyed out and deactivated once "infinite" is selected
        check_game_time_infinite.setOnCheckedChangeListener { buttonView, isChecked ->
            val game_timers = findViewById<LinearLayout>(R.id.timer_new_game_time)
            if (isChecked) {
                if (nameEntered && roundTimeEntered) {
                    choosePlayersButtonBlue.visibility = View.VISIBLE
                    choosePlayersButtonGrey.visibility = View.GONE
                } else {
                    choosePlayersButtonBlue.visibility = View.GONE
                    choosePlayersButtonGrey.visibility = View.VISIBLE
                }

                for (i in 0..<game_timers.size) {
                    if (game_timers.getChildAt(i) is org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                        game_timers.getChildAt(i).setEnabled(false)
                        for (j in 0..<(game_timers.getChildAt(i) as ViewGroup).size) {
                            (game_timers.getChildAt(i) as ViewGroup).getChildAt(j).apply {
                                if (this is EditText) {
                                    setTextColor(Color.LTGRAY)
                                }
                            }
                        }
                    } else if (game_timers.getChildAt(i) is TextView) {
                        (game_timers.getChildAt(i) as TextView).setTextColor(Color.LTGRAY)
                    }
                }
            } else {
                if (nameEntered && roundTimeEntered && gameTimeEntered) {
                    choosePlayersButtonBlue.visibility = View.VISIBLE
                    choosePlayersButtonGrey.visibility = View.GONE
                } else {
                    choosePlayersButtonBlue.visibility = View.GONE
                    choosePlayersButtonGrey.visibility = View.VISIBLE
                }

                Log.e("NewGameActivity", game_timers.size.toString())
                for (i in 0..<game_timers.size) if (game_timers.getChildAt(i) is org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                    game_timers.getChildAt(i).setEnabled(true)
                    for (j in 0..<(game_timers.getChildAt(i) as ViewGroup).size) {
                        (game_timers.getChildAt(i) as ViewGroup).getChildAt(j).apply {
                            if (this is EditText) {
                                setTextColor(Color.BLACK)
                            }
                        }
                    }
                } else if (game_timers.getChildAt(i) is TextView) (game_timers.getChildAt(i) as TextView).setTextColor(
                    Color.BLACK
                )
            }

            setGameTime()
            checkIfGameTimeEntered()
        }

        game_mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                if (game_mode.selectedItemPosition == TAGHelper.TIME_TRACKING) {
                    choosePlayersButtonBlue.visibility = View.VISIBLE
                    choosePlayersButtonGrey.visibility = View.GONE
                    findViewById<View?>(R.id.time_properties)?.visibility = View.INVISIBLE
                    // standard time for time tracking mode, all start by 0
                    round_time_h.value = 0
                    round_time_m.value = 0
                    round_time_s.value = 0
                    game_time_h.value = 0
                    game_time_m.value = 0
                    game_time_s.value = 0
                    gameTimeEntered = true
                    roundTimeEntered = true
                } else {
                    findViewById<View?>(R.id.time_properties)?.visibility = View.VISIBLE
                    setGameTime()
                    setRoundTime()
                    checkIfGameTimeEntered()
                    checkIfRoundTimeEntered()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        }

        choosePlayersButtonBlue.setOnClickListener { createNewGame() }

        choosePlayersButtonGrey.setOnClickListener { createNewGame() }

        game_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable?) {
                nameEntered = game_name.getText().toString().isNotEmpty()

                if (nameEntered && roundTimeEntered && gameTimeEntered) {
                    choosePlayersButtonBlue.visibility = View.VISIBLE
                    choosePlayersButtonGrey.visibility = View.GONE
                } else {
                    choosePlayersButtonBlue.visibility = View.GONE
                    choosePlayersButtonGrey.visibility = View.VISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // load game number
        val settings = PreferenceManager.getDefaultSharedPreferences(this)
        val gameNumber = settings.getInt("gameNumber", 1)
        game_name.setText(getString(R.string.gameNameStandard) + " " + gameNumber)

        val lastGame = viewModel.getLastGame()
        // standard values
        if (lastGame != null) {
            chess_mode.setChecked(lastGame.chessMode != 0)
            check_game_time_infinite.setChecked(lastGame.gameTimeInfinite != 0)
            check_new_game_reset_time.setChecked(lastGame.resetRoundTime != 0)
            check_new_game_delta.setChecked(lastGame.roundTimeDelta > 0)

            if (lastGame.gameTimeInfinite != 0) {
                game_time_h.value = getTimeValues(lastGame.gameTime)[0]
                game_time_m.value = getTimeValues(lastGame.gameTime)[1]
                game_time_s.value = getTimeValues(lastGame.gameTime)[2]
            } else {
                game_time_h.value = 0
                game_time_m.value = 0
                game_time_s.value = 0
            }
            round_time_h.value = getTimeValues(lastGame.roundTime)[0]
            round_time_m.value = getTimeValues(lastGame.roundTime)[1]
            round_time_s.value = getTimeValues(lastGame.roundTime)[2]
            // previous game mode
            game_mode.setSelection(lastGame.gameMode)

            if (lastGame.roundTimeDelta > 0) {
                delta_seconds.value = getTimeValues(lastGame.roundTimeDelta)[0]
                delta_minutes.value = getTimeValues(lastGame.roundTimeDelta)[1]
                delta_hours.value = getTimeValues(lastGame.roundTimeDelta)[2]
            }
        } else {
            game_time_h.value = 1
            round_time_m.value = 5
        }

        setGameTime()
        setRoundTime()
        val rootView = findViewById<View>(R.id.main_content)
        rootView.setBackgroundColor(getResources().getColor(R.color.white))
    }

    private fun setRoundTime() {
        val round_time_h_in_s = round_time_h.value * 3600
        val round_time_m_in_s = round_time_m.value * 60
        round_total_time_in_s = round_time_s.value + round_time_m_in_s + round_time_h_in_s
    }

    private fun setGameTime() {
        if (check_game_time_infinite.isChecked) {
            game_total_time_in_s = Int.Companion.MAX_VALUE
            return
        }

        val game_time_h_in_s = game_time_h.value * 3600
        val game_time_m_in_s = game_time_m.value * 60
        game_total_time_in_s = game_time_s.value + game_time_m_in_s + game_time_h_in_s
    }

    private fun setDividerColor(picker: NumberPicker?, color: String) {
        val pickerFields = NumberPicker::class.java.declaredFields
        for (pf in pickerFields) {
            if (pf.getName() == "mSelectionDivider") {
                pf.isAccessible = true
                try {
                    val colorDrawable = color.toColorInt().toDrawable()
                    pf.set(picker, colorDrawable)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: Resources.NotFoundException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    private fun createNewGame() {
        val newGame = Game()

        //game name
        newGame.name = game_name.getText().toString()

        //round time
        round_time_h.clearFocus()
        round_time_m.clearFocus()
        round_time_s.clearFocus()
        setRoundTime()
        newGame.roundTime = (round_total_time_in_s * 1000).toLong()


        //game time
        game_time_h.clearFocus()
        game_time_m.clearFocus()
        game_time_s.clearFocus()
        setGameTime()
        newGame.gameTime = (if (check_game_time_infinite.isChecked) game_total_time_in_s else game_total_time_in_s * 1000).toLong()

        newGame.isLastRound = 0

        if (!nameEntered) {
            Toast.makeText(this, R.string.gameNameSizeError, Toast.LENGTH_SHORT).show()
            return
        } else if (!gameTimeEntered || !roundTimeEntered) {
            Toast.makeText(this, R.string.roundTimeSetError, Toast.LENGTH_SHORT).show()
            return
        } else {
            //reset round time
            if (check_new_game_reset_time.isChecked) newGame.resetRoundTime = 1
            else newGame.resetRoundTime = 0


            //round time delta
            if (check_new_game_delta.isChecked) {
                val delta_hours_in_seconds = delta_hours.value * 3600
                val delta_minutes_in_seconds = delta_minutes.value * 60
                val total_delta_in_seconds =
                    delta_seconds.value + delta_hours_in_seconds + delta_minutes_in_seconds

                newGame.roundTimeDelta = (total_delta_in_seconds * 1000).toLong()
            }

            //game time infinite
            if (check_game_time_infinite.isChecked) newGame.gameTimeInfinite = 1
            else newGame.gameTimeInfinite = 0

            //chess mode
            if (chess_mode.isChecked) newGame.chessMode = 1
            else newGame.chessMode = 0

            //new games are never saved
            newGame.saved = 0

            //game mode
            newGame.gameMode = game_mode.selectedItemPosition

            viewModel.newGame = newGame


            // round time must not be larger than game time
            if (newGame.gameTime < newGame.roundTime) {
                newGame.roundTime = newGame.gameTime
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_new_game)
                    .setMessage(R.string.roundTimeLargerInfo)
                    .setPositiveButton(R.string.ok) { _,_ -> choosePlayers() }
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .show()
            } else choosePlayers()
        }
    }

    fun choosePlayers() {
        val intent = Intent(this, ChoosePlayersActivity::class.java)
        startActivity(intent)
    }

    private fun getTimeValues(time_ms: Long): IntArray {
        val h = (time_ms / 3600000).toInt()
        val m = (time_ms - h * 3600000).toInt() / 60000
        val s = (time_ms - h * 3600000 - m * 60000).toInt() / 1000

        return intArrayOf(h, m, s)
    }
}
