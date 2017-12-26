package org.secuso.privacyfriendlyboardgameclock.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Quang Anh Dang on 26.12.2017.
 *
 * @author Quang Anh Dang
 */

public class CountdownTimerService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
