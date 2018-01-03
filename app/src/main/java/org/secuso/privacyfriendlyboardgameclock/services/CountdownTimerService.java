package org.secuso.privacyfriendlyboardgameclock.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;

/**
 * Created by Quang Anh Dang on 26.12.2017.
 *
 * @author Quang Anh Dang
 */

public class CountdownTimerService extends Service{
    public static final String classTAG = "CountdownTimerService";
    private NotificationManager notificationManager;
    private CountDownTimer gameCountDownTimer;
    private CountDownTimer roundCountDownTimer;
    // Start Value is -1
    private long currentRoundTimeMs, currentGameTimeMs, gameTime;
    private final long COUNTDOWN_INTERVAL = 50;
    private boolean isPaused = true;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private Intent broadcastIntent;
    private Handler handler;
    private long exeedGameTimeMs, exeedGameTimeInitMs, exeedGameTimeNowMs, exeedGameTimePauseMs;
    private long exeedRoundTimeMs, exeedRoundTimeInitMs, exeedRoundTimeNowMs, exeedRoundTimePauseMs;
    private Runnable exeedGameTimeMsCounter = new Runnable() {
        @Override
        public void run() {
            exeedGameTimeNowMs = System.currentTimeMillis();
            exeedGameTimeMs = exeedGameTimeNowMs - exeedGameTimeInitMs;
            broadcastIntent.putExtra(TAGHelper.GAME_COUNT_IN_NEGATIVE_TAG,exeedGameTimeMs);
            sendBroadcast(broadcastIntent);
            broadcastIntent.removeExtra(TAGHelper.GAME_COUNT_IN_NEGATIVE_TAG);
            handler.postDelayed(this, COUNTDOWN_INTERVAL);
        }
    };
    private Runnable exeedRoundTimeMsCounter = new Runnable() {
        @Override
        public void run() {
            exeedRoundTimeNowMs = System.currentTimeMillis();
            exeedRoundTimeMs = exeedRoundTimeNowMs - exeedRoundTimeInitMs;
            broadcastIntent.putExtra(TAGHelper.ROUND_COUNT_IN_NEGATIVE_TAG,exeedRoundTimeMs);
            sendBroadcast(broadcastIntent);
            broadcastIntent.removeExtra(TAGHelper.ROUND_COUNT_IN_NEGATIVE_TAG);
            handler.postDelayed(this, COUNTDOWN_INTERVAL);
        }
    };


    public class LocalBinder extends Binder {
        public CountdownTimerService getService() {
            return CountdownTimerService.this;
        }
    }


    @Override
    public void onCreate() {
        currentRoundTimeMs = currentGameTimeMs = gameTime = -1;
        exeedGameTimeMs = exeedGameTimeInitMs = exeedGameTimeNowMs = exeedGameTimePauseMs = -1;
        exeedRoundTimeMs = exeedRoundTimeInitMs = exeedRoundTimeNowMs = exeedRoundTimePauseMs = -1;
        broadcastIntent = new Intent(TAGHelper.COUNTDOWN_SERVICE_BROADCAST_TAG);
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
        handler = new Handler();
        Log.i(classTAG, "CountdownTimerService created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(TAGHelper.COUNT_DOWN_TIMER_NOTIFICATION_ID);
        pauseTimer();
        Log.i(classTAG, "CountdownTimerService destroyed.");
        Toast.makeText(this, R.string.CountDownTimerServiceStopped,Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initGameCountdownTimer(final long initTime){
        Log.i(classTAG, "Game Countdown Timer initialized.");
        gameCountDownTimer = new CountDownTimer(initTime, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentGameTimeMs = millisUntilFinished;
                broadcastIntent.putExtra(TAGHelper.GAME_COUNT_DOWN_TAG, millisUntilFinished);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.GAME_COUNT_DOWN_TAG);
            }

            @Override
            public void onFinish() {
                // if game finished, send a true signal
                broadcastIntent.putExtra(TAGHelper.GAME_FINISHED_SIGNAL, true);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.GAME_FINISHED_SIGNAL);
                currentGameTimeMs = 0;
                exeedGameTimeInitMs = System.currentTimeMillis();
                handler.post(exeedGameTimeMsCounter);
            }
        };
    }

    public void initRoundCountdownTimer(final long initTime){
        Log.i(classTAG, "Round Countdown Timer initialized.");
        roundCountDownTimer = new CountDownTimer(initTime, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentRoundTimeMs = millisUntilFinished;
                broadcastIntent.putExtra(TAGHelper.ROUND_COUNT_DOWN_TAG, millisUntilFinished);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.ROUND_COUNT_DOWN_TAG);
            }

            @Override
            public void onFinish() {
                // if round finished, send a true signal
                broadcastIntent.putExtra(TAGHelper.ROUND_FINISHED_SIGNAL, true);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.ROUND_FINISHED_SIGNAL);
                currentRoundTimeMs = 0;
                exeedRoundTimeInitMs = System.currentTimeMillis();
                handler.post(exeedRoundTimeMsCounter);
            }
        };
    }

    /**
     * called after init method to start game timer
     */
    public void startGameTimer(){
        if(gameCountDownTimer != null){
            gameCountDownTimer.start();
        }
    }

    /**
     * called after init method to start round timer
     */
    public void startRoundTimer(){
        if (roundCountDownTimer != null) {
            roundCountDownTimer.start();
        }
    }

    public void pauseTimer(){
        isPaused = true;
        if(gameCountDownTimer != null) gameCountDownTimer.cancel();
        if(roundCountDownTimer != null) roundCountDownTimer.cancel();
        exeedGameTimePauseMs = System.currentTimeMillis();
        handler.removeCallbacks(exeedGameTimeMsCounter);
        exeedRoundTimePauseMs = System.currentTimeMillis();
        handler.removeCallbacks(exeedRoundTimeMsCounter);
    }

    public void resumeTimer(){
        isPaused = false;
        if(currentGameTimeMs > 0){
            initGameCountdownTimer(currentGameTimeMs);
            startGameTimer();
        }
        if(currentRoundTimeMs > 0){
            initRoundCountdownTimer(currentRoundTimeMs);
            startRoundTimer();
        }
        if(currentGameTimeMs == 0){
            exeedGameTimeInitMs += System.currentTimeMillis() - exeedGameTimePauseMs;
            handler.post(exeedGameTimeMsCounter);
        }
        if(currentRoundTimeMs == 0){
            exeedRoundTimeInitMs += System.currentTimeMillis() - exeedRoundTimePauseMs;
            handler.post(exeedRoundTimeMsCounter);
        }
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.serviceNotificationContent);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.icon)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.serviceNotificationLabel))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notificationManager.notify(TAGHelper.COUNT_DOWN_TIMER_NOTIFICATION_ID, notification);
    }

    public long getCurrentRoundTimeMs() {
        return currentRoundTimeMs;
    }

    public void setCurrentRoundTimeMs(long currentRoundTimeMs) {
        this.currentRoundTimeMs = currentRoundTimeMs;
    }

    public long getCurrentGameTimeMs() {
        return currentGameTimeMs;
    }

    public void setCurrentGameTimeMs(long currentGameTimeMs) {
        this.currentGameTimeMs = currentGameTimeMs;
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setGameTime(long gameTime) {
        this.gameTime = gameTime;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }
}
