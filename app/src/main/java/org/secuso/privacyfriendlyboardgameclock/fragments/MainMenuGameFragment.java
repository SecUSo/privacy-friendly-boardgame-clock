package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.OnSwipeTouchListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.SelectPlayerListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.services.CountdownTimerService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Quang Anh Dang on 17.12.2017.
 *
 * @author Quang Anh Dang
 */
public class MainMenuGameFragment extends Fragment {
    // TODO #6 I/Choreographer: Skipped 3165 frames!  The application may be doing too much work on its main thread.
    Activity activity;
    MainActivity mainActivity;
    View rootView;
    private Game game;
    private HashMap<Long, Long> playerRoundTimes;
    // which round number will the player be in the next time he has turn
    private HashMap<Long, Long> playerRounds;
    private List<Player> players;

    private Player currentPlayer;

    private Button playPauseButton;
    private Button nextPlayerButton;
    private ImageButton finishGameButton, saveGameButton;

    private long currentRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
    private long currentGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
    private long currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
    private long currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
    private int nextPlayerIndex, currentPlayerIndex;

    private TextView currentPlayerTv;
    private TextView currentPlayerRound;
    private ImageView currentPlayerIcon;

    private TextView gameTimerTv, roundTimerTv;

    private boolean alreadySaved = true;
    private boolean alreadyPaused = false;
    private int isFinished = 0;
    private int isLastRound = 0;
    private boolean isPaused = true;

    private List<Player> playersQueue;

    private CountdownTimerService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((CountdownTimerService.LocalBinder)service).getService();
            Log.i("MainMenuGameFragment", "Service Connected.");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.i("ServiceDisconnected", "Service Disconnected.");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        activity.bindService(new Intent(activity,
                CountdownTimerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            activity.unbindService(mConnection);
            mIsBound = false;
        }
    }

    private BroadcastReceiver br;


