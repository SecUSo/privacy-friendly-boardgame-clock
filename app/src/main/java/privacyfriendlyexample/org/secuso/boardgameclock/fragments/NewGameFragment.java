package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SyncStatusObserver;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;

public class NewGameFragment extends Fragment {

    private Activity activity;
    private NumberPicker round_time_s, round_time_m, round_time_h;
    private NumberPicker delta_seconds, delta_minutes, delta_hours;
    private NumberPicker game_time_s, game_time_m, game_time_h;
    private CheckBox check_new_game_delta, check_new_game_reset_time;
    private Spinner game_mode;
    private EditText game_name;
    private boolean nameEntered = false, roundTimeEntered = true, gameTimeEntered = true;
    private int round_total_time_in_s, game_total_time_in_s;
    private Button choosePlayersButtonBlue, choosePlayersButtonGrey;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = this.getActivity();

        View rootView = inflater.inflate(R.layout.fragment_new_game, container, false);
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

        round_time_m.setValue(2);
        game_time_m.setValue(15);
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
                if (inputGameName.getText().toString().length() > 0)
                    nameEntered = true;
                else
                    nameEntered = false;

                if (nameEntered && roundTimeEntered && gameTimeEntered){
                    choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                    choosePlayersButtonGrey.setVisibility(View.GONE);
                }
                else
                {
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

        return rootView;
    }

    NumberPicker.OnValueChangeListener gameValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            setGameTime();

            if(game_total_time_in_s <= 0)
                gameTimeEntered = false;
            else
                gameTimeEntered = true;

            if (nameEntered && roundTimeEntered && gameTimeEntered){
                choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                choosePlayersButtonGrey.setVisibility(View.GONE);
            }
            else
            {
                choosePlayersButtonBlue.setVisibility(View.GONE);
                choosePlayersButtonGrey.setVisibility(View.VISIBLE);
            }

        }
    };

    NumberPicker.OnValueChangeListener roundValueChangedListener = new NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            setRoundTime();

            if(round_total_time_in_s <= 0)
                roundTimeEntered = false;
            else
                roundTimeEntered = true;

            if (nameEntered && roundTimeEntered && gameTimeEntered){
                choosePlayersButtonBlue.setVisibility(View.VISIBLE);
                choosePlayersButtonGrey.setVisibility(View.GONE);
            }
            else
            {
                choosePlayersButtonBlue.setVisibility(View.GONE);
                choosePlayersButtonGrey.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }


    private void setRoundTime(){
        int round_time_h_in_s = round_time_h.getValue() * 3600;
        int round_time_m_in_s = round_time_m.getValue() * 60;
        round_total_time_in_s = round_time_s.getValue() + round_time_m_in_s + round_time_h_in_s;
    }

    private void setGameTime(){
        int game_time_h_in_s = game_time_h.getValue() * 3600;
        int game_time_m_in_s = game_time_m.getValue() * 60;
        game_total_time_in_s = game_time_s.getValue() + game_time_m_in_s + game_time_h_in_s;
    }

    private void createNewGame() {
        Game newGame = new Game();

        //game name
        newGame.setName(game_name.getText().toString());

        //round time
        newGame.setRound_time(round_total_time_in_s);

        //game time
        newGame.setGame_time(game_total_time_in_s);

        if (!nameEntered) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.error)
                    .setMessage(getString(R.string.gameNameSizeError))
                    .setIcon(android.R.drawable.ic_menu_help)

                    .setPositiveButton(getString(R.string.ok), null)
                            .show();
        } else if (!gameTimeEntered || !roundTimeEntered) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.ok)
                    .setMessage(R.string.roundTimeSetError)
                    .setPositiveButton(R.string.ok, null)
                    .setIcon(android.R.drawable.ic_menu_help)

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

                newGame.setRound_time_delta(total_delta_in_seconds);
            }

            //game mode
            newGame.setGame_mode(game_mode.getSelectedItemPosition());

            //new games are never saved
            newGame.setSaved(0);

            ((MainActivity) activity).setGame(newGame);

            choosePlayers();
        }
    }

    public void choosePlayers() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new ChoosePlayersFragment());
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


}
