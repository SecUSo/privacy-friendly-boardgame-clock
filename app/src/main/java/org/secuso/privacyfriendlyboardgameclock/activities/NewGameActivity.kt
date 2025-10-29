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

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Spinner
import android.widget.TextView
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper
import org.secuso.privacyfriendlyboardgameclock.model.Game

/**
 * Created by Quang Anh Dang on 04.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the New Game Page, after touching New Game Button on the main page
 */
class NewGameActivity : BaseActivity() {
    private var gds: GamesDataSourceSingleton? = null
    private var round_time_s: NumberPicker? = null
    private var round_time_m: NumberPicker? = null
    private var round_time_h: NumberPicker? = null
    private var delta_seconds: NumberPicker? = null
    private var delta_minutes: NumberPicker? = null
    private var delta_hours: NumberPicker? = null
    private var game_time_s: NumberPicker? = null
    private var game_time_m: NumberPicker? = null
    private var game_time_h: NumberPicker? = null
    private var check_new_game_delta: CheckBox? = null
    private var check_new_game_reset_time: CheckBox? = null
    private var check_game_time_infinite: CheckBox? = null
    private var chess_mode: CheckBox? = null
    private var game_mode: Spinner? = null
    private var game_name: EditText? = null
    private var nameEntered = true
    private var roundTimeEntered = true
    private var gameTimeEntered = true
    private var round_total_time_in_s = 0
    private var game_total_time_in_s = 0
    private var choosePlayersButtonBlue: Button? = null
    private var choosePlayersButtonGrey: Button? = null
    var gameValueChangedListener: NumberPicker.OnValueChangeListener =
        object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                setGameTime()
                checkIfGameTimeEntered()
            }
        }

    private fun checkIfGameTimeEntered() {
        gameTimeEntered = game_total_time_in_s > 0

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite!!.isChecked())) {
            choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
            choosePlayersButtonGrey!!.setVisibility(View.GONE)
        } else {
            choosePlayersButtonBlue!!.setVisibility(View.GONE)
            choosePlayersButtonGrey!!.setVisibility(View.VISIBLE)
        }
    }

    var roundValueChangedListener: NumberPicker.OnValueChangeListener =
        object : NumberPicker.OnValueChangeListener {
            override fun onValueChange(picker: NumberPicker?, oldVal: Int, newVal: Int) {
                setRoundTime()
                checkIfRoundTimeEntered()
            }
        }

    private fun checkIfRoundTimeEntered() {
        roundTimeEntered = round_total_time_in_s > 0

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite!!.isChecked())) {
            choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
            choosePlayersButtonGrey!!.setVisibility(View.GONE)
        } else {
            choosePlayersButtonBlue!!.setVisibility(View.GONE)
            choosePlayersButtonGrey!!.setVisibility(View.VISIBLE)
        }
    }

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_game)
        gds = GamesDataSourceSingleton.getInstance(this)

        game_time_s = findViewById<NumberPicker>(R.id.seconds_new_game_time)
        game_time_s!!.setMinValue(0)
        game_time_s!!.setMaxValue(59)

        game_time_m = findViewById<NumberPicker>(R.id.minutes_new_game_time)
        game_time_m!!.setMinValue(0)
        game_time_m!!.setMaxValue(59)

        game_time_h = findViewById<NumberPicker>(R.id.hours_new_game_time)
        game_time_h!!.setMinValue(0)
        game_time_h!!.setMaxValue(99)

        round_time_s = findViewById<NumberPicker>(R.id.seconds_new_round_time)
        round_time_s!!.setMinValue(0)
        round_time_s!!.setMaxValue(59)

        round_time_m = findViewById<NumberPicker>(R.id.minutes_new_round_time)
        round_time_m!!.setMinValue(0)
        round_time_m!!.setMaxValue(59)

        round_time_h = findViewById<NumberPicker>(R.id.hours_new_round_time)
        round_time_h!!.setMinValue(0)
        round_time_h!!.setMaxValue(99)

        delta_seconds = findViewById<NumberPicker>(R.id.seconds_new_game_delta)
        delta_seconds!!.setMinValue(0)
        delta_seconds!!.setMaxValue(59)

        delta_minutes = findViewById<NumberPicker>(R.id.minutes_new_game_delta)
        delta_minutes!!.setMinValue(0)
        delta_minutes!!.setMaxValue(59)

        delta_hours = findViewById<NumberPicker>(R.id.hours_new_game_delta)
        delta_hours!!.setMinValue(0)
        delta_hours!!.setMaxValue(99)

        game_time_h!!.setOnValueChangedListener(gameValueChangedListener)
        game_time_m!!.setOnValueChangedListener(gameValueChangedListener)
        game_time_s!!.setOnValueChangedListener(gameValueChangedListener)

        round_time_h!!.setOnValueChangedListener(roundValueChangedListener)
        round_time_m!!.setOnValueChangedListener(roundValueChangedListener)
        round_time_s!!.setOnValueChangedListener(roundValueChangedListener)

        val dividerColor = "#024265"
        setDividerColor(round_time_s, dividerColor)
        setDividerColor(round_time_m, dividerColor)
        setDividerColor(round_time_h, dividerColor)
        setDividerColor(delta_seconds, dividerColor)
        setDividerColor(delta_minutes, dividerColor)
        setDividerColor(delta_hours, dividerColor)
        setDividerColor(game_time_h, dividerColor)
        setDividerColor(game_time_m, dividerColor)
        setDividerColor(game_time_s, dividerColor)

        check_new_game_delta = findViewById<CheckBox>(R.id.check_new_game_delta)
        check_new_game_reset_time = findViewById<CheckBox>(R.id.check_new_game_reset_time)
        check_game_time_infinite = findViewById<CheckBox>(R.id.check_game_time_infinite)
        chess_mode = findViewById<CheckBox>(R.id.check_chess_mode)

        val delta_timers = findViewById<LinearLayout>(R.id.timer_new_game_delta)
        check_new_game_delta!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                if (isChecked) {
                    delta_timers.setVisibility(View.VISIBLE)
                } else {
                    delta_timers.setVisibility(View.INVISIBLE)
                }
            }
        })

        // complicated looking way to switch text color in nested custom number pickers
        // game timer number pickers should be greyed out and deactivated once "infinite" is selected
        check_game_time_infinite!!.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                val game_timers = findViewById<LinearLayout>(R.id.timer_new_game_time)
                if (isChecked) {
                    if (nameEntered && roundTimeEntered) {
                        choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
                        choosePlayersButtonGrey!!.setVisibility(View.GONE)
                    } else {
                        choosePlayersButtonBlue!!.setVisibility(View.GONE)
                        choosePlayersButtonGrey!!.setVisibility(View.VISIBLE)
                    }

                    for (i in 0..<game_timers.getChildCount()) if (game_timers.getChildAt(i) is org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                        game_timers.getChildAt(i).setEnabled(false)
                        for (j in 0..<(game_timers.getChildAt(i) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildCount()) if ((game_timers.getChildAt(
                                i
                            ) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildAt(
                                j
                            ) is EditText
                        ) ((game_timers.getChildAt(i) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildAt(
                            j
                        ) as EditText).setTextColor(
                            Color.LTGRAY
                        )
                    } else if (game_timers.getChildAt(i) is TextView) (game_timers.getChildAt(i) as TextView).setTextColor(
                        Color.LTGRAY
                    )
                } else {
                    if (nameEntered && roundTimeEntered && gameTimeEntered) {
                        choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
                        choosePlayersButtonGrey!!.setVisibility(View.GONE)
                    } else {
                        choosePlayersButtonBlue!!.setVisibility(View.GONE)
                        choosePlayersButtonGrey!!.setVisibility(View.VISIBLE)
                    }

                    System.err.println(game_timers.getChildCount())
                    for (i in 0..<game_timers.getChildCount()) if (game_timers.getChildAt(i) is org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                        game_timers.getChildAt(i).setEnabled(true)
                        for (j in 0..<(game_timers.getChildAt(i) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildCount()) if ((game_timers.getChildAt(
                                i
                            ) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildAt(
                                j
                            ) is EditText
                        ) ((game_timers.getChildAt(i) as org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker).getChildAt(
                            j
                        ) as EditText).setTextColor(
                            Color.BLACK
                        )
                    } else if (game_timers.getChildAt(i) is TextView) (game_timers.getChildAt(i) as TextView).setTextColor(
                        Color.BLACK
                    )
                }

                setGameTime()
                checkIfGameTimeEntered()
            }
        })

        game_mode = findViewById<Spinner>(R.id.spinner_new_game_mode)
        game_mode!!.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                i: Int,
                l: Long
            ) {
                if (game_mode!!.getSelectedItemPosition() == TAGHelper.TIME_TRACKING) {
                    choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
                    choosePlayersButtonGrey!!.setVisibility(View.GONE)
                    findViewById<View?>(R.id.time_properties).setVisibility(View.INVISIBLE)
                    // standard time for time tracking mode, all start by 0
                    round_time_h!!.setValue(0)
                    round_time_m!!.setValue(0)
                    round_time_s!!.setValue(0)
                    game_time_h!!.setValue(0)
                    game_time_m!!.setValue(0)
                    game_time_s!!.setValue(0)
                    gameTimeEntered = true
                    roundTimeEntered = true
                } else {
                    findViewById<View?>(R.id.time_properties).setVisibility(View.VISIBLE)
                    setGameTime()
                    setRoundTime()
                    checkIfGameTimeEntered()
                    checkIfRoundTimeEntered()
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {
            }
        })

        game_name = findViewById<EditText>(R.id.input_new_game_name)

        choosePlayersButtonBlue = findViewById<Button>(R.id.choosePlayersButtonBlue)
        choosePlayersButtonBlue!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                createNewGame()
            }
        })

        choosePlayersButtonGrey = findViewById<Button>(R.id.choosePlayersButtonGrey)
        choosePlayersButtonGrey!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                createNewGame()
            }
        })

        val inputGameName = findViewById<EditText>(R.id.input_new_game_name)
        inputGameName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(arg0: Editable?) {
                nameEntered = inputGameName.getText().toString().length > 0

                if (nameEntered && roundTimeEntered && gameTimeEntered) {
                    choosePlayersButtonBlue!!.setVisibility(View.VISIBLE)
                    choosePlayersButtonGrey!!.setVisibility(View.GONE)
                } else {
                    choosePlayersButtonBlue!!.setVisibility(View.GONE)
                    choosePlayersButtonGrey!!.setVisibility(View.VISIBLE)
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
        inputGameName.setText(getString(R.string.gameNameStandard) + " " + gameNumber)

        // standard values
        if (gds!!.getGame() != null) {
            val g = gds!!.getGame()

            chess_mode!!.setChecked(g.getChess_mode() != 0)
            check_game_time_infinite!!.setChecked(g.getGame_time_infinite() != 0)
            check_new_game_reset_time!!.setChecked(g.getReset_round_time() != 0)
            check_new_game_delta!!.setChecked(g.getRound_time_delta() > 0)

            if (g.getGame_time_infinite() != 0) {
                game_time_h!!.setValue(getTimeValues(g.getGame_time())[0])
                game_time_m!!.setValue(getTimeValues(g.getGame_time())[1])
                game_time_s!!.setValue(getTimeValues(g.getGame_time())[2])
            } else {
                game_time_h!!.setValue(0)
                game_time_m!!.setValue(0)
                game_time_s!!.setValue(0)
            }
            round_time_h!!.setValue(getTimeValues(g.getRound_time())[0])
            round_time_m!!.setValue(getTimeValues(g.getRound_time())[1])
            round_time_s!!.setValue(getTimeValues(g.getRound_time())[2])
            // previous game mode
            game_mode!!.setSelection(g.getGame_mode())

            if (g.getRound_time_delta() > 0) {
                delta_seconds!!.setValue(getTimeValues(g.getRound_time_delta())[0])
                delta_minutes!!.setValue(getTimeValues(g.getRound_time_delta())[1])
                delta_hours!!.setValue(getTimeValues(g.getRound_time_delta())[2])
            }
        } else {
            game_time_h!!.setValue(1)
            round_time_m!!.setValue(5)
        }

        setGameTime()
        setRoundTime()
        gds!!.setGame(null)
        val rootView = findViewById<View>(R.id.main_content)
        rootView.setBackgroundColor(getResources().getColor(R.color.white))
    }

    private fun isRoundTimeEntered(): Boolean {
        return round_time_h!!.getValue() != 0 || round_time_m!!.getValue() != 0 || round_time_s!!.getValue() != 0
    }

    private fun isGameTimeEntered(): Boolean {
        return game_time_h!!.getValue() != 0 || game_time_m!!.getValue() != 0 || game_time_s!!.getValue() != 0
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getSupportActionBar()!!.setTitle(R.string.game_button_start)
        // disable NavigationDrawer
        setDrawerEnabled(false)
    }

    public override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * when ever user chooses to navigate up within app activity hierachy from the action bar
     * @return
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setRoundTime() {
        val round_time_h_in_s = round_time_h!!.getValue() * 3600
        val round_time_m_in_s = round_time_m!!.getValue() * 60
        round_total_time_in_s = round_time_s!!.getValue() + round_time_m_in_s + round_time_h_in_s
    }

    private fun setGameTime() {
        if (check_game_time_infinite!!.isChecked()) {
            game_total_time_in_s = Int.Companion.MAX_VALUE
            return
        }

        val game_time_h_in_s = game_time_h!!.getValue() * 3600
        val game_time_m_in_s = game_time_m!!.getValue() * 60
        game_total_time_in_s = game_time_s!!.getValue() + game_time_m_in_s + game_time_h_in_s
    }

    private fun setDividerColor(picker: NumberPicker?, color: String?) {
        val pickerFields = NumberPicker::class.java.getDeclaredFields()
        for (pf in pickerFields) {
            if (pf.getName() == "mSelectionDivider") {
                pf.setAccessible(true)
                try {
                    val colorDrawable = ColorDrawable(Color.parseColor(color))
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
        newGame.setName(game_name!!.getText().toString())

        //round time
        round_time_h!!.clearFocus()
        round_time_m!!.clearFocus()
        round_time_s!!.clearFocus()
        setRoundTime()
        newGame.setRound_time((round_total_time_in_s * 1000).toLong())


        //game time
        game_time_h!!.clearFocus()
        game_time_m!!.clearFocus()
        game_time_s!!.clearFocus()
        setGameTime()
        newGame.setGame_time((if (check_game_time_infinite!!.isChecked()) game_total_time_in_s else game_total_time_in_s * 1000).toLong())

        newGame.setIsLastRound(0)

        if (!nameEntered) {
            showToast(getString(R.string.gameNameSizeError))
            return
        } else if (!gameTimeEntered || !roundTimeEntered) {
            showToast(getString(R.string.roundTimeSetError))
            return
        } else {
            //reset round time
            if (check_new_game_reset_time!!.isChecked()) newGame.setReset_round_time(1)
            else newGame.setReset_round_time(0)


            //round time delta
            if (check_new_game_delta!!.isChecked()) {
                val delta_hours_in_seconds = delta_hours!!.getValue() * 3600
                val delta_minutes_in_seconds = delta_minutes!!.getValue() * 60
                val total_delta_in_seconds =
                    delta_seconds!!.getValue() + delta_hours_in_seconds + delta_minutes_in_seconds

                newGame.setRound_time_delta((total_delta_in_seconds * 1000).toLong())
            }

            //game time infinite
            if (check_game_time_infinite!!.isChecked()) newGame.setGame_time_infinite(1)
            else newGame.setGame_time_infinite(0)

            //chess mode
            if (chess_mode!!.isChecked()) newGame.setChess_mode(1)
            else newGame.setChess_mode(0)

            //new games are never saved
            newGame.setSaved(0)

            //game mode
            newGame.setGame_mode(game_mode!!.getSelectedItemPosition())

            gds!!.setGame(newGame)


            // round time must not be larger than game time
            if (newGame.getGame_time() < newGame.getRound_time()) {
                gds!!.getGame().setRound_time(newGame.getGame_time())
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_new_game)
                    .setMessage(R.string.roundTimeLargerInfo)
                    .setPositiveButton(R.string.ok, object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, whichButton: Int) {
                            choosePlayers()
                        }
                    })
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

    val navigationDrawerID: Int
        get() = 0
}
