package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;
import privacyfriendlyexample.org.secuso.boardgameclock.view.OnSwipeTouchListener;

public class GameFragment extends Fragment {

    Activity activity;
    View rootView;
    private Game game;
    private HashMap<Long, Long> playerRoundTimes;
    private HashMap<Long, Long> playerRounds;
    private List<Player> players;

    private Player currentPlayer;
    private int nextPlayerIndex;
    private int startPlayerIndex;
    private long currentRoundTimeMs;
    private long currentGameTimeMs;
    private long roundChronoTimeWhenStopped = 0;
    private long gameChronoTimeWhenStopped = 0;

    private Chronometer roundChrono, gameChrono;
    private Button playPauseButton;

    private Button nextPlayerButton, saveGameButton;

    private TextView currentPlayerTv;
    private TextView currentPlayerRound;
    private ImageView currentPlayerIcon;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();

        rootView = inflater.inflate(R.layout.fragment_game, container, false);
        container.removeAllViews();

        playPauseButton = (Button) rootView.findViewById(R.id.gamePlayPauseButton);

        game = ((MainActivity) activity).getGame();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(game.getName());

        nextPlayerIndex = game.getNextPlayerIndex();
        startPlayerIndex = game.getStartPlayerIndex();

        players = game.getPlayers();
        playerRoundTimes = game.getPlayer_round_times();
        playerRounds = game.getPlayer_rounds();

        currentPlayer = players.get(startPlayerIndex);

        currentPlayerTv = (TextView) rootView.findViewById(R.id.game_current_player_name);
        currentPlayerTv.setText(currentPlayer.getName());

        currentPlayerRound = (TextView) rootView.findViewById(R.id.game_current_player_round);
        currentPlayerRound.setText("Round: " + playerRounds.get(currentPlayer.getId()).toString());

        currentPlayerIcon = (ImageView) rootView.findViewById(R.id.imageViewIcon);
        currentPlayerIcon.setImageURI(Uri.parse(currentPlayer.getPhotoUri()));

        currentRoundTimeMs = playerRoundTimes.get(currentPlayer.getId()) * 1000;
        currentGameTimeMs = game.getCurrentGameTime() * 1000;

        initChronometers();

        nextPlayerButton = (Button) rootView.findViewById(R.id.nextPlayerButton);
        nextPlayerButton.setOnClickListener(nextPlayer);

        saveGameButton = (Button) rootView.findViewById(R.id.saveGameButton);
        saveGameButton.setOnClickListener(saveGame);

        playPauseButton.setText("PLAY");
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                roundChrono.setBase(SystemClock.elapsedRealtime() + 1000);
                gameChrono.setBase(SystemClock.elapsedRealtime());
                roundChrono.start();
                gameChrono.start();
                playPauseButton.setText("PAUSE");
                playPauseButton.setOnClickListener(pause);

                nextPlayerButton.setVisibility(View.VISIBLE);

