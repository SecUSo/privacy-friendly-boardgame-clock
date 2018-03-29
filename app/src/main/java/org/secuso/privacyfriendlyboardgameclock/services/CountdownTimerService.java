/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlyboardgameclock.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameCountDownActivity;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

/**
 * Created by Quang Anh Dang on 26.12.2017.
 * @author Quang Anh Dang
 * The Service responsible for counting down time
 */

public class CountdownTimerService extends Service{
    private Game game;
    private MediaPlayer roundEndSound = null;
    private MediaPlayer gameEndSound = null;
    public static final String classTAG = "CountdownTimerService";
    private NotificationManager notificationManager;
    private CountDownTimer gameCountDownTimer;
    private CountDownTimer roundCountDownTimer;
    // Start Value is -1
    private long currentRoundTimeMs, currentGameTimeMs, gameTime;
    private boolean isPaused = true;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private Intent broadcastIntent;
    //TODO run on new thread crash if update UI along side
    private HandlerThread handlerThread;
    private Handler handler;
    // In order: The actual time delta, the system time by the time we start counting, the system time now and the system time when paused
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
            handler.postDelayed(this, TAGHelper.COUNTDOWN_INTERVAL);
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
            handler.postDelayed(this, TAGHelper.COUNTDOWN_INTERVAL);
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
        // prepare the sound files
        roundEndSound = MediaPlayer.create(this,R.raw.roundend);
        gameEndSound = MediaPlayer.create(this, R.raw.gameend);

        // Create new thread for the Handler
        /*handlerThread = new HandlerThread("CountingThread");
        handlerThread.start();*/
        handler = new Handler(/*handlerThread.getLooper()*/);
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
        roundEndSound.release();
        gameEndSound.release();
        Log.i(classTAG, "CountdownTimerService destroyed.");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initGameCountdownTimer(final long initTime){
        Log.i(classTAG, "Game Countdown Timer initialized.");
        gameCountDownTimer = new CountDownTimer(initTime, TAGHelper.COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentGameTimeMs = millisUntilFinished;
                broadcastIntent.putExtra(TAGHelper.GAME_COUNT_DOWN_TAG, millisUntilFinished);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.GAME_COUNT_DOWN_TAG);
            }

            @Override
            public void onFinish() {
                gameEndSound.start();
                // if game finished, send a true signal
                broadcastIntent.putExtra(TAGHelper.GAME_FINISHED_SIGNAL, true);
                sendBroadcast(broadcastIntent);
                currentGameTimeMs = 0;
                exeedGameTimeInitMs = System.currentTimeMillis();
                handler.post(exeedGameTimeMsCounter);
            }
        };
    }

    public void initRoundCountdownTimer(final long initTime){
        Log.i(classTAG, "Round Countdown Timer initialized.");
        roundCountDownTimer = new CountDownTimer(initTime, TAGHelper.COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentRoundTimeMs = millisUntilFinished;
                broadcastIntent.putExtra(TAGHelper.ROUND_COUNT_DOWN_TAG, millisUntilFinished);
                sendBroadcast(broadcastIntent);
                broadcastIntent.removeExtra(TAGHelper.ROUND_COUNT_DOWN_TAG);
            }

            @Override
            public void onFinish() {
                roundEndSound.start();
                // if round finished, send a true signal
                broadcastIntent.putExtra(TAGHelper.ROUND_FINISHED_SIGNAL, true);
                sendBroadcast(broadcastIntent);
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
        isPaused = false;
        if(gameCountDownTimer != null){
            gameCountDownTimer.start();
        }
    }

    /**
     * called after init method to start round timer
     */
    public void startRoundTimer(){
        isPaused = false;
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
     * Show a notification while this service is running
     * and also start the service as foreground
     */
    public void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.serviceNotificationContent);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, GameCountDownActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        initChannels(this);

        // Set the info for the views that show in the notification panel.
        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this,"countdown")
                .setSmallIcon(R.mipmap.icon)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.serviceNotificationLabel))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent);  // The intent to send when the entry is clicked

        Notification notification = nBuilder.build();

        startForeground(TAGHelper.COUNT_DOWN_TIMER_NOTIFICATION_ID, notification);
    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("countdown",
                "Count Down Timer",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Count Down Timer");
        notificationManager.createNotificationChannel(channel);
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

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Intent getBroadcastIntent() {
        return broadcastIntent;
    }
}
