/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.ActionBar;
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
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.GameHistoryInfoDialogFragment;
import org.secuso.privacyfriendlyboardgameclock.fragments.GameResultDialogFragment;
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
 * Created by Quang Anh Dang on 03.01.2018.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the Activity for the actual Game Countdown Mode
 */

public class GameCountDownActivity extends BaseActivity {
    private BroadcastReceiver br;
    private GamesDataSourceSingleton gds;
    private Game game;
    private long gameTime;
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

    private long roundTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG;
    private long gameTimeOriTenPercent = TAGHelper.DEFAULT_VALUE_LONG;

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
            prepareAll();
            Log.i("GameCountDownActivity", "Service Connected.");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.i("GameCountDownActivity", "Service Disconnected.");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(GameCountDownActivity.this,
                CountdownTimerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    View.OnClickListener saveGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playPauseButton.setOnClickListener(pause);
            playPauseButton.performClick();
            new AlertDialog.Builder(GameCountDownActivity.this)
                    .setTitle(R.string.saveGame)
                    .setMessage(R.string.sureToSaveGameQuestion)
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveGameToDb(1);
                            alreadySaved = true;
                            Toast.makeText(GameCountDownActivity.this, R.string.gameSavedSuccess, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    };
    private View.OnClickListener showGameResults = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            Fragment prev = fm.findFragmentByTag(TAGHelper.DIALOG_FRAGMENT);
            if(prev != null) ft.remove(prev);
            ft.addToBackStack(null);

            // Create and show the dialog
            GameResultDialogFragment showGameInfo = new GameResultDialogFragment();
            showGameInfo.show(ft, TAGHelper.DIALOG_FRAGMENT);
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

    private View.OnClickListener pauseFinishedGame = new View.OnClickListener() {
        public void onClick(View v) {
            isPaused = true;

            mBoundService.pauseTimer();
            gameTime = currentGameTimeMs;

            playPauseButton.setText(R.string.resume);
            playPauseButton.setOnClickListener(continueFinishedGame);

            updateTimerTextViews();
        }
    };

    private View.OnClickListener continueFinishedGame = new View.OnClickListener() {
        public void onClick(View v) {
            alreadySaved = false;

            saveGameButton.setVisibility(View.GONE);
            finishGameButton.setVisibility(View.GONE);

            isPaused = false;

            updateAndResumeTimer();

            playPauseButton.setText(R.string.pause_capslock);
            playPauseButton.setOnClickListener(pauseFinishedGame);
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

                    final AlertDialog.Builder builder = new AlertDialog.Builder(GameCountDownActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_player_sequence, null);
                    builder.setView(dialogView);
                    builder.setTitle(R.string.manualChoiceHeading);
                    builder.setPositiveButton(R.string.confirm,null);
                    builder.setCancelable(false);

                    final ListView myListView = (ListView) dialogView.findViewById(R.id.set_player_sequence_list);
                    SelectPlayerListAdapter listAdapter = new SelectPlayerListAdapter(GameCountDownActivity.this, R.id.set_player_sequence_list, players);
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
                    final AlertDialog ad = builder.show();
                    Button positiveButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (players.size() != selectedPlayers.size()) {
                                showToast(getString(R.string.manualChoiceError));
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
            playPauseButton.setOnClickListener(pause);
            playPauseButton.performClick();
            new AlertDialog.Builder(GameCountDownActivity.this)
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
                playPauseButton.setOnClickListener(pause);
                playPauseButton.performClick();
                new AlertDialog.Builder(GameCountDownActivity.this)
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
                                //isLastRound = 1;
                                //game.setIsLastRound(1);
                                playPauseButton.setOnClickListener(run);
                                nextPlayerButton.setOnClickListener(nextPlayer);
                                //nextPlayerButton.performClick();
                            }
                        })
                        .show();
            }
            else finishGame();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if(checkIfSingletonDataIsCorrupt()) return;

