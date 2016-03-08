package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import privacyfriendlyexample.org.secuso.boardgameclock.R;

/**
 * Created by yonjuni on 12.01.16.
 */
public class SettingsFragment extends Fragment {

    Activity activity;
    Button languageButton, themeButton, importBackupButton, exportBackupButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_settings);

        ImageView iv = (ImageView) rootView.findViewById(R.id.appIcon);
        iv.setImageResource(R.mipmap.ic_launcher);

        languageButton = (Button) rootView.findViewById(R.id.languageButton);
        themeButton = (Button) rootView.findViewById(R.id.themeButton);
        importBackupButton = (Button) rootView.findViewById(R.id.importBackupButton);
        exportBackupButton = (Button) rootView.findViewById(R.id.exportBackupButton);

        languageButton.setOnClickListener(language);
        themeButton.setOnClickListener(theme);
        importBackupButton.setOnClickListener(importBackup);
        exportBackupButton.setOnClickListener(exportBackup);

        container.removeAllViews();
        return rootView;
    }

    private String[] languages = new String[]{"DE", "EN"};
    OnClickListener language = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(languages, 0, null)
                    .setTitle("Change Language")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                            changeLang(languages[selectedPosition]);

                            Toast.makeText(getActivity(), "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    };

    OnClickListener theme = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "NOT YET IMPLEMENTED", Toast.LENGTH_SHORT).show();

        }
    };

    OnClickListener importBackup = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(languages, 0, null)
                    .setTitle("Import Database Backup")
                    .setMessage("Are you sure you want to import the current database backup from external storage? The active database will be replaced.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            importBackup();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }
    };

    private void importBackup() {

        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/privacyfriendlyexample.org.secuso.boardgameclock/databases/database.db";
                    String backupDBPath = "pfa-boardgameclock_database.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (backupDB.exists()) {
                        FileChannel src = new FileInputStream(backupDB).getChannel();
                        FileChannel dst = new FileOutputStream(currentDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }

                    if (currentDB.exists() && backupDB.getTotalSpace() == currentDB.getTotalSpace())
                        Toast.makeText(getActivity(), "Backup successfully loaded!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.w("Settings Backup", e);
            }
    }

    OnClickListener exportBackup = new OnClickListener() {

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(getActivity())
                    .setSingleChoiceItems(languages, 0, null)
                    .setTitle("Export Database Backup")
                    .setMessage("Are you sure you want to create a backup from the active database? The current database backup will be replaced.")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            exportBackup();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();

        }

    };

    private void exportBackup() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/privacyfriendlyexample.org.secuso.boardgameclock/databases/database.db";
                    String backupDBPath = "pfa-boardgameclock_database.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (currentDB.exists()) {
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }

                    if (backupDB.exists() && backupDB.getTotalSpace() == currentDB.getTotalSpace())
                        Toast.makeText(getActivity(), "Backup successfully saved at external storage!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.w("Settings Backup", e);
            }
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    private void changeLang(String lang) {
        if (lang.equalsIgnoreCase(""))
            return;

        Locale myLocale = new Locale(lang);
        saveLocale(lang);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getActivity().getBaseContext().getResources().updateConfiguration(config, getActivity().getBaseContext().getResources().getDisplayMetrics());

        updateTexts();
    }

    private void saveLocale(String lang) {
        String langPref = "Language";
        SharedPreferences prefs = getActivity().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, lang);
        editor.commit();
    }

    private void updateTexts() {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_settings);

    }
}