    private GamesDataSourceSingleton gds;
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
    private long gameTime;
    private View.OnClickListener showGameResults = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuGameResultFragment());
            fragmentTransaction.addToBackStack(activity.getString(R.string.gameResultsFragment));
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            fragmentTransaction.commit();

        }
    };

    private View.OnClickListener run = new View.OnClickListener() {
        public void onClick(View v) {
            alreadySaved = false;

            saveGameButton.setVisibility(View.GONE);
            finishGameButton.setVisibility(View.GONE);

            isPaused = false;

            updateAndResumeTimer();

            playPauseButton.setText(R.string.pause_capslock);
            playPauseButton.setOnClickListener(pause);
        }
    };

    private View.OnClickListener pause = new View.OnClickListener() {
        public void onClick(View v) {
            isPaused = true;

            mBoundService.pauseTimer();
            gameTime = currentGameTimeMs;

            saveGameButton.setVisibility(View.VISIBLE);
            finishGameButton.setVisibility(View.VISIBLE);

            playPauseButton.setText(R.string.resume);
            playPauseButton.setOnClickListener(run);

            updateTimerTextViews();

        }
    };
    private View.OnClickListener nextPlayer = new View.OnClickListener() {
        public void onClick(View v) {
            currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
            // save current player data
            long currPlayerId = currentPlayer.getId();
            long nextPlayerRound = playerRounds.get(currPlayerId) + 1;

            // just put the time current player has left in to the list
            playerRoundTimes.put(currPlayerId, currentRoundTimeMs);
            // update the round number for current player
            playerRounds.put(currPlayerId, nextPlayerRound);

            if (game.getIsLastRound() == 1 && getPlayersNotInRound(nextPlayerRound).size() == 0) {
                nextPlayerRound -= 1;
                playerRounds.put(currPlayerId, nextPlayerRound);
                finishGame();
                return;
            } else if (!isPaused) {
                if (game.getChess_mode() == 1)
                    mBoundService.pauseTimer();
                else // update view by clicking pause button
                    playPauseButton.performClick();

                gameTime = currentGameTimeMs;
            }


            // determine next player
            if (game.getGame_mode() == 0) {
                nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } else if (game.getGame_mode() == 1) {
                if (currentPlayerIndex == 0)
                    nextPlayerIndex = players.size() - 1;
                else
                    nextPlayerIndex = currentPlayerIndex - 1;
            } else if (game.getGame_mode() == 2) {
                playersQueue = getPlayersNotInRound(nextPlayerRound);
                playersQueue.remove(currentPlayer);

                if (playersQueue.size() == 0)
                    for (Player p : players)
                        playersQueue.add(p);

                playersQueue.remove(currentPlayer);

                int r = new Random().nextInt(playersQueue.size());

                nextPlayerIndex = players.indexOf(playersQueue.get(r));
            } else if (game.getGame_mode() == 3) {
                if (getPlayersNotInRound(nextPlayerRound).size() == 0) {
                    // hide player
                    currentPlayerTv.setVisibility(View.INVISIBLE);
                    currentPlayerRound.setVisibility(View.INVISIBLE);
                    currentPlayerIcon.setVisibility(View.INVISIBLE);
                    roundTimerTv.setVisibility(View.INVISIBLE);

                    final ArrayList<Player> selectedPlayers = new ArrayList<>();

                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    View dialogView = activity.getLayoutInflater().inflate(R.layout.dialog_set_player_sequence, null);
                    alertDialog.setView(dialogView);
                    alertDialog.setTitle(R.string.manualChoiceHeading);
                    alertDialog.setCancelable(false);

                    final ListView myListView = (ListView) dialogView.findViewById(R.id.set_player_sequence_list);
                    SelectPlayerListAdapter listAdapter = new SelectPlayerListAdapter(getActivity(), R.id.set_player_sequence_list, players);
                    myListView.setAdapter(listAdapter);
                    myListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                            TextView tv = (TextView) v.findViewById(R.id.textViewNumber);
                            if (tv.getText() == "" && myListView.getCheckedItemCount() > 0) {
                                selectedPlayers.add((Player) adapter.getItemAtPosition(position));
                                tv.setText(selectedPlayers.indexOf((Player) adapter.getItemAtPosition(position)) + 1 + ".");
                            } else {
                                int deletedNumber = selectedPlayers.indexOf(adapter.getItemAtPosition(position)) + 1;
                                selectedPlayers.remove(adapter.getItemAtPosition(position));
                                tv.setText("");

                                ListView playersList = (ListView) v.getParent();
                                SparseBooleanArray checked = playersList.getCheckedItemPositions();
                                int size = checked.size();
                                for (int i = 0; i < size; i++) {
                                    int key = checked.keyAt(i);
                                    boolean checkedValue = checked.get(key);
                                    if (checkedValue) {
                                        TextView number = (TextView) playersList.getChildAt(key).findViewById(R.id.textViewNumber);
                                        String numberText = number.getText().toString();
                                        int indexDot = numberText.indexOf(".");
                                        if (indexDot != -1) {
                                            int value = Integer.valueOf(numberText.substring(0, indexDot));
                                            if (value > deletedNumber) {
                                                value--;
                                                number.setText(value + ".");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    });
                    final AlertDialog ad = alertDialog.show();

                    final Button resumeButton = (Button) dialogView.findViewById(R.id.setPlayerSequenceButton);
                    resumeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (players.size() != selectedPlayers.size()) {
                                new AlertDialog.Builder(activity)
                                        .setTitle(R.string.error)
                                        .setMessage(R.string.manualChoiceError)
                                        .setIcon(android.R.drawable.ic_menu_info_details)
                                        .setPositiveButton(R.string.ok, null)
                                        .show();
                            } else {
                                players = selectedPlayers;
                                game.setPlayers(players);

                                // unhide player
                                currentPlayerTv.setVisibility(View.VISIBLE);
                                currentPlayerRound.setVisibility(View.VISIBLE);
                                currentPlayerIcon.setVisibility(View.VISIBLE);
                                roundTimerTv.setVisibility(View.VISIBLE);

                                nextPlayerIndex = 0;
                                currentPlayerIndex = nextPlayerIndex;
                                currentPlayer = players.get(nextPlayerIndex);
                                restorePlayerData(currentPlayer.getId());
                                updateViews();

                                if (!isPaused && game.getChess_mode() == 1) {
                                    updateAndResumeTimer();
                                }

                                updateGame();

                                ad.dismiss();
                            }
                        }
                    });
                } else {
                    nextPlayerIndex = (currentPlayerIndex + 1) % players.size();

                    // set next player to current player
                    currentPlayerIndex = nextPlayerIndex;
                    currentPlayer = players.get(nextPlayerIndex);
                    restorePlayerData(currentPlayer.getId());
                    updateViews();

                    if (!isPaused && game.getChess_mode() == 1) {
                        updateAndResumeTimer();
                    }

                    updateGame();

                }
            }

            if (game.getGame_mode() != 3) {
                // set next player to current player
                currentPlayerIndex = nextPlayerIndex;
                currentPlayer = players.get(nextPlayerIndex);
                restorePlayerData(currentPlayer.getId());
                updateViews();

                if (!isPaused && game.getChess_mode() == 1) {
                    updateAndResumeTimer();
                }

                updateGame();
            }
        }
    };
    private View.OnClickListener finishGame = new View.OnClickListener() {
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
    private View.OnClickListener wantToFinish = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // Option not reset Round Time, each player has only certain amount of time
            // if one player runs out of time --> ask if game ends or continue with other
            // remaining players
            if (getPlayersNotInRound(playerRounds.get(currentPlayer.getId()) + 1).size() - 1 > 0){
                new AlertDialog.Builder(activity)
                        .setTitle(R.string.roundTimeOverDialogHeading)
                        .setMessage(R.string.roundTimeOverDialogQuestion)
                        .setIcon(android.R.drawable.ic_menu_help)
                        .setPositiveButton(R.string.finishGame, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                isFinished = 1;
                                finishGame();
                            }
                        })
                        .setNegativeButton(R.string.resume, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                isLastRound = 1;
                                game.setIsLastRound(1);
                                playPauseButton.setOnClickListener(pause);
                                nextPlayerButton.setOnClickListener(nextPlayer);
                                nextPlayerButton.performClick();
                            }
                        })
                        .show();
            }
            else finishGame();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        mainActivity = ((MainActivity) activity);
        startTimerService();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGUI(intent); // or whatever method used to update your GUI fields
            }
        };
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        gds = GamesDataSourceSingleton.getInstance(activity);

        // prevent phone from sleeping while game is running
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rootView = inflater.inflate(R.layout.fragment_main_menu_game, container, false);
        container.removeAllViews();

        // show swipe dialog
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        boolean firstStart = settings.getBoolean("showSwipeDialog", true);
        if (firstStart) {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.swipeDialogQuestion)
                    .setMessage(R.string.swipeDialogAnswer)
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setPositiveButton(R.string.ok, null)
                    .show();

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("showSwipeDialog", false);
            editor.commit();
        }


        playPauseButton = (Button) rootView.findViewById(R.id.gamePlayPauseButton);
        finishGameButton = (ImageButton) rootView.findViewById(R.id.finishGameButton);
        saveGameButton = (ImageButton) rootView.findViewById(R.id.saveGameButton);

        gameTimerTv = (TextView) rootView.findViewById(R.id.game_timer);
        roundTimerTv = (TextView) rootView.findViewById(R.id.round_timer);

        game = ((MainActivity) activity).getGame();
        ((AppCompatActivity) activity).getSupportActionBar().setSubtitle(game.getName());

        players = game.getPlayers();
        playerRoundTimes = game.getPlayer_round_times();
        playerRounds = game.getPlayer_rounds();

        currentPlayerIndex = game.getStartPlayerIndex();
        nextPlayerIndex = game.getNextPlayerIndex();

        currentPlayer = players.get(game.getStartPlayerIndex());
        playersQueue = getPlayersNotInRound(playerRounds.get(currentPlayer.getId()));

        currentPlayerTv = (TextView) rootView.findViewById(R.id.game_current_player_name);
        currentPlayerTv.setText(currentPlayer.getName());

        currentPlayerRound = (TextView) rootView.findViewById(R.id.game_current_player_round);
        currentPlayerRound.setText(playerRounds.get(currentPlayer.getId()).toString());

        currentPlayerIcon = (ImageView) rootView.findViewById(R.id.imageViewIcon);
        currentPlayerIcon.setImageBitmap(currentPlayer.getIcon());

        currentRoundTimeMs = playerRoundTimes.get(currentPlayer.getId());
        currentGameTimeMs = game.getCurrentGameTime();
        gameTime = currentGameTimeMs;

        isLastRound = game.getIsLastRound();

        if (game.getGame_time_infinite() == 1) {
            gameTimerTv.setText(activity.getString(R.string.infinite));
        }

        updateTimerTextViews();

        nextPlayerButton = (Button) rootView.findViewById(R.id.nextPlayerButton);
        nextPlayerButton.setOnClickListener(nextPlayer);

        ImageButton saveGameButton = (ImageButton) rootView.findViewById(R.id.saveGameButton);
        saveGameButton.setOnClickListener(saveGame);

        ImageButton finishGameButton = (ImageButton) rootView.findViewById(R.id.finishGameButton);
        finishGameButton.setOnClickListener(finishGame);

        playPauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                alreadySaved = false;
                isPaused = false;

                mBoundService.initRoundCountdownTimer(currentRoundTimeMs);
                // if game time not infinit, init game timer
                if(game.getGame_time_infinite() == 0){
                    mBoundService.initGameCountdownTimer(currentGameTimeMs);
                    mBoundService.startGameTimer();
                }
                mBoundService.startRoundTimer();

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

    private void restorePlayerData(long currPlayerId) {
        // restore player data
        if (game.getReset_round_time() == 1) {

            currentRoundTimeMs = game.getRound_time();

            if ((game.getRound_time_delta() != -1) && (playerRounds.get(currPlayerId) > 1))
                currentRoundTimeMs += game.getRound_time_delta() * (playerRounds.get(currPlayerId) - 1);

        } else {
            currentRoundTimeMs = playerRoundTimes.get(currPlayerId);

            if ((game.getRound_time_delta() != -1) && (playerRounds.get(currPlayerId) > 1))
                currentRoundTimeMs += game.getRound_time_delta();
        }
    }

    private void updateViews() {
        updateTimer();

        // update view
        currentPlayerTv.setText(currentPlayer.getName());
        currentPlayerRound.setText(playerRounds.get(currentPlayer.getId()).toString());
        currentPlayerIcon.setImageBitmap(currentPlayer.getIcon());
        updateTimerTextViews();

    }

    private void updateTimerTextViews() {
        long roundTimeToUse, gameTimeToUse;
        String round_time_result = "";
        String game_time_result = "";

        if(currentExceedRoundTimeMs >= 0){
            roundTimeToUse = currentExceedRoundTimeMs;
            round_time_result = "-";
        }
        else roundTimeToUse = currentRoundTimeMs;

        if(currentExceedGameTimeMs >= 0){
            gameTimeToUse = currentExceedGameTimeMs;
            game_time_result = "-";
        }
        else gameTimeToUse = currentGameTimeMs;

        String round_time_hh = getTimeStrings(roundTimeToUse)[0];
        String round_time_mm = getTimeStrings(roundTimeToUse)[1];
        String round_time_ss = getTimeStrings(roundTimeToUse)[2];
        String round_time_ms = getTimeStrings(roundTimeToUse)[3];
        round_time_result = round_time_result + round_time_hh + ":" + round_time_mm + ":" + round_time_ss + "'" + round_time_ms;
        roundTimerTv.setText(round_time_result);

        // highlight low timers red colored
        if (currentRoundTimeMs <= 5000)
            roundTimerTv.setTextColor(Color.RED);
        else
            roundTimerTv.setTextColor(Color.BLACK);

        // if game time is not infinite
        if (game.getGame_time_infinite() == 0) {

            String game_time_hh = getTimeStrings(gameTimeToUse)[0];
            String game_time_mm = getTimeStrings(gameTimeToUse)[1];
            String game_time_ss = getTimeStrings(gameTimeToUse)[2];
            game_time_result = game_time_result + game_time_hh + ":" + game_time_mm + ":" + game_time_ss;
            gameTimerTv.setText(game_time_result);

            if (game.getGame_time_infinite() == 0 && currentGameTimeMs <= 5000)
                gameTimerTv.setTextColor(Color.RED);
            else
                gameTimerTv.setTextColor(Color.BLACK);
        }
    }

    private List<Player> getPlayersNotInRound(long round) {
        List<Player> retPlayers = new ArrayList<>();

        for (int i = 0; i < players.size(); i++)
            if (playerRounds.get(players.get(i).getId()) != round)
                retPlayers.add(players.get(i));

        return retPlayers;
    }


    private void showMainMenu() {
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(null);

        getFragmentManager().popBackStack(getString(R.string.mainMenuWelcomeFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.MainActivity_fragment_container, new MainMenuWelcomeFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuWelcomeFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }

    /**
     * update the current game object
     * called by saveGameToDb, by nextPlayer button,
     * (3)
     */
    public void updateGame() {
        playerRoundTimes.put(currentPlayer.getId(), currentRoundTimeMs);
        playerRounds.put(currentPlayer.getId(), playerRounds.get(currentPlayer.getId()));

        game.setPlayer_round_times(playerRoundTimes);
        game.setPlayer_rounds(playerRounds);
        game.setNextPlayerIndex(nextPlayerIndex);
        game.setStartPlayerIndex(currentPlayerIndex);
        if (game.getGame_time_infinite() == 0)
            game.setCurrentGameTime(currentGameTimeMs);
        game.setFinished(isFinished);
        game.setIsLastRound(isLastRound);

        mainActivity.setGame(game);
    }

    /**
     * save Game to DB is called when game is finished or save button is clicked
     * is called by finishGame, or when click saveGame Button
     * (2)
     * @param save 0 if game is finished, 1 if game is still on going
     */
    private void saveGameToDb(int save) {
        updateGame();
        game.setSaved(save);

        gds.saveGame(game);
    }

    /**
     * game is finished, set finish flag, update buttons and textview
     * and call save game to DB
     * (1)
     */
    private void finishGame() {
        isFinished = 1;
        game.setFinished(isFinished);
        updateGame();
        ((MainActivity) activity).setHistoryGame(game);

        stopTimerService();

        saveGameButton.setVisibility(View.GONE);
        finishGameButton.setVisibility(View.GONE);
        nextPlayerButton.setVisibility(View.GONE);
        playPauseButton.setText(R.string.showResults);
        playPauseButton.setOnClickListener(showGameResults);

        saveGameToDb(0);

    }


    /**
     *
     * @param time_ms time in milliseconds
     * @return a String Array list of 4 elements, hour, minutes, seconds and milliseconds
     */
    private String[] getTimeStrings(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;

        String ms = "0";
        try {
            ms = String.valueOf(String.valueOf(time_ms).charAt(String.valueOf(time_ms).length() - 3));
        } catch (StringIndexOutOfBoundsException e) {
        }
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        return new String[]{hh, mm, ss, ms};
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


    @Override
    public void onResume() {
        super.onResume();
        setKeyListenerOnView(getView());
        activity.registerReceiver(br, new IntentFilter(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG));
        Log.i("MainMenuGameFragment", "Registered Broadcast Receiver." );
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onPause() {
        super.onPause();
        unregisterRegister();
    }

    @Override
    public void onStop() {
        unregisterRegister();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopTimerService();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void startTimerService(){
        activity.startService(new Intent(activity, CountdownTimerService.class));
        doBindService();
    }

    private void stopTimerService(){
        unregisterRegister();
        doUnbindService();
        activity.stopService(new Intent(activity,CountdownTimerService.class));
    }

    private void unregisterRegister(){
        try{
            activity.unregisterReceiver(br);
        }catch (Exception e){
        }
        Log.i("MainMenuGameFragment", "Unregistered Broadcast Receiver." );
    }

    private void updateAndResumeTimer(){
        mBoundService.setCurrentRoundTimeMs(currentRoundTimeMs);
        if(game.getGame_time_infinite() == 0){
            mBoundService.setCurrentGameTimeMs(currentGameTimeMs);
        }
        mBoundService.resumeTimer();
    }

    private void updateTimer(){
        mBoundService.setCurrentRoundTimeMs(currentRoundTimeMs);
        if(game.getGame_time_infinite() == 0){
            mBoundService.setCurrentGameTimeMs(currentGameTimeMs);
        }
    }
    /**
     * receive Broadcast Information, interpret this and update GUI and every variable based on information received
     * @param intent
     */
    private void updateGUI(Intent intent){
        long gameMsTillFinished, gameMsExceeded, roundMsTillFinished, roundMsExceeded;
        gameMsTillFinished = gameMsExceeded = roundMsTillFinished =  roundMsExceeded = TAGHelper.DEFAULT_VALUE_LONG;
        boolean gameFinishedSignal = false;
        boolean roundFinishedSignal = false;
        if(intent.getExtras() != null){
            // retrieved all possible value
            gameMsTillFinished = intent.getLongExtra(TAGHelper.GAME_COUNT_DOWN_TAG, TAGHelper.DEFAULT_VALUE_LONG);
            gameMsExceeded = intent.getLongExtra(TAGHelper.GAME_COUNT_IN_NEGATIVE_TAG, TAGHelper.DEFAULT_VALUE_LONG);
            gameFinishedSignal = intent.getBooleanExtra(TAGHelper.GAME_FINISHED_SIGNAL, false);
            roundMsTillFinished = intent.getLongExtra(TAGHelper.ROUND_COUNT_DOWN_TAG, TAGHelper.DEFAULT_VALUE_LONG);
            roundMsExceeded = intent.getLongExtra(TAGHelper.ROUND_COUNT_IN_NEGATIVE_TAG, TAGHelper.DEFAULT_VALUE_LONG);
            roundFinishedSignal = intent.getBooleanExtra(TAGHelper.ROUND_FINISHED_SIGNAL, false);

            // handle game timer
            if(gameFinishedSignal){
                isFinished = 1;
                mainActivity.getGameEndSound().start();
                currentGameTimeMs = 0;
                // all buttons gone but finished button
                saveGameButton.setVisibility(View.GONE);
                finishGameButton.setVisibility(View.GONE);
                nextPlayerButton.setVisibility(View.GONE);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.setText(R.string.finishGame);
                playPauseButton.setOnClickListener(finishGame);
            }
            if(gameMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
                currentGameTimeMs = gameMsTillFinished;
            }
            else if(gameMsExceeded != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedGameTimeMs = gameMsExceeded;
            }

            // handle round timer
            if(roundFinishedSignal){
                mainActivity.getRoundEndSound().start();
                currentRoundTimeMs = 0;
                if(game.getChess_mode() == 1){
                    if (isFinished == 0 && game.getReset_round_time() == 0){
                        mBoundService.pauseTimer();
                        wantToFinish.onClick(getView());
                    }
                    else nextPlayerButton.performClick();
                } else if (isFinished == 0 && game.getReset_round_time() == 1){
                    // TODO optional else case
                } else if (isFinished == 0 && game.getReset_round_time() == 0) {
                    playPauseButton.setOnClickListener(wantToFinish);
                    playPauseButton.setVisibility(View.VISIBLE);
                    nextPlayerButton.setOnClickListener(wantToFinish);
                    nextPlayerButton.setVisibility(View.VISIBLE);
                }
                else{
                    // TODO optional else case
                }

            }
            if(roundMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
                currentRoundTimeMs = roundMsTillFinished;
            }
            else if(roundMsExceeded != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedRoundTimeMs = roundMsExceeded;
            }
            gameTime = currentGameTimeMs;
            // updateGame(); do we need to call this every tick?
            updateTimerTextViews();
        }
    }

    private void initCountDownTimerOLD(final long initTime) {
        // TODO Delete this
        // init round time chronometer
        CountDownTimer countDownTimer = new CountDownTimer(initTime, 50) {

            public void onTick(long millisUntilFinished) {
                //pause game if drawer opened
                DrawerLayout drawer = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START) && !alreadyPaused) {
                    alreadyPaused = true;
                    playPauseButton.performClick();
                } else
                    alreadyPaused = false;

                updateTimerTextViews();

                currentRoundTimeMs = millisUntilFinished;
                currentGameTimeMs = gameTime - initTime + currentRoundTimeMs;

                if (game.getGame_time_infinite() == 0 && currentGameTimeMs < 100) {
                    gameTimerTv.setText("00:00:00");
                    isFinished = 1;
                    this.onFinish();
                }

                updateGame();
            }

            public void onFinish() {
                if (isFinished == 0 && game.getReset_round_time() == 1)
                    nextPlayerButton.performClick();
                else if (isFinished == 0 && game.getReset_round_time() == 0) {
                    gameTime = currentGameTimeMs;
                    updateTimerTextViews();
                    roundTimerTv.setText("00:00:00'0");

                    // Option not reset Round Time, each player has only certain amount of time
                    // if one player runs out of time --> ask if game ends or continue with other
                    // remaining players
                    if (getPlayersNotInRound(playerRounds.get(currentPlayer.getId()) + 1).size() - 1 > 0)
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.roundTimeOverDialogHeading)
                                .setMessage(R.string.roundTimeOverDialogQuestion)
                                .setIcon(android.R.drawable.ic_menu_help)
                                .setPositiveButton(R.string.finishGame, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        isFinished = 1;
                                        finishGame();
                                    }
                                })
                                .setNegativeButton(R.string.resume, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        isLastRound = 1;
                                        game.setIsLastRound(1);
                                        nextPlayerButton.performClick();
                                    }
                                })
                                .show();
                    else
                        finishGame();
                } else
                    finishGame();
            }
        };
    }
}
