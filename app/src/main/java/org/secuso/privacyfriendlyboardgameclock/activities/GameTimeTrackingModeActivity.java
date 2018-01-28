package org.secuso.privacyfriendlyboardgameclock.activities;

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
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.fragments.GameResultDialogFragment;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.helpers.TimeTrackingPlayerAdapter;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.services.TimeTrackingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Quang Anh Dang on 26.01.2018.
 *
 * @author Quang Anh Dang
 */

public class GameTimeTrackingModeActivity extends BaseActivity implements ItemClickListener{
    private PlayersDataSourceSingleton pds;
    private GamesDataSourceSingleton gds;
    private Game game;
    private List<Player> players;
    private HashMap<Long,Long> playersTime;
    private List<Long> playerIDs = new ArrayList<>();
    private long currentGameTimeMs = 0;
    private BroadcastReceiver br;
    private TimeTrackingPlayerAdapter timeTrackingAdapter;
    private int isFinished = 0;
    private boolean alreadySaved = false;

    // Views and Buttons Definitions
    private TextView currentGameTimeTV;
    private ImageButton saveGameButton;
    private Button playPauseButton;
    private ImageButton finishGameButton;

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

    private View.OnClickListener saveGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pauseTimeTrackers();
            new AlertDialog.Builder(GameTimeTrackingModeActivity.this)
                    .setTitle(R.string.saveGame)
                    .setMessage(R.string.sureToSaveGameQuestion)
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            saveGameToDb(1);
                            alreadySaved = true;
                            Toast.makeText(GameTimeTrackingModeActivity.this, R.string.gameSavedSuccess, Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        }
    };

    /**
     * TODO OLD remove later if not needed
     */
    private View.OnClickListener runAllPendlingTrackers = new View.OnClickListener() {
        public void onClick(View v) {
            alreadySaved = false;

            saveGameButton.setVisibility(View.GONE);
            finishGameButton.setVisibility(View.GONE);

            resumeTimeTrackers();

            playPauseButton.setText(R.string.pause_capslock);
            playPauseButton.setOnClickListener(pauseAllActiveTrackers);
        }
    };

    /**
     * TODO OLD maybe removed later if not needed
     */
    private View.OnClickListener pauseAllActiveTrackers = new View.OnClickListener() {
        public void onClick(View v) {
            pauseTimeTrackers();

            saveGameButton.setVisibility(View.VISIBLE);
            finishGameButton.setVisibility(View.VISIBLE);

            playPauseButton.setText(R.string.resume);
            playPauseButton.setOnClickListener(runAllPendlingTrackers);
        }
    };

    private View.OnClickListener pauseAll = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            pauseTimeTrackers();
        }
    };

    private View.OnClickListener finishGame = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            pauseTimeTrackers();
            new AlertDialog.Builder(GameTimeTrackingModeActivity.this)
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

    
    private TimeTrackingService mBoundService;
    private boolean mIsBound = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
            mBoundService = ((TimeTrackingService.LocalBinder)service).getService();
            // Init only once
            if(!mBoundService.isAllTrackerInit()){
                mBoundService.initAllTracker(playersTime, currentGameTimeMs);
            }
            Log.i("GameTimeTrackingModeAct", "Service Connected.");
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
            mBoundService = null;
            Log.i("GameTimeTrackingModeAct", "Service Disconnected.");
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(GameTimeTrackingModeActivity.this,
                TimeTrackingService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check if date saved in Singleton Class corrupted, if yes return to Main Menu
        if(checkIfSingletonDataIsCorrupt()) return;

        setContentView(R.layout.activity_time_tracking_mode);
        pds = PlayersDataSourceSingleton.getInstance(this);
        gds = GamesDataSourceSingleton.getInstance(this);

        // prevent phone from sleeping while game is running
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // get game from SingleTon Class, if null, show MainMenu
        if(gds.getGame() != null){
            game = gds.getGame();
        }
        else showMainMenu();

        // register broadcast receiver
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateGUI(intent);
            }
        };

        // populate data
        players = game.getPlayers();
        for(Player p: players) playerIDs.add(p.getId());
        playersTime = game.getPlayer_round_times();

        currentGameTimeMs = game.getCurrentGameTime();
        currentGameTimeTV = findViewById(R.id.game_timer);
        updateGameTimerTextview();

        saveGameButton =  findViewById(R.id.saveGameButton);
        saveGameButton.setOnClickListener(saveGame);

        playPauseButton = findViewById(R.id.gamePlayPauseButton);
        playPauseButton.setText(R.string.pause_capslock);
        playPauseButton.setOnClickListener(pauseAll);
        playPauseButton.setVisibility(View.VISIBLE);

        finishGameButton = findViewById(R.id.finishGameButton);
        finishGameButton.setOnClickListener(finishGame);

        RecyclerView playerRecycleView = findViewById(R.id.player_list);
        playerRecycleView.setHasFixedSize(false);
        timeTrackingAdapter = new TimeTrackingPlayerAdapter(this, players, this);
        playerRecycleView.setAdapter(timeTrackingAdapter);
        playerRecycleView.setLayoutManager(new LinearLayoutManager(this));
        playerRecycleView.setItemAnimator(null);

        // start time tracking service
        startTimeTrackerService();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getSupportActionBar().setSubtitle(game.getName());
        setDrawerEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(br, new IntentFilter(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG));
        Log.i("GameTimeTrackingModeAct", "Registered Broadcast Receiver." );
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
        stopTimeTrackerService();
        super.onDestroy();
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
        stopTimeTrackerService();

        saveGameButton.setVisibility(View.GONE);
        finishGameButton.setVisibility(View.GONE);
        playPauseButton.setText(R.string.showResults);
        playPauseButton.setOnClickListener(showGameResults);

        saveGameToDb(0);
    }

    /**
     * update the current game object
     * called by saveGameToDb, by nextPlayer button,
     * (3)
     */
    public void updateGame() {
        game.setPlayer_round_times(playersTime);
        game.setCurrentGameTime(currentGameTimeMs);
        game.setFinished(isFinished);
    }

    /**
     * resume all the paused time trackers
     */
    private void resumeTimeTrackers() {
        ArrayList<Long> copyPendlingPlayers = new ArrayList<>(mBoundService.getPendlingPlayerList());
        for(Long playerID: copyPendlingPlayers){
            mBoundService.resumeTimeTracker(playerID);
        }
    }

    /**
     * pause all the active time trackers
     */
    private void pauseTimeTrackers() {
        ArrayList<Long> copyActivePlayersList = new ArrayList<>(mBoundService.getActivePlayersList());
        for(Long playerID: copyActivePlayersList){
            // save all to pausingList, if needed to save the state, set this to true
            mBoundService.pauseTimeTracker(playerID, false);
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

    private void updateGUI(Intent intent) {
        if(intent.getExtras() != null){
            // UPDATE PLAYER TIME
            for(int i = 0; i < playerIDs.size(); i++){
                long currentPlayerID = playerIDs.get(i);
                long currentPlayerTime =  intent.getLongExtra(String.valueOf(currentPlayerID), TAGHelper.DEFAULT_VALUE_LONG);
                if(currentPlayerTime != TAGHelper.DEFAULT_VALUE_LONG){
                    playersTime.put(currentPlayerID, currentPlayerTime);
                    timeTrackingAdapter.notifyItemChanged(i);
                }
            }
            // UPDATE GAME TIME
            long currentGameTimeNew = intent.getLongExtra(TAGHelper.GAME_TIME_TRACKING, TAGHelper.DEFAULT_VALUE_LONG);
            if(currentGameTimeNew != TAGHelper.DEFAULT_VALUE_LONG){
                currentGameTimeMs = currentGameTimeNew;
                updateGameTimerTextview();
            }
        }
    }

    private void updateGameTimerTextview() {
        // UDPATE GAME TIME TRACKER
        String[] gameTimeInString = getTimeStrings(currentGameTimeMs);
        String game_time_hh = gameTimeInString[0];
        String game_time_mm = gameTimeInString[1];
        String game_time_ss = gameTimeInString[2];
        String game_time_result = game_time_hh + ":" + game_time_mm + ":" + game_time_ss;
        currentGameTimeTV.setText(game_time_result);
    }

    private void startTimeTrackerService(){
        startService(new Intent(this, TimeTrackingService.class));
        doBindService();
    }

    private void stopTimeTrackerService(){
        unregisterRegister();
        doUnbindService();
        stopService(new Intent(this,TimeTrackingService.class));
    }

    private void unregisterRegister(){
        try{
            unregisterReceiver(br);
        }catch (Exception e){
        }
        Log.i("GameTimeTrackingModeAct", "Unregistered Broadcast Receiver." );
    }

    @Override
    public void onBackPressed() {
        if (!mBoundService.isGamePaused())
            playPauseButton.performClick();

        if (isFinished == 1) {
            showMainMenu();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.quitGame))
                    .setMessage(getString(R.string.leaveGameQuestion))
                    .setIcon(android.R.drawable.ic_menu_help)
                    .setPositiveButton(getString(R.string.saveGame), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (!alreadySaved){
                                saveGameToDb(1);
                                showMainMenu();
                            }
                            else
                                showMainMenu();
                        }
                    })
                    .setNeutralButton(R.string.withoutSave, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showMainMenu();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        }
    }

    @Override
    protected int getNavigationDrawerID() {
        return 0;
    }

    public HashMap<Long, Long> getPlayerTime() {
        return playersTime;
    }

    @Override
    public void onItemClick(View view, int position) {
        List<Long> activePlayers = mBoundService.getActivePlayersList();
        Player currentPlayer = timeTrackingAdapter.getPlayer(position);
        // if player already running, then pause this player

        if(activePlayers.contains(currentPlayer.getId())){
            mBoundService.pauseTimeTracker(currentPlayer.getId(), false);
        } else{
            mBoundService.resumeTimeTracker(currentPlayer.getId());
            /* TODO OLD saving state mode
            playPauseButton.setVisibility(View.VISIBLE);
            playPauseButton.setOnClickListener(pauseAllActiveTrackers);
            playPauseButton.setText(R.string.pause_capslock);*/
        }

        // UPDATE VIEW OF PLAYPAUSEBUTTON
        /* TODO OLD saving state mode
        if(mBoundService.areAllPlayersPausingOrNotStarted()){
            playPauseButton.setVisibility(View.INVISIBLE);
            playPauseButton.setOnClickListener(pauseAllActiveTrackers);
            playPauseButton.setText(R.string.pause_capslock);
        }*/
        playPauseButton.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onItemLongClicked(View view, int position) {
        // NOT NEEDED YET
        return false;
    }

    public Game getGame() {
        return game;
    }
}
