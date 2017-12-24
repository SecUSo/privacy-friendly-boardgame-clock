package org.secuso.privacyfriendlyboardgameclock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import org.secuso.privacyfriendlyboardgameclock.R;

/**
 * Created by Quang Anh Dang on 24.12.2017.
 *
 * @author Quang Anh Dang
 */

public class BackUpActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_backup);
    }
}