                rootView.setOnTouchListener(new OnSwipeTouchListener(activity.getBaseContext()) {
                    @Override
                    public void onSwipeLeft() {
                        nextPlayerButton.callOnClick();
                    }

                    @Override
                    public void onSwipeRight() {
                        nextPlayerButton.callOnClick();
                    }
                });
            }
        });

        return rootView;
    }

    private void initChronometers() {

        roundChrono = (Chronometer) rootView.findViewById(R.id.round_chrono);
        gameChrono = (Chronometer) rootView.findViewById(R.id.game_chrono);

        // init round time chronometer
        String round_time_hh = getTimeStrings(currentRoundTimeMs)[0];
        String round_time_mm = getTimeStrings(currentRoundTimeMs)[1];
        String round_time_ss = getTimeStrings(currentRoundTimeMs)[2];
        roundChrono.setFormat(round_time_hh + ":" + round_time_mm + ":" + round_time_ss);
        roundChrono.setOnChronometerTickListener(roundChronoTicker);

        // init game time chronometer
        String game_time_hh = getTimeStrings(currentGameTimeMs)[0];
        String game_time_mm = getTimeStrings(currentGameTimeMs)[1];
        String game_time_ss = getTimeStrings(currentGameTimeMs)[2];
        gameChrono.setFormat(game_time_hh + ":" + game_time_mm + ":" + game_time_ss);
        gameChrono.setOnChronometerTickListener(gameChronoTicker);

        roundChrono.setBase(SystemClock.elapsedRealtime());
        gameChrono.setBase(SystemClock.elapsedRealtime());
    }

    private void refreshRoundChronometer(){
        roundChrono = (Chronometer) rootView.findViewById(R.id.round_chrono);

        // init round time chronometer
        String round_time_hh = getTimeStrings(currentRoundTimeMs)[0];
        String round_time_mm = getTimeStrings(currentRoundTimeMs)[1];
        String round_time_ss = getTimeStrings(currentRoundTimeMs)[2];
        roundChrono.setFormat(round_time_hh + ":" + round_time_mm + ":" + round_time_ss);
        roundChrono.setOnChronometerTickListener(roundChronoTicker);

        roundChrono.setBase(SystemClock.elapsedRealtime());
    }

    View.OnClickListener saveGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Save Game")
                    .setMessage("Are you sure you want to save the current game?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            playerRoundTimes.put(currentPlayer.getId(), (currentRoundTimeMs + roundChronoTimeWhenStopped) / 1000);
                            playerRounds.put(currentPlayer.getId(), playerRounds.get(currentPlayer.getId()));

                            game.setPlayer_round_times(playerRoundTimes);
                            game.setPlayer_rounds(playerRounds);
                            game.setNextPlayerIndex(nextPlayerIndex);
                            game.setStartPlayerIndex(startPlayerIndex);
                            game.setCurrentGameTime((currentGameTimeMs + gameChronoTimeWhenStopped) / 1000);
                            game.setSaved(1);

                            GamesDataSource gds = new GamesDataSource(getActivity());
                            gds.open();
                            gds.saveGame(game);
                            gds.close();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    };

    Chronometer.OnChronometerTickListener roundChronoTicker = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer cArg) {

            long time_ms = cArg.getBase() + currentRoundTimeMs - SystemClock.elapsedRealtime();
            String hh = getTimeStrings(time_ms)[0];
            String mm = getTimeStrings(time_ms)[1];
            String ss = getTimeStrings(time_ms)[2];
            cArg.setText(hh + ":" + mm + ":" + ss);

            if (hh.equals("00") && mm.equals("00") &&
                    (ss.equals("05") || ss.equals("04") || ss.equals("03") || ss.equals("02") || ss.equals("01") || ss.equals("00")))
                roundChrono.setTextColor(Color.RED);
            else
                roundChrono.setTextColor(getResources().getColor(R.color.darkblue));

            if (hh.equals("00") && mm.equals("00") && ss.equals("00")) {
                if (game.getReset_round_time() == 0) {
                    roundChrono.stop();
                    gameChrono.stop();
                    nextPlayerButton.setVisibility(View.GONE);
                    saveGameButton.setVisibility(View.GONE);
                    playPauseButton.setText("Show Results");
                    playPauseButton.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity(), "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show();

                        }

                    });
                } else {
                    nextPlayerButton.performClick();
                }
            }
        }
    };

    Chronometer.OnChronometerTickListener gameChronoTicker = new Chronometer.OnChronometerTickListener() {
        @Override
        public void onChronometerTick(Chronometer cArg) {

            long time_ms = cArg.getBase() + currentGameTimeMs - SystemClock.elapsedRealtime();
            String hh = getTimeStrings(time_ms)[0];
            String mm = getTimeStrings(time_ms)[1];
            String ss = getTimeStrings(time_ms)[2];
            cArg.setText(hh + ":" + mm + ":" + ss);

            if (hh.equals("00") && mm.equals("00") &&
                    (ss.equals("05") || ss.equals("04") || ss.equals("03") || ss.equals("02") || ss.equals("01") || ss.equals("00")))
                gameChrono.setTextColor(Color.RED);
            else
                gameChrono.setTextColor(getResources().getColor(R.color.darkblue));

            if (hh.equals("00") && mm.equals("00") && ss.equals("00")) {
                roundChrono.stop();
                gameChrono.stop();

                nextPlayerButton.setVisibility(View.INVISIBLE);
                playPauseButton.setText("Show Results");
                playPauseButton.setOnClickListener(null);
            }
        }
    };


    private String[] getTimeStrings(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        return new String[]{hh, mm, ss};
    }

    View.OnClickListener pause = new View.OnClickListener() {
        public void onClick(View v) {

            roundChronoTimeWhenStopped = roundChrono.getBase() - SystemClock.elapsedRealtime();
            gameChronoTimeWhenStopped = gameChrono.getBase() - SystemClock.elapsedRealtime();

            nextPlayerButton.setVisibility(View.INVISIBLE);
            rootView.setOnTouchListener(null);

            roundChrono.stop();
            gameChrono.stop();

            saveGameButton.setVisibility(View.VISIBLE);

            playPauseButton.setText("RESUME");
            playPauseButton.setOnClickListener(run);

        }
    };

    View.OnClickListener run = new View.OnClickListener() {
        public void onClick(View v) {

            roundChrono.setBase(SystemClock.elapsedRealtime() + roundChronoTimeWhenStopped);
            gameChrono.setBase(SystemClock.elapsedRealtime() + gameChronoTimeWhenStopped);

            nextPlayerButton.setVisibility(View.VISIBLE);
            saveGameButton.setVisibility(View.GONE);

            rootView.setOnTouchListener(new OnSwipeTouchListener(activity.getBaseContext()) {
                @Override
                public void onSwipeLeft() {
                    nextPlayerButton.callOnClick();
                }

                @Override
                public void onSwipeRight() {
                    nextPlayerButton.callOnClick();
                }
            });

            roundChrono.start();
            gameChrono.start();

            playPauseButton.setText("PAUSE");
            playPauseButton.setOnClickListener(pause);
        }
    };

    View.OnClickListener nextPlayer = new View.OnClickListener() {
        public void onClick(View v) {

            long currPlayerId = currentPlayer.getId();
            System.err.println(currPlayerId);
            playerRoundTimes.put(currPlayerId, (currentRoundTimeMs + roundChronoTimeWhenStopped) / 1000);

            roundChrono.setBase(SystemClock.elapsedRealtime());
            playPauseButton.performClick();

            playerRounds.put(currPlayerId, playerRounds.get(currPlayerId) + 1);
            startPlayerIndex = nextPlayerIndex;

            if (game.getGame_mode() == 0) {
                currentPlayer = players.get(nextPlayerIndex);
                nextPlayerIndex = (nextPlayerIndex + 1) % players.size();
            } else if (game.getGame_mode() == 1) {
                currentPlayer = players.get(nextPlayerIndex);
                if (nextPlayerIndex <= 0)
                    nextPlayerIndex = players.size() - 1;
                else
                    nextPlayerIndex = nextPlayerIndex - 1;
            } else if (game.getGame_mode() == 2) {
                currentPlayer = players.get(nextPlayerIndex);

                int randomPlayerIndex = new Random().nextInt(players.size());
                while (randomPlayerIndex == nextPlayerIndex)
                    randomPlayerIndex = new Random().nextInt(players.size());

                nextPlayerIndex = randomPlayerIndex;
            }

            currPlayerId = currentPlayer.getId();

            if (game.getReset_round_time() == 1) {
                currentRoundTimeMs = game.getRound_time() * 1000;
            } else {
                currentRoundTimeMs = playerRoundTimes.get(currPlayerId) * 1000;
            }

            if ((game.getRound_time_delta() != -1) && (playerRounds.get(currPlayerId) > 1))
                currentRoundTimeMs += game.getRound_time_delta() * 1000 * (playerRounds.get(currPlayerId) - 1);

            currentPlayerTv.setText(currentPlayer.getName());
            currentPlayerRound.setText("Round: " + playerRounds.get(currentPlayer.getId()).toString());
            currentPlayerIcon.setImageURI(Uri.parse(currentPlayer.getPhotoUri()));

            refreshRoundChronometer();
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
