package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;

public class NewGameFragment extends Fragment {

    private Activity activity;
    private NumberPicker time_seconds, time_minutes, time_hours;
    private NumberPicker delta_seconds, delta_minutes, delta_hours;
    private CheckBox check_new_game_delta, check_new_game_reset_time;
    private Spinner game_mode;
    private EditText game_name;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = this.getActivity();

        View rootView = inflater.inflate(R.layout.fragment_new_game, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.action_new_game);
        container.removeAllViews();

        time_seconds =  (NumberPicker) rootView.findViewById(R.id.seconds_new_game_time);
        time_seconds.setMinValue(0);
        time_seconds.setMaxValue(59);

        time_minutes =  (NumberPicker) rootView.findViewById(R.id.minutes_new_game_time);
        time_minutes.setMinValue(0);
        time_minutes.setMaxValue(59);

        time_hours =  (NumberPicker) rootView.findViewById(R.id.hours_new_game_time);
        time_hours.setMinValue(0);
        time_hours.setMaxValue(99);

        delta_seconds =  (NumberPicker) rootView.findViewById(R.id.seconds_new_game_delta);
        delta_seconds.setMinValue(0);
        delta_seconds.setMaxValue(59);

        delta_minutes =  (NumberPicker) rootView.findViewById(R.id.minutes_new_game_delta);
        delta_minutes.setMinValue(0);
        delta_minutes.setMaxValue(59);

        delta_hours =  (NumberPicker) rootView.findViewById(R.id.hours_new_game_delta);
        delta_hours.setMinValue(0);
        delta_hours.setMaxValue(99);

        String dividerColor = "#024265";
        setDividerColor(time_seconds, dividerColor);
        setDividerColor(time_minutes, dividerColor);
        setDividerColor(time_hours, dividerColor);
        setDividerColor(delta_seconds, dividerColor);
        setDividerColor(delta_minutes, dividerColor);
        setDividerColor(delta_hours, dividerColor);

        check_new_game_delta = ( CheckBox ) rootView.findViewById(R.id.check_new_game_delta);
        check_new_game_reset_time = (CheckBox) rootView.findViewById(R.id.check_new_game_reset_time);

        final LinearLayout delta_timers = ( LinearLayout ) rootView.findViewById(R.id.timer_new_game_delta);
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

        Button b = (Button) rootView.findViewById(R.id.choosePlayersButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewGame();

                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content_frame, new ChoosePlayersFragment());
                fragmentTransaction.addToBackStack("ChoosePlayersFragment");
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

                fragmentTransaction.commit();
            }
        });

        return rootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void createNewGame(){
        Game newGame = new Game();

        //game name
        newGame.setName(game_name.getText().toString());

        //round time
        int hours_in_seconds = time_hours.getValue() * 3600;
        int minutes_in_seconds = time_minutes.getValue() * 60;
        int total_time_in_seconds = time_seconds.getValue() + minutes_in_seconds + hours_in_seconds;
        newGame.setRound_time(total_time_in_seconds);

        //reset round time
        if (check_new_game_reset_time.isChecked())
            newGame.setReset_round_time(1);
        else
            newGame.setReset_round_time(0);

        //round time delta
        if (check_new_game_delta.isChecked()){
            int delta_hours_in_seconds = delta_hours.getValue() * 3600;
            int delta_minutes_in_seconds = delta_minutes.getValue() * 60;
            int total_delta_in_seconds = delta_seconds.getValue() + delta_hours_in_seconds + delta_minutes_in_seconds;

            newGame.setRound_time_delta(total_delta_in_seconds);
        }

        //game mode
        newGame.setGame_mode(game_mode.getSelectedItemPosition());

        ((MainActivity) activity).setGame(newGame);
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
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }


}
