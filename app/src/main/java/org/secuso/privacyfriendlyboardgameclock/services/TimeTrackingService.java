package org.secuso.privacyfriendlyboardgameclock.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.LongSparseArray;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameCountDownActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.GameTimeTrackingModeActivity;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Quang Anh Dang on 26.01.2018.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 * the service responsible for counting up time in time tracking service
 */

public class TimeTrackingService extends Service {
    private Game game;
    /**
     * a hash map with Player ID as ID and a custom Runnable as Time Tracker
     */
    private LongSparseArray<TimeTrackingRunnable> playerIDTimeTrackerSparseArray;
    private NotificationManager notificationManager;
    /**
     * This is the object that receives interactions from clients.  See
     * RemoteService for a more complete example.
     */
    private final IBinder mBinder = new LocalBinder();
    private Intent broadcastIntent;
    private Handler handler;
    private long gameTimeDelta, gameTimeInit, gameTimeNow, gameTimePaused;
    /**
     * List of player whose time we re currently tracking
     */
    private List<Long> activePlayersList = new ArrayList<>();
    /**
     * List of players whose are on pendling between pausing and starting (triggered by pressing pause button forcing all active players to pause)
     */
    private List<Long> pendlingPlayerList = new ArrayList<>();
    /**
     * List of players whose are on pause (players who has not started yet not included)
     */
    private List<Long> pausingPlayersList = new ArrayList<>();
    private Runnable gameTimeTracker = new Runnable() {
        @Override
        public void run() {
            gameTimeNow = System.currentTimeMillis();
            gameTimeDelta = gameTimeNow - gameTimeInit;
            broadcastIntent.putExtra(TAGHelper.GAME_TIME_TRACKING,gameTimeDelta);
            sendBroadcast(broadcastIntent);
            broadcastIntent.removeExtra(TAGHelper.ROUND_COUNT_IN_NEGATIVE_TAG);
            handler.postDelayed(this, TAGHelper.COUNTDOWN_INTERVAL);
        }
    };


    public class LocalBinder extends Binder {
        public TimeTrackingService getService() {
            return TimeTrackingService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        broadcastIntent = new Intent(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Create new thread for the Handler
        /*handlerThread = new HandlerThread("CountingThread");
        handlerThread.start();*/
        handler = new Handler(/*handlerThread.getLooper()*/);
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(TAGHelper.COUNT_DOWN_TIMER_NOTIFICATION_ID);
        // Stop all the time trackers
        for(int i = 0; i < playerIDTimeTrackerSparseArray.size(); i++){
            pauseTimeTracker(playerIDTimeTrackerSparseArray.keyAt(i), true);
        }
        Log.i("TimeTrackingService", "TimeTrackingService destroyed.");
        super.onDestroy();
    }

    /**
     * Show a notification while this service is running
     * and also start the service as foreground
     */
    public void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.serviceNotificationContent);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GameTimeTrackingModeActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        initChannels(this);

