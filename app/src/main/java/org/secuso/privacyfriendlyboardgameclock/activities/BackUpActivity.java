package org.secuso.privacyfriendlyboardgameclock.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.DbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3. Copyright (C) 2016-2017 Karola Marky
 * @author Quang Anh Dang
 * This is the Activity Class for the Back Up Page
 */

public class BackUpActivity extends BaseActivity{
    private Button importBackupButton, exportBackupButton;
    private final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_backup);
        importBackupButton = findViewById(R.id.importBackupButton);
        exportBackupButton = findViewById(R.id.exportBackupButton);
    }

    public void importBackupButton(View view){
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.importDatabaseBackup)
                    .setMessage(R.string.importDatabaseBackupInfoMessage)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            importBackup();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .setIcon(android.R.drawable.ic_menu_help)
                    .show();
        }
    }

    public void exportBackupButton(View view){
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.exportDatabaseBackup)
                    .setMessage(R.string.exportDatabaseBackupInfoMessage)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exportBackup();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .setIcon(android.R.drawable.ic_menu_help)

                    .show();
        }
       }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.importDatabaseBackup)
                            .setMessage(R.string.importDatabaseBackupInfoMessage)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    importBackup();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .setIcon(android.R.drawable.ic_menu_help)

                            .show();
                }
                return;
            }
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.exportDatabaseBackup)
                            .setMessage(R.string.exportDatabaseBackupInfoMessage)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    exportBackup();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .setIcon(android.R.drawable.ic_menu_help)

                            .show();
                }
                return;
            }
        }
    }

    private void importBackup() {

        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = this.getDatabasePath(DbHelper.DB_NAME).getPath();
                    String backupDBPath = "pfa-boardgameclock-database.db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (backupDB.exists()) {
                        copy(backupDB, currentDB);
                    }

                    if (currentDB.exists() && backupDB.length() == currentDB.length())
                        Toast.makeText(this, R.string.importDatabaseBackupSuccess, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, R.string.noBackupFound, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }
    }

    private void exportBackup() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = this.getDatabasePath(DbHelper.DB_NAME).getPath();
                    String backupDBPath = "pfa-boardgameclock-database.db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (currentDB.exists()) {
                        copy(currentDB, backupDB);
                    }

                    if (backupDB.exists() && backupDB.length() == currentDB.length())
                        Toast.makeText(this, getString(R.string.exportDatabaseBackupSuccess) + " " + backupDB.getPath(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, R.string.backupExportError, Toast.LENGTH_LONG).show();
            }
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    protected int getNavigationDrawerID() {
        return R.id.nav_backup;
    }
}
