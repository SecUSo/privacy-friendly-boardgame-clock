package org.secuso.privacyfriendlyboardgameclock.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;

/**
 * Created by Quang Anh Dang on 06.01.2018.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 * the Service responsible for that the app is killed.
 * Originally used to detect when to close the database
 */

public class DetectTaskClearedService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DetectTaskCleared", "Service Started");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("DetectTaskCleared", "Service Destroyed");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("DetectTaskCleared", "END");
        try{
            GamesDataSourceSingleton.getInstance(getApplicationContext()).close();
            PlayersDataSourceSingleton.getInstance(getApplicationContext()).close();
        }catch (Exception e){

        }
        stopSelf();
    }
}
