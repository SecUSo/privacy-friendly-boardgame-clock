package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

/**
 * Created by yonjuni on 12.01.16.
 */
public class GameFragment extends Fragment {

    Activity activity;
    private Game game;
    private Player currentPlayer;
    private boolean isPaused = true;
    private boolean isFinished = false;

    private Chronometer mChronometer;
    private long timeWhenStopped = 0;
    private Button playPauseButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();

        View rootView = inflater.inflate(R.layout.fragment_game, container, false);
        container.removeAllViews();

        playPauseButton = (Button) rootView.findViewById(R.id.gamePlayPauseButton);

        game = ((MainActivity) activity).getGame();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(game.getName());

        if (game.getGame_mode() != 2)
            currentPlayer = game.getPlayers().get(0);
        else
            currentPlayer = game.getPlayers().get(new Random().nextInt(game.getPlayers().size()));

        TextView tv = (TextView) rootView.findViewById(R.id.game_current_player_name);
        tv.setText(currentPlayer.getName());

        ImageView iv = (ImageView) rootView.findViewById(R.id.imageViewIcon);
        iv.setImageURI(Uri.parse(currentPlayer.getPhotoUri()));

        final long round_time_milliseconds = game.getRound_time() * 1000;

        // init time text for chronometer
        long time = round_time_milliseconds;
        int h = (int) (time / 3600000);
        int m = (int) (time - h * 3600000) / 60000;
        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        // init chronometer
        mChronometer = (Chronometer) rootView.findViewById(R.id.chronometer);

        mChronometer.setFormat(hh + ":" + mm + ":" + ss);

        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {

                long time = cArg.getBase() + round_time_milliseconds - SystemClock.elapsedRealtime();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);

                if (hh.equals("00") && mm.equals("00") && ss.equals("00")) {
                    mChronometer.stop();
                    isFinished = true;
                    playPauseButton.setText("Show Results");
                    playPauseButton.setOnClickListener(null);
                }
            }
        });
        mChronometer.setBase(SystemClock.elapsedRealtime());

        // Watch for button clicks.
        playPauseButton.setText("PLAY");
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mChronometer.setBase(SystemClock.elapsedRealtime());

                mChronometer.start();
                playPauseButton.setText("PAUSE");
                playPauseButton.setOnClickListener(pause);
                isPaused = false;
            }
        });
        return rootView;
    }

    View.OnClickListener pause = new View.OnClickListener() {
        public void onClick(View v) {
            timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
            playPauseButton.setText("PLAY");
            playPauseButton.setOnClickListener(run);
        }
    };

    View.OnClickListener run = new View.OnClickListener() {
        public void onClick(View v) {
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            mChronometer.start();
            playPauseButton.setText("PAUSE");
            playPauseButton.setOnClickListener(pause);
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

}
