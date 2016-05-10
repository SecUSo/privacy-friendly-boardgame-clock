package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
    MainActivity mainActivity;
    View rootView;
    private Game game;
    private HashMap<Long, Long> playerRoundTimes;
    private HashMap<Long, Long> playerRounds;
    private List<Player> players;

    private Player currentPlayer;

    private CountDownTimer roundTimer, gameTimer;
    private Button playPauseButton;
    private Button nextPlayerButton;

    private LinearLayout gameButtons;

    private long currentRoundTimeMs, currentGameTimeMs, totalTimePlayed = 0;
    private int nextPlayerIndex, startPlayerIndex;

    private TextView currentPlayerTv;
    private TextView currentPlayerRound;
    private ImageView currentPlayerIcon;

    private TextView gameTimerTv, roundTimerTv;

    private boolean alreadySaved = true;
    private boolean alreadyPaused = false;
    private int isFinished = 0;
    private boolean isPaused = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        mainActivity = ((MainActivity) activity);

        // prevent phone from sleeping while game is running
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rootView = inflater.inflate(R.layout.fragment_game, container, false);
        container.removeAllViews();

        playPauseButton = (Button) rootView.findViewById(R.id.gamePlayPauseButton);

        gameTimerTv = (TextView) rootView.findViewById(R.id.game_timer);
        roundTimerTv = (TextView) rootView.findViewById(R.id.round_timer);

        game = ((MainActivity) activity).getGame();
        ((AppCompatActivity) activity).getSupportActionBar().setSubtitle(game.getName());

        players = game.getPlayers();
        playerRoundTimes = game.getPlayer_round_times();
        playerRounds = game.getPlayer_rounds();

        startPlayerIndex = game.getStartPlayerIndex();
        nextPlayerIndex = game.getNextPlayerIndex();

        currentPlayer = players.get(game.getStartPlayerIndex());

        playersQueue = getPlayersNotInRound(playerRounds.get(currentPlayer.getId()));

        currentPlayerTv = (TextView) rootView.findViewById(R.id.game_current_player_name);
        currentPlayerTv.setText(currentPlayer.getName());

        currentPlayerRound = (TextView) rootView.findViewById(R.id.game_current_player_round);
        currentPlayerRound.setText(playerRounds.get(currentPlayer.getId()).toString());

        currentPlayerIcon = (ImageView) rootView.findViewById(R.id.imageViewIcon);
        currentPlayerIcon.setImageBitmap(currentPlayer.getIcon());

        currentRoundTimeMs = playerRoundTimes.get(currentPlayer.getId()) * 1000;
        currentGameTimeMs = game.getCurrentGameTime() * 1000;

        initTimerTextViews();
        initChronometers();

        nextPlayerButton = (Button) rootView.findViewById(R.id.nextPlayerButton);
        nextPlayerButton.setOnClickListener(nextPlayer);

        gameButtons = (LinearLayout) rootView.findViewById(R.id.gameButtons);

        Button saveGameButton = (Button) rootView.findViewById(R.id.saveGameButton);
        saveGameButton.setOnClickListener(saveGame);

        Button finishGameButton = (Button) rootView.findViewById(R.id.finishGameButton);
        finishGameButton.setOnClickListener(finishGame);

        playPauseButton.setText(R.string.play_capslock);
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alreadySaved = false;
                isPaused = false;

                roundTimer.start();
                gameTimer.start();

                playPauseButton.setText(R.string.pause_capslock);
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

    private void initTimerTextViews() {
        String round_time_hh = getTimeStrings(currentRoundTimeMs)[0];
        String round_time_mm = getTimeStrings(currentRoundTimeMs)[1];
        String round_time_ss = getTimeStrings(currentRoundTimeMs)[2];
        roundTimerTv.setText(round_time_hh + ":" + round_time_mm + ":" + round_time_ss);

        String game_time_hh = getTimeStrings(currentGameTimeMs)[0];
        String game_time_mm = getTimeStrings(currentGameTimeMs)[1];
        String game_time_ss = getTimeStrings(currentGameTimeMs)[2];
        gameTimerTv.setText(game_time_hh + ":" + game_time_mm + ":" + game_time_ss);
    }

    private void initChronometers() {

        // init round time chronometer
        roundTimer = new CountDownTimer(currentRoundTimeMs, 100) {

            public void onTick(long millisUntilFinished) {
                String round_time_hh = getTimeStrings(millisUntilFinished)[0];
                String round_time_mm = getTimeStrings(millisUntilFinished)[1];
                String round_time_ss = getTimeStrings(millisUntilFinished)[2];
                roundTimerTv.setText(round_time_hh + ":" + round_time_mm + ":" + round_time_ss);

                if (millisUntilFinished <= 5800)
                    roundTimerTv.setTextColor(Color.RED);

                currentRoundTimeMs = millisUntilFinished;

                if (mainActivity.isDrawerOpened() && !alreadyPaused){
                    alreadyPaused = true;
                    playPauseButton.performClick();
                }
                else
                    alreadyPaused = false;

                updateGame();
            }

            public void onFinish() {
                if (game.getReset_round_time() == 1 && currentGameTimeMs >= 500)
                    nextPlayerButton.performClick();
                else
                    finishGame();
            }

        };

        // init game time chronometer
        gameTimer = new CountDownTimer(currentGameTimeMs, 100) {

            public void onTick(long millisUntilFinished) {
                String game_time_hh = getTimeStrings(millisUntilFinished)[0];
                String game_time_mm = getTimeStrings(millisUntilFinished)[1];
                String game_time_ss = getTimeStrings(millisUntilFinished)[2];
                gameTimerTv.setText(game_time_hh + ":" + game_time_mm + ":" + game_time_ss);

                if (millisUntilFinished <= 5800)
                    gameTimerTv.setTextColor(Color.RED);

                currentGameTimeMs = millisUntilFinished;

                updateGame();
            }

            public void onFinish() {
                finishGame();
            }
        };
    }

    View.OnClickListener saveGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.saveGame)
                    .setMessage(R.string.sureToSaveGameQuestion)
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveGameToDb(1);
                            alreadySaved = true;

                            Toast.makeText(activity, R.string.gameSavedSuccess, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    };

    private void updateGame() {
        playerRoundTimes.put(currentPlayer.getId(), currentRoundTimeMs / 1000);
        playerRounds.put(currentPlayer.getId(), playerRounds.get(currentPlayer.getId()));

        game.setPlayer_round_times(playerRoundTimes);
        game.setPlayer_rounds(playerRounds);
        game.setNextPlayerIndex(nextPlayerIndex);
        game.setStartPlayerIndex(startPlayerIndex);
        game.setCurrentGameTime(currentGameTimeMs / 1000);
        game.setFinished(isFinished);

        mainActivity.setGame(game);
    }

    private void saveGameToDb(int save){
        game.setSaved(save);

        GamesDataSource gds = new GamesDataSource(activity);
        gds.open();
        gds.saveGame(game);
        gds.close();
    }

    private void finishGame() {
        isFinished = 1;
        game.setFinished(isFinished);

        ((MainActivity) activity).setHistoryGame(game);

        roundTimer.cancel();
        gameTimer.cancel();

        nextPlayerButton.setVisibility(View.GONE);
        gameButtons.setVisibility(View.GONE);
        playPauseButton.setText(R.string.showResults);
        playPauseButton.setOnClickListener(showGameResults);

        saveGameToDb(0);

    }

    private View.OnClickListener showGameResults = new View.OnClickListener(){
        @Override
        public void onClick(View v) {

            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new GameResultsFragment());
            fragmentTransaction.addToBackStack(activity.getString(R.string.gameResultsFragment));
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();

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

    View.OnClickListener finishGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.finishGame)
                    .setMessage(R.string.finishGameQuestion)
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            finishGame();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    };

    View.OnClickListener pause = new View.OnClickListener() {
        public void onClick(View v) {
            nextPlayerButton.setVisibility(View.GONE);
            rootView.setOnTouchListener(null);

            isPaused = true;

            roundTimer.cancel();
            gameTimer.cancel();

            gameButtons.setVisibility(View.VISIBLE);

            playPauseButton.setText(R.string.resume_capslock);
            playPauseButton.setOnClickListener(run);

        }
    };

    View.OnClickListener run = new View.OnClickListener() {
        public void onClick(View v) {
            alreadySaved = false;

            nextPlayerButton.setVisibility(View.VISIBLE);
            gameButtons.setVisibility(View.GONE);

            isPaused = false;

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

            initChronometers();
            roundTimer.start();
            gameTimer.start();

            playPauseButton.setText(R.string.pause_capslock);
            playPauseButton.setOnClickListener(pause);
        }
    };

    List<Player> playersQueue;

    private List<Player> getPlayersNotInRound(long round) {
        List<Player> retPlayers = new ArrayList<>();

        for (int i = 0; i < players.size(); i++)
            if (playerRounds.get(players.get(i).getId()) != round)
                retPlayers.add(players.get(i));

        return retPlayers;
    }

    View.OnClickListener nextPlayer = new View.OnClickListener() {
        public void onClick(View v) {

            playPauseButton.performClick();

            long currPlayerId = currentPlayer.getId();
            playerRoundTimes.put(currPlayerId, currentRoundTimeMs / 1000);

            long nextPlayerRound = playerRounds.get(currPlayerId) + 1;

            playerRounds.put(currPlayerId, nextPlayerRound);
            startPlayerIndex = nextPlayerIndex;

            if (game.getGame_mode() == 0 || game.getGame_mode() == 3) {
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

                playersQueue = getPlayersNotInRound(nextPlayerRound);
                playersQueue.remove(currentPlayer);

                if (playersQueue.size() == 0)
                    for (Player p : players)
                        playersQueue.add(p);

                playersQueue.remove(currentPlayer);

                int r = new Random().nextInt(playersQueue.size());

                nextPlayerIndex = players.indexOf(playersQueue.get(r));


            }

            currPlayerId = currentPlayer.getId();

            if (game.getReset_round_time() == 1) {
                currentRoundTimeMs = game.getRound_time() * 1000;

                if ((game.getRound_time_delta() != -1) && (playerRounds.get(currPlayerId) > 1))
                    currentRoundTimeMs += game.getRound_time_delta() * 1000 * (playerRounds.get(currPlayerId) - 1);

            } else {
                currentRoundTimeMs = playerRoundTimes.get(currPlayerId) * 1000;

                if ((game.getRound_time_delta() != -1) && (playerRounds.get(currPlayerId) > 1))
                    currentRoundTimeMs += game.getRound_time_delta() * 1000;
            }

            currentPlayerTv.setText(currentPlayer.getName());
            currentPlayerRound.setText(playerRounds.get(currentPlayer.getId()).toString());
            currentPlayerIcon.setImageBitmap(currentPlayer.getIcon());

            initTimerTextViews();
            initChronometers();
        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setKeyListenerOnView(View v) {
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    if (!isPaused && (isFinished == 0))
                        playPauseButton.performClick();

                    String dialogTitle;
                    String dialogQuestion;

                    if (isFinished == 1) {
                        dialogTitle = getString(R.string.backToMainMenu);
                        dialogQuestion = getString(R.string.backToMainMenuQuestion);
                    } else {
                        dialogTitle = getString(R.string.quitGame);
                        dialogQuestion = getString(R.string.leaveGameQuestion);
                    }

                    new AlertDialog.Builder(activity)
                            .setTitle(dialogTitle)
                            .setMessage(dialogQuestion)
                            .setIcon(android.R.drawable.ic_menu_help)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if ((isFinished == 0) && !alreadySaved)
                                        new AlertDialog.Builder(activity)
                                                .setTitle(R.string.quitGame)
                                                .setMessage(R.string.quitGameQuestion)
                                                .setIcon(android.R.drawable.ic_menu_help)
                                                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        saveGameToDb(1);
                                                        showMainMenu();
                                                    }
                                                })
                                                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        showMainMenu();
                                                    }
                                                })
                                                .show();
                                    else
                                        showMainMenu();

                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();

                    return true;
                } else
                    return false;
            }
        });
    }

    private void showMainMenu() {
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(null);

        getFragmentManager().popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainMenuFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        setKeyListenerOnView(getView());
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity){
            a=(Activity) context;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
