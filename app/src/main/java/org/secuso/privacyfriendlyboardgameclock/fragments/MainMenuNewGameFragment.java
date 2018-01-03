package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

/**
 * Created by Quang Anh Dang on 15.12.2017.
 */

public class MainMenuNewGameFragment extends Fragment{
    private Activity activity;
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

            gameTimeEntered = game_total_time_in_s > 0;

            if (nameEntered && roundTimeEntered && gameTimeEntered) {
                choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                choosePlayersButtonGrey.setVisibility(View.GONE);
            } else {
                choosePlayersButtonBlue.setVisibility(View.GONE);
                choosePlayersButtonGrey.setVisibility(View.VISIBLE);
            }

        }
    };
    NumberPicker.OnValueChangeListener roundValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            setRoundTime();

            roundTimeEntered = round_total_time_in_s > 0;

            if (nameEntered && roundTimeEntered && gameTimeEntered) {
                choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                choosePlayersButtonGrey.setVisibility(View.GONE);
            } else {
                choosePlayersButtonBlue.setVisibility(View.GONE);
                choosePlayersButtonGrey.setVisibility(View.VISIBLE);
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = this.getActivity();

        final View rootView = inflater.inflate(R.layout.fragment_main_menu_new_game, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_new_game);
        container.removeAllViews();

        game_time_s = (NumberPicker) rootView.findViewById(R.id.seconds_new_game_time);
        game_time_s.setMinValue(0);
        game_time_s.setMaxValue(59);

        game_time_m = (NumberPicker) rootView.findViewById(R.id.minutes_new_game_time);
        game_time_m.setMinValue(0);
        game_time_m.setMaxValue(59);

        game_time_h = (NumberPicker) rootView.findViewById(R.id.hours_new_game_time);
        game_time_h.setMinValue(0);
        game_time_h.setMaxValue(99);

        round_time_s = (NumberPicker) rootView.findViewById(R.id.seconds_new_round_time);
        round_time_s.setMinValue(0);
        round_time_s.setMaxValue(59);

        round_time_m = (NumberPicker) rootView.findViewById(R.id.minutes_new_round_time);
        round_time_m.setMinValue(0);
        round_time_m.setMaxValue(59);

        round_time_h = (NumberPicker) rootView.findViewById(R.id.hours_new_round_time);
        round_time_h.setMinValue(0);
        round_time_h.setMaxValue(99);

        delta_seconds = (NumberPicker) rootView.findViewById(R.id.seconds_new_game_delta);
        delta_seconds.setMinValue(0);
        delta_seconds.setMaxValue(59);

        delta_minutes = (NumberPicker) rootView.findViewById(R.id.minutes_new_game_delta);
        delta_minutes.setMinValue(0);
        delta_minutes.setMaxValue(59);

        delta_hours = (NumberPicker) rootView.findViewById(R.id.hours_new_game_delta);
        delta_hours.setMinValue(0);
        delta_hours.setMaxValue(99);

        game_time_h.setOnValueChangedListener(gameValueChangedListener);
        game_time_m.setOnValueChangedListener(gameValueChangedListener);
        game_time_s.setOnValueChangedListener(gameValueChangedListener);

        round_time_h.setOnValueChangedListener(roundValueChangedListener);
        round_time_m.setOnValueChangedListener(roundValueChangedListener);
        round_time_s.setOnValueChangedListener(roundValueChangedListener);

        // standard values
        if (((MainActivity) activity).getGame() != null) {
            Game g = ((MainActivity) activity).getGame();

            game_time_h.setValue(getTimeValues(g.getGame_time())[0]);
            game_time_m.setValue(getTimeValues(g.getGame_time())[1]);
            game_time_s.setValue(getTimeValues(g.getGame_time())[2]);
            round_time_h.setValue(getTimeValues(g.getRound_time())[0]);
            round_time_m.setValue(getTimeValues(g.getRound_time())[1]);
            round_time_s.setValue(getTimeValues(g.getRound_time())[2]);

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

        check_new_game_delta = (CheckBox) rootView.findViewById(R.id.check_new_game_delta);
        check_new_game_reset_time = (CheckBox) rootView.findViewById(R.id.check_new_game_reset_time);
        check_game_time_infinite = (CheckBox) rootView.findViewById(R.id.check_game_time_infinite);
        chess_mode = (CheckBox) rootView.findViewById(R.id.check_chess_mode);


        final LinearLayout delta_timers = (LinearLayout) rootView.findViewById(R.id.timer_new_game_delta);
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
                LinearLayout game_timers = (LinearLayout) rootView.findViewById(R.id.timer_new_game_time);
                if (isChecked) {
                    for (int i = 0; i < game_timers.getChildCount(); i++)
                        if (game_timers.getChildAt(i) instanceof org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) {
                            game_timers.getChildAt(i).setEnabled(false);
                            for (int j = 0; j < ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildCount(); j++)
                                if (((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j) instanceof EditText)
                                    ((EditText) ((org.secuso.privacyfriendlyboardgameclock.helpers.NumberPicker) game_timers.getChildAt(i)).getChildAt(j)).setTextColor(Color.LTGRAY);
                        } else if (game_timers.getChildAt(i) instanceof TextView)
                            ((TextView) game_timers.getChildAt(i)).setTextColor(Color.LTGRAY);

                } else {
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

        game_mode = (Spinner) rootView.findViewById(R.id.spinner_new_game_mode);

        game_name = (EditText) rootView.findViewById(R.id.input_new_game_name);

        choosePlayersButtonBlue = (Button) rootView.findViewById(R.id.choosePlayersButtonBlue);
        choosePlayersButtonBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        choosePlayersButtonGrey = (Button) rootView.findViewById(R.id.choosePlayersButtonGrey);
        choosePlayersButtonGrey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();
            }
        });

        final EditText inputGameName = (EditText) rootView.findViewById(R.id.input_new_game_name);
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        int gameNumber = settings.getInt("gameNumber", 1);
        inputGameName.setText(getActivity().getString(R.string.gameNameStandard) + " " + gameNumber);

        ((MainActivity) activity).setGame(null);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }

    @Override
    public void onResume() {
        // disable NavigationDrawer
        ((MainActivity)activity).setDrawerEnabled(false);
        super.onResume();
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
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.error)
                    .setMessage(getString(R.string.gameNameSizeError))
                    .setIcon(android.R.drawable.ic_menu_info_details)

                    .setPositiveButton(getString(R.string.ok), null)
                    .show();
        } else if (!gameTimeEntered || !roundTimeEntered) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.ok)
                    .setMessage(R.string.roundTimeSetError)
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(android.R.drawable.ic_menu_info_details)

                    .show();
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

            //game mode
            newGame.setGame_mode(game_mode.getSelectedItemPosition());

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

            ((MainActivity) activity).setGame(newGame);


            // round time must not be larger than game time
            if (newGame.getGame_time() < newGame.getRound_time()) {
                ((MainActivity) activity).getGame().setRound_time(newGame.getGame_time());
                new AlertDialog.Builder(getActivity())
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

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuChoosePlayersFragment());
        fragmentTransaction.addToBackStack(getString(R.string.choosePlayersFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
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

    private int[] getTimeValues(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;

        return new int[]{h, m, s};
    }
}