        gds = GamesDataSourceSingleton.getInstance(this);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGUI(intent);
            }
        };

        // prevent phone from sleeping while game is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_game_countdown);

        // show swipe dialog
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstStart = settings.getBoolean("showSwipeDialog", true);
        if (firstStart) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.swipeDialogQuestion)
                    .setMessage(R.string.swipeDialogAnswer)
                    .setIcon(android.R.drawable.ic_menu_info_details)
                    .setPositiveButton(R.string.ok, null)
                    .show();

            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("showSwipeDialog", false);
            editor.commit();
        }

        playPauseButton = findViewById(R.id.gamePlayPauseButton);
        finishGameButton = findViewById(R.id.finishGameButton);
        saveGameButton = findViewById(R.id.saveGameButton);

        gameTimerTv = findViewById(R.id.game_timer);
        roundTimerTv = findViewById(R.id.round_timer);

        currentPlayerTv = findViewById(R.id.game_current_player_name);
        currentPlayerRound = findViewById(R.id.game_current_player_round);
        currentPlayerIcon = findViewById(R.id.imageViewIcon);
        nextPlayerButton = findViewById(R.id.nextPlayerButton);
        ImageButton saveGameButton = findViewById(R.id.saveGameButton);
        saveGameButton.setOnClickListener(saveGame);
        ImageButton finishGameButton = findViewById(R.id.finishGameButton);
        finishGameButton.setOnClickListener(finishGame);

        // If service already exists
        if(isMyServiceRunning(CountdownTimerService.class)){
            game = null;
        }else{
            // get game from SingleTon Class, if null, show MainMenu
            if(gds.getGame() != null){
                game = gds.getGame();
            }
            else showMainMenu();
        }
        startTimerService();
    }

    /**
     * before starting, prepare everything incl. views based on
     * if the service already exists and running or this is a
     * complete new game
     */
    private void prepareAll(){
        // if the service already exists
        boolean isNewGame = game != null;
        if(game == null) game = mBoundService.getGame();

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if(ab != null) ab.setSubtitle(game.getName());

        players = game.getPlayers();
        playerRoundTimes = game.getPlayer_round_times();
        playerRounds = game.getPlayer_rounds();
        currentPlayerIndex = game.getStartPlayerIndex();
        nextPlayerIndex = game.getNextPlayerIndex();
        currentPlayer = players.get(game.getStartPlayerIndex());
        playersQueue = getPlayersNotInRound(playerRounds.get(currentPlayer.getId()));
        currentPlayerTv.setText(currentPlayer.getName());
        currentPlayerRound.setText(playerRounds.get(currentPlayer.getId()).toString());
        currentPlayerIcon.setImageBitmap(currentPlayer.getIcon());
        currentRoundTimeMs = playerRoundTimes.get(currentPlayer.getId());
        currentGameTimeMs = game.getCurrentGameTime();
        roundTimeOriTenPercent = (long)(currentRoundTimeMs * 0.1);
        gameTimeOriTenPercent = (long)(currentGameTimeMs * 0.1);
        gameTime = currentGameTimeMs;
        isLastRound = game.getIsLastRound();
        if (game.getGame_time_infinite() == 1) {
            gameTimerTv.setText(getString(R.string.infinite));
        }
        updateTimerTextViews();
        nextPlayerButton.setOnClickListener(nextPlayer);
        findViewById(R.id.main_content).setOnTouchListener(new OnSwipeTouchListener(getBaseContext()) {
            @Override
            public void onSwipeLeft() {
                nextPlayerButton.callOnClick();
            }

            @Override
            public void onSwipeRight() {
                nextPlayerButton.callOnClick();
            }
        });

        if(isNewGame){
            playPauseButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    alreadySaved = false;
                    isPaused = false;
                    mBoundService.showNotification();
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
                }
            });
            mBoundService.setGame(game);
        }else{
            alreadySaved = game.getSaved() == 1;
            isPaused = mBoundService.isPaused();
            if(isPaused){
                playPauseButton.setText(R.string.resume);
                playPauseButton.setOnClickListener(run);

            }else{
                playPauseButton.setText(R.string.pause_capslock);
                playPauseButton.setOnClickListener(pause);
            }
            nextPlayerButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setDrawerEnabled(false);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG));
        Log.i("GameCountDownActivity", "Registered Broadcast Receiver." );
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
        unregisterRegister();
        doUnbindService();
        super.onDestroy();
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
        if (currentRoundTimeMs <= roundTimeOriTenPercent)
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

            if (game.getGame_time_infinite() == 0 && currentGameTimeMs <= gameTimeOriTenPercent)
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
        gds.setGame(game);
        game.setFinished(isFinished);
        updateGame();
        stopTimerService();

        saveGameButton.setVisibility(View.GONE);
        finishGameButton.setVisibility(View.GONE);
        nextPlayerButton.setVisibility(View.GONE);
        playPauseButton.setText(R.string.showResults);
        playPauseButton.setOnClickListener(showGameResults);

        saveGameToDb(0);
    }

    @Override
    public void onBackPressed() {
        if (!isPaused && (isFinished == 0))
            playPauseButton.performClick();

        if (isFinished == 1) {
            showMainMenu();
        } else {
            playPauseButton.setOnClickListener(pause);
            playPauseButton.performClick();
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.quitGame))
                    .setMessage(getString(R.string.leaveGameQuestion))
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(getString(R.string.saveGame), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (!alreadySaved){
                                saveGameToDb(1);
                                stopTimerService();
                                showMainMenu();
                            }
                            else
                                showMainMenu();
                        }
                    })
                    .setNeutralButton(R.string.withoutSave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            stopTimerService();
                            showMainMenu();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
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

    /**
     * @return true if service has already been running and started
     */
    private void startTimerService(){
        startService(new Intent(this, CountdownTimerService.class));
        doBindService();
    }

    private void stopTimerService(){
        unregisterRegister();
        doUnbindService();
        stopService(new Intent(this,CountdownTimerService.class));
    }

    private void unregisterRegister(){
        try{
            unregisterReceiver(br);
        }catch (Exception e){
        }
        Log.i("GameCountDownActivity", "Unregistered Broadcast Receiver." );
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
                // Remove finish signal after reading
                mBoundService.getBroadcastIntent().removeExtra(TAGHelper.GAME_FINISHED_SIGNAL);
                currentGameTimeMs = 0;
                /*isFinished = 1;*/
                // all buttons gone but finished button
                /*saveGameButton.setVisibility(View.GONE);
                finishGameButton.setVisibility(View.GONE);
                nextPlayerButton.setVisibility(View.VISIBLE);
                nextPlayerButton.setOnClickListener(finishGame);
                nextPlayerButton.setText(R.string.finishGame);
                playPauseButton.setVisibility(View.VISIBLE);
                playPauseButton.setOnClickListener(pauseFinishedGame);*/
            }

            // handle round timer
            if(roundFinishedSignal){
                // Remove finish signal after reading
                mBoundService.getBroadcastIntent().removeExtra(TAGHelper.ROUND_FINISHED_SIGNAL);
                currentRoundTimeMs = 0;
                if(game.getChess_mode() == 1){
                    if (isFinished == 0 && game.getReset_round_time() == 0){
                        mBoundService.pauseTimer();
                        wantToFinish.onClick(findViewById(R.id.main_content));
                    }
                    else nextPlayerButton.performClick();
                } else if (isFinished == 0 && game.getReset_round_time() == 1){
                    // optional else case
                } /*else if (isFinished == 0 && game.getReset_round_time() == 0) {
                    playPauseButton.setOnClickListener(wantToFinish);
                    playPauseButton.setVisibility(View.VISIBLE);
                    nextPlayerButton.setOnClickListener(wantToFinish);
                    nextPlayerButton.setVisibility(View.VISIBLE);
                }*/
                else{
                    // optional else case
                }

            }

            if(gameMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedGameTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
                currentGameTimeMs = gameMsTillFinished;
            }
            else if(gameMsExceeded != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedGameTimeMs = gameMsExceeded;
            }

            if(roundMsTillFinished != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedRoundTimeMs = TAGHelper.DEFAULT_VALUE_LONG;
                currentRoundTimeMs = roundMsTillFinished;
            }
            else if(roundMsExceeded != TAGHelper.DEFAULT_VALUE_LONG){
                currentExceedRoundTimeMs = roundMsExceeded;
            }
            gameTime = currentGameTimeMs;
            updateGame(); //do we need to call this every tick?
            mBoundService.setGame(game);
            updateTimerTextViews();
        }
    }

    public Game getGame() {
        return game;
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }
}
