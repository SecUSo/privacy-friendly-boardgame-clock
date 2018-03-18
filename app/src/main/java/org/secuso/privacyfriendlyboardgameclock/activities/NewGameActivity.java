package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.sql.SQLOutput;

/**
 * Created by Quang Anh Dang on 04.01.2018.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 * This is the Activity for the New Game Page, after touching New Game Button on the main page
 */

public class NewGameActivity extends BaseActivity {
    private GamesDataSourceSingleton gds;
    private NumberPicker round_time_s, round_time_m, round_time_h;
    private NumberPicker delta_seconds, delta_minutes, delta_hours;
    private NumberPicker game_time_s, game_time_m, game_time_h;
    private CheckBox check_new_game_delta, check_new_game_reset_time, check_game_time_infinite, chess_mode;
    private Spinner game_mode;
    private EditText game_name;
    private boolean nameEntered = true, roundTimeEntered = true, gameTimeEntered = true;
    private int round_total_time_in_s, game_total_time_in_s;
    private Button choosePlayersButtonBlue, choosePlayersButtonGrey;
    NumberPicker.OnValueChangeListener gameValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            setGameTime();
            checkIfGameTimeEntered();
        }
    };

    private void checkIfGameTimeEntered() {
        gameTimeEntered = game_total_time_in_s > 0;

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite.isChecked())) {
            choosePlayersButtonBlue.setVisibility(View.VISIBLE);
            choosePlayersButtonGrey.setVisibility(View.GONE);
        } else {
            choosePlayersButtonBlue.setVisibility(View.GONE);
            choosePlayersButtonGrey.setVisibility(View.VISIBLE);
        }

    }

    NumberPicker.OnValueChangeListener roundValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            setRoundTime();
            checkIfRoundTimeEntered();
        }
    };

    private void checkIfRoundTimeEntered() {
        roundTimeEntered = round_total_time_in_s > 0;

        if (nameEntered && roundTimeEntered && (gameTimeEntered || check_game_time_infinite.isChecked())) {
            choosePlayersButtonBlue.setVisibility(View.VISIBLE);
            choosePlayersButtonGrey.setVisibility(View.GONE);
        } else {
            choosePlayersButtonBlue.setVisibility(View.GONE);
            choosePlayersButtonGrey.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        gds = GamesDataSourceSingleton.getInstance(this);

        game_time_s = findViewById(R.id.seconds_new_game_time);
        game_time_s.setMinValue(0);
        game_time_s.setMaxValue(59);

        game_time_m = findViewById(R.id.minutes_new_game_time);
        game_time_m.setMinValue(0);
        game_time_m.setMaxValue(59);

        game_time_h = findViewById(R.id.hours_new_game_time);
        game_time_h.setMinValue(0);
        game_time_h.setMaxValue(99);

        round_time_s = findViewById(R.id.seconds_new_round_time);
        round_time_s.setMinValue(0);
        round_time_s.setMaxValue(59);

        round_time_m = findViewById(R.id.minutes_new_round_time);
        round_time_m.setMinValue(0);
        round_time_m.setMaxValue(59);

        round_time_h = findViewById(R.id.hours_new_round_time);
        round_time_h.setMinValue(0);
        round_time_h.setMaxValue(99);

        delta_seconds = findViewById(R.id.seconds_new_game_delta);
        delta_seconds.setMinValue(0);
        delta_seconds.setMaxValue(59);

        delta_minutes = findViewById(R.id.minutes_new_game_delta);
        delta_minutes.setMinValue(0);
        delta_minutes.setMaxValue(59);

        delta_hours = findViewById(R.id.hours_new_game_delta);
        delta_hours.setMinValue(0);
        delta_hours.setMaxValue(99);

        game_time_h.setOnValueChangedListener(gameValueChangedListener);
        game_time_m.setOnValueChangedListener(gameValueChangedListener);
        game_time_s.setOnValueChangedListener(gameValueChangedListener);

        round_time_h.setOnValueChangedListener(roundValueChangedListener);
        round_time_m.setOnValueChangedListener(roundValueChangedListener);
        round_time_s.setOnValueChangedListener(roundValueChangedListener);

        String dividerColor = "#024265";
        setDividerColor(round_time_s, dividerColor);
        setDividerColor(round_time_m, dividerColor);
        setDividerColor(round_time_h, dividerColor);
        setDividerColor(delta_seconds, dividerColor);
        setDividerColor(delta_minutes, dividerColor);
        setDividerColor(delta_hours, dividerColor);
        setDividerColor(game_time_h, dividerColor);
        setDividerColor(game_time_m, dividerColor);
        setDividerColor(game_time_s, dividerColor);

        check_new_game_delta = findViewById(R.id.check_new_game_delta);
        check_new_game_reset_time = findViewById(R.id.check_new_game_reset_time);
        check_game_time_infinite = findViewById(R.id.check_game_time_infinite);
        chess_mode = findViewById(R.id.check_chess_mode);

        final LinearLayout delta_timers = findViewById(R.id.timer_new_game_delta);
        check_new_game_delta.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    delta_timers.setVisibility(View.VISIBLE);
                } else {
                    delta_timers.setVisibility(View.INVISIBLE);
                }
            }
        });

        // complicated looking way to switch text color in nested custom number pickers
        // game timer number pickers should be greyed out and deactivated once "infinite" is selected
        check_game_time_infinite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LinearLayout game_timers = findViewById(R.id.timer_new_game_time);
                if (isChecked) {
                    if (nameEntered && roundTimeEntered){
                        choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                        choosePlayersButtonGrey.setVisibility(View.GONE);
                    } else {
                        choosePlayersButtonBlue.setVisibility(View.GONE);
                        choosePlayersButtonGrey.setVisibility(View.VISIBLE);
                    }

                    for (int i = 0; i < game_timers.getChildCount(); i++)
                        if (game_timers.getChildAt(i) instanceof org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                            game_timers.getChildAt(i).setEnabled(false);
                            for (int j = 0; j < ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildCount(); j++)
                                if (((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j) instanceof EditText)
                                    ((EditText) ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j)).setTextColor(Color.LTGRAY);
                        } else if (game_timers.getChildAt(i) instanceof TextView)
                            ((TextView) game_timers.getChildAt(i)).setTextColor(Color.LTGRAY);

                } else {
                    if (nameEntered && roundTimeEntered && gameTimeEntered ) {
                        choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                        choosePlayersButtonGrey.setVisibility(View.GONE);
                    } else {
                        choosePlayersButtonBlue.setVisibility(View.GONE);
                        choosePlayersButtonGrey.setVisibility(View.VISIBLE);
                    }

                    System.err.println(game_timers.getChildCount());
                    for (int i = 0; i < game_timers.getChildCount(); i++)
                        if (game_timers.getChildAt(i) instanceof org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                            game_timers.getChildAt(i).setEnabled(true);
                            for (int j = 0; j < ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildCount(); j++)
                                if (((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j) instanceof EditText)
                                    ((EditText) ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j)).setTextColor(Color.BLACK);
                        } else if (game_timers.getChildAt(i) instanceof TextView)
                            ((TextView) game_timers.getChildAt(i)).setTextColor(Color.BLACK);

                }
            }
        });

        game_mode = findViewById(R.id.spinner_new_game_mode);
        game_mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(game_mode.getSelectedItemPosition() == TAGHelper.TIME_TRACKING){
                    choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                    choosePlayersButtonGrey.setVisibility(View.GONE);
                    findViewById(R.id.time_properties).setVisibility(View.INVISIBLE);
                    // standard time for time tracking mode, all start by 0
                    round_time_h.setValue(0);
                    round_time_m.setValue(0);
                    round_time_s.setValue(0);
                    game_time_h.setValue(0);
                    game_time_m.setValue(0);
                    game_time_s.setValue(0);
                    gameTimeEntered = true;
                    roundTimeEntered = true;
                }
                else{
                    findViewById(R.id.time_properties).setVisibility(View.VISIBLE);
                    setGameTime();
                    setRoundTime();
                    checkIfGameTimeEntered();
                    checkIfRoundTimeEntered();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        game_name = findViewById(R.id.input_new_game_name);

        choosePlayersButtonBlue = findViewById(R.id.choosePlayersButtonBlue);
        choosePlayersButtonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        choosePlayersButtonGrey = findViewById(R.id.choosePlayersButtonGrey);
        choosePlayersButtonGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        final EditText inputGameName = findViewById(R.id.input_new_game_name);
        inputGameName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
                nameEntered = inputGameName.getText().toString().length() > 0;

                if (nameEntered && roundTimeEntered && gameTimeEntered) {
                    choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                    choosePlayersButtonGrey.setVisibility(View.GONE);
                } else {
                    choosePlayersButtonBlue.setVisibility(View.GONE);
                    choosePlayersButtonGrey.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // load game number
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int gameNumber = settings.getInt("gameNumber", 1);
        inputGameName.setText(getString(R.string.gameNameStandard) + " " + gameNumber);

        // standard values
        if (gds.getGame() != null) {
            Game g = gds.getGame();

            game_time_h.setValue(getTimeValues(g.getGame_time())[0]);
            game_time_m.setValue(getTimeValues(g.getGame_time())[1]);
            game_time_s.setValue(getTimeValues(g.getGame_time())[2]);
            round_time_h.setValue(getTimeValues(g.getRound_time())[0]);
            round_time_m.setValue(getTimeValues(g.getRound_time())[1]);
            round_time_s.setValue(getTimeValues(g.getRound_time())[2]);
            // previous game mode
            game_mode.setSelection(g.getGame_mode());

            if (g.getRound_time_delta() > 0) {
                delta_seconds.setValue(getTimeValues(g.getRound_time_delta())[0]);
                delta_minutes.setValue(getTimeValues(g.getRound_time_delta())[1]);
                delta_hours.setValue(getTimeValues(g.getRound_time_delta())[2]);
            }
        } else {
            game_time_h.setValue(1);
            round_time_m.setValue(5);
        }

        setGameTime();
        setRoundTime();
        gds.setGame(null);
        View rootView = findViewById(R.id.main_content);
        rootView.setBackgroundColor(getResources().getColor(R.color.white));
    }

    private boolean isRoundTimeEntered() {
        return round_time_h.getValue() != 0 || round_time_m.getValue() != 0 || round_time_s.getValue() != 0;
    }

    private boolean isGameTimeEntered() {
        return game_time_h.getValue() != 0 || game_time_m.getValue() != 0 || game_time_s.getValue() != 0;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.game_button_start);
        // disable NavigationDrawer
        setDrawerEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * when ever user chooses to navigate up within app activity hierachy from the action bar
     * @return
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setRoundTime() {
        int round_time_h_in_s = round_time_h.getValue() * 3600;
        int round_time_m_in_s = round_time_m.getValue() * 60;
        round_total_time_in_s = round_time_s.getValue() + round_time_m_in_s + round_time_h_in_s;
    }

    private void setGameTime() {
        int game_time_h_in_s = game_time_h.getValue() * 3600;
        int game_time_m_in_s = game_time_m.getValue() * 60;
        game_total_time_in_s = game_time_s.getValue() + game_time_m_in_s + game_time_h_in_s;
    }

    private void setDividerColor(NumberPicker picker, String color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(color));
                    pf.set(picker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void createNewGame() {

        Game newGame = new Game();

        //game name
        newGame.setName(game_name.getText().toString());

        //round time
        round_time_h.clearFocus();
        round_time_m.clearFocus();
        round_time_s.clearFocus();
        setRoundTime();
        newGame.setRound_time(round_total_time_in_s * 1000);

        //game time
        game_time_h.clearFocus();
        game_time_m.clearFocus();
        game_time_s.clearFocus();
        setGameTime();
        newGame.setGame_time(game_total_time_in_s * 1000);

        newGame.setIsLastRound(0);

        if (!nameEntered) {
            showToast(getString(R.string.gameNameSizeError));
            return;
        } else if (!gameTimeEntered || !roundTimeEntered) {
            showToast(getString(R.string.roundTimeSetError));
            return;
        } else {
            //reset round time
            if (check_new_game_reset_time.isChecked())
                newGame.setReset_round_time(1);
            else
                newGame.setReset_round_time(0);

            //round time delta
            if (check_new_game_delta.isChecked()) {
                int delta_hours_in_seconds = delta_hours.getValue() * 3600;
                int delta_minutes_in_seconds = delta_minutes.getValue() * 60;
                int total_delta_in_seconds = delta_seconds.getValue() + delta_hours_in_seconds + delta_minutes_in_seconds;

                newGame.setRound_time_delta(total_delta_in_seconds * 1000);
            }

            //game time infinite
            if (check_game_time_infinite.isChecked())
                newGame.setGame_time_infinite(1);
            else
                newGame.setGame_time_infinite(0);

            //chess mode
            if (chess_mode.isChecked())
                newGame.setChess_mode(1);
            else
                newGame.setChess_mode(0);

            //new games are never saved
            newGame.setSaved(0);

            //game mode
            newGame.setGame_mode(game_mode.getSelectedItemPosition());

            gds.setGame(newGame);


            // round time must not be larger than game time
            if (newGame.getGame_time() < newGame.getRound_time()) {
                gds.getGame().setRound_time(newGame.getGame_time());
                new AlertDialog.Builder(this)
                        .setTitle(R.string.action_new_game)
                        .setMessage(R.string.roundTimeLargerInfo)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                choosePlayers();
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_info_details)
                        .show();
            } else
                choosePlayers();
        }
    }

    public void choosePlayers() {
        Intent intent = new Intent(this, ChoosePlayersActivity.class);
        startActivity(intent);
    }

    private int[] getTimeValues(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;

        return new int[]{h, m, s};
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }
}