        // Set the info for the views that show in the notification panel.
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this,"timetracking")
                .setSmallIcon(R.mipmap.icon)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.serviceNotificationLabel))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        startForeground(TAGHelper.COUNT_DOWN_TIMER_NOTIFICATION_ID, nBuilder.build());
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("timetracking",
                "Time Tracking Mode",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Time Tracking Mode");
        notificationManager.createNotificationChannel(channel);
    }

    public Intent getBroadcastIntent() {
        return broadcastIntent;
    }

    /**
     * ALWAYS CALL THIS METHOD FIRST BEFORE START TRACKING WITH resumeTimeTracker(long) method
     * Initialize all the necessary parameters and runnables based on.
     * @param playerTime the Map of Player ID and their starting time
     */
    public void initAllTracker(HashMap<Long,Long> playerTime, long gameTime){
        this.gameTimeDelta = gameTime;
        playerIDTimeTrackerSparseArray = new LongSparseArray<>();
        for(Map.Entry<Long, Long> entry:playerTime.entrySet()){
            playerIDTimeTrackerSparseArray.put(entry.getKey()
                    , new TimeTrackingRunnable(String.valueOf(entry.getKey()), entry.getValue()));
        }
    }

    /**
     * pause time tracker for the given player
     * @param playerID player ID of a player whose tracker we want to pause
     * @param pendling true if pause triggered by pressing universal pause button.
     *                 However if this player is paused individually --> false
     */
    public void pauseTimeTracker(long playerID, boolean pendling){
        TimeTrackingRunnable timeTracker = playerIDTimeTrackerSparseArray.get(playerID);
        if(timeTracker != null) {
            activePlayersList.remove(playerID);
            if(pendling) pendlingPlayerList.add(playerID);
            else pausingPlayersList.add(playerID);
            // GAME TIME TRACKER
            if(isGamePaused()){
                gameTimePaused = System.currentTimeMillis();
                handler.removeCallbacks(gameTimeTracker);
            }
            // PLAYER TIME TRACKER
            timeTracker.isPaused = true;
            timeTracker.timePaused = System.currentTimeMillis();
            handler.removeCallbacks(timeTracker);
        }

    }

    /**
     * resume or start (for the first time) time tracker for the given player
     * @param playerID player ID of a player whose tracker we want to resume
     */
    public void resumeTimeTracker(long playerID){
        TimeTrackingRunnable timeTracker = playerIDTimeTrackerSparseArray.get(playerID);
        if(timeTracker != null) {
            // GAME TIME TRACKER
            // before doing anything, check if the game is already init meaning
            // if this starting for the first time
            if(!isGameEverStarted()){
                showNotification();
                gameTimeInit = System.currentTimeMillis() - gameTimeDelta;
                gameTimeNow = System.currentTimeMillis();
                handler.post(gameTimeTracker);
            } else if(isGamePaused()){ // if game timer is on paused, resume it
                gameTimeInit += System.currentTimeMillis() - gameTimePaused;
                handler.post(gameTimeTracker);
            }

            // PLAYER TIME TRACKER
            activePlayersList.add(playerID);
            if(timeTracker.isAlreadyInit && timeTracker.isPaused){
                // remove the player from pausing list
                pausingPlayersList.remove(playerID);
                pendlingPlayerList.remove(playerID);
                timeTracker.isPaused = false;
                timeTracker.timeInit += System.currentTimeMillis() - timeTracker.timePaused;
                handler.post(timeTracker);
            }
            else if (!timeTracker.isAlreadyInit){
                timeTracker.timeInit = System.currentTimeMillis() - timeTracker.savedGameTimeDelta;
                timeTracker.timeNow = System.currentTimeMillis();
                timeTracker.isAlreadyInit = true;
                handler.post(timeTracker);
            }
        }

    }

    /**
     *
     * @return true if the game is started for the first time already
     */
    public boolean isGameEverStarted(){
        return !activePlayersList.isEmpty() || !pendlingPlayerList.isEmpty() || !pausingPlayersList.isEmpty();
    }

    /**
     *
     * @return true if the game is paused
     */
    public boolean isGamePaused(){
        return activePlayersList.isEmpty() && (!pausingPlayersList.isEmpty() || !pendlingPlayerList.isEmpty());
    }

    /**
     *
     * @return true if all players are either in pausing state or hasn't started yet
     */
    public boolean areAllPlayersPausingOrNotStarted(){
        return activePlayersList.isEmpty() && pendlingPlayerList.isEmpty() && !pausingPlayersList.isEmpty();
    }

    /**
     *
     * @return true if playerIDTimeTrackerSparseArray is not null
     */
    public boolean isAllTrackerInit(){
        return playerIDTimeTrackerSparseArray != null;
    }

    public List<Long> getActivePlayersList() {
        return activePlayersList;
    }

    public List<Long> getPausingPlayersList() {
        return pausingPlayersList;
    }

    public List<Long> getPendlingPlayerList() {
        return pendlingPlayerList;
    }

    /**
     * TODO Javadoc
     */
    protected class TimeTrackingRunnable implements Runnable{
        private String playerID;
        // if its a saved game, player might already have an amount of time running (only used once if started), else 0 if game is not saved
        private long savedGameTimeDelta;
        // In order: The actual time delta, the system time by the time we start counting minus the starting amount of time, the system time now and the system time when paused
        private long timeDelta, timeInit, timeNow, timePaused;
        private boolean isPaused;
        private boolean isAlreadyInit;

        /**
         *
         * @param playerID ID of this player in String
         * @param savedGameTimeDelta if its a saved game, player might already have an amount of time running (only used once if started), else 0 if game is not saved
         */
        public TimeTrackingRunnable(String playerID, long savedGameTimeDelta) {
            this.playerID = playerID;
            this.timeDelta = this.timeInit = this.timeNow = this.timePaused = 0;
            this.isAlreadyInit = false;
            this.isPaused = false;
            this.savedGameTimeDelta = savedGameTimeDelta;
        }

        @Override
        public void run() {
            timeNow = System.currentTimeMillis();
            timeDelta = timeNow - timeInit;
            // Put player ID and the current game time for this player in broadcast
            broadcastIntent.putExtra(playerID, timeDelta);
            sendBroadcast(broadcastIntent);
            broadcastIntent.removeExtra(playerID);
            handler.postDelayed(this, TAGHelper.COUNTDOWN_INTERVAL);
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }
}
