package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.db.DbHelper;
import org.secuso.privacyfriendlyboardgameclock.db.GamesDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BackupDialog extends DialogFragment {

    MainActivity activity;
    Button importBackupButton, exportBackupButton;
    OnClickListener importBackup = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 7);
            else if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                new AlertDialog.Builder(getActivity())
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
            } else {
                importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            }
        }
    };

    OnClickListener exportBackup = new OnClickListener() {

        @Override
        public void onClick(View v) {


            if (Build.VERSION.SDK_INT >= 23)
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 8);
            else if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                new AlertDialog.Builder(getActivity())
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
            } else {
                importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            }

        }

    };

    Dialog dialog;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        this.getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        dialog = this.getDialog();

        View rootView = inflater.inflate(R.layout.dialog_backup, container, false);

        importBackupButton = (Button) rootView.findViewById(R.id.importBackupButton);
        exportBackupButton = (Button) rootView.findViewById(R.id.exportBackupButton);

        if ((ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
            exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
        }
        else {
            importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
        }

        importBackupButton.setOnClickListener(importBackup);
        exportBackupButton.setOnClickListener(exportBackup);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.getDialog().setTitle(getString(R.string.backup));
        this.getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_menu_rotate);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 7: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                    exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                    new AlertDialog.Builder(getActivity())
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
                } else {
                    importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                    exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                }

                return;
            }
            case 8: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                    exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
                    new AlertDialog.Builder(getActivity())
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
                } else {
                    importBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
                    exportBackupButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
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
                    String currentDBPath = getActivity().getDatabasePath(DbHelper.DB_NAME).getPath();
                    String backupDBPath = "pfa-boardgameclock-database.db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (backupDB.exists()) {
                        copy(backupDB, currentDB);
                    }

                    if (currentDB.exists() && backupDB.length() == currentDB.length())
                        Toast.makeText(getActivity(), R.string.importDatabaseBackupSuccess, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), R.string.noBackupFound, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }

        this.getDialog().cancel();
        updateListView();
        updateResumeGameButton();
    }

    private void exportBackup() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = getActivity().getDatabasePath(DbHelper.DB_NAME).getPath();
                    String backupDBPath = "pfa-boardgameclock-database.db";
                    File currentDB = new File(currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    if (currentDB.exists()) {
                        copy(currentDB, backupDB);
                    }

                    if (backupDB.exists() && backupDB.length() == currentDB.length())
                        Toast.makeText(getActivity(), getString(R.string.exportDatabaseBackupSuccess) + " " + backupDB.getPath(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.backupExportError, Toast.LENGTH_LONG).show();
            }
        this.getDialog().cancel();
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

    private void updateResumeGameButton() {
        Button loadGameButton = (Button) activity.findViewById(R.id.resumeGameButton);

        GamesDataSource gds = activity.getGamesDataSource();
        if (loadGameButton != null)
        if (gds.getSavedGames().size() == 0) {
            loadGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            loadGameButton.setOnClickListener(null);
        } else {
            loadGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
            loadGameButton.setOnClickListener(MainMenuFragment.resumeGameListener);
        }
    }

    private void updateListView(){
        for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++){
            String fragmentName = getFragmentManager().getBackStackEntryAt(i).getName();
            if (fragmentName.equals(getString(R.string.playerManagementFragment)) ||
                    fragmentName.equals(getString(R.string.gameHistoryFragment)) ||
                    fragmentName.equals(getString(R.string.loadGameFragment))){
                System.err.println(fragmentName + ": " + getFragmentManager().findFragmentByTag(fragmentName));
                refreshFragment(fragmentName);
            }
        }
    }

    private void refreshFragment(String fName) {
        Fragment f = null;
        if (fName.equals(getString(R.string.playerManagementFragment)))
            f = new PlayerManagementFragment();
         else if (fName.equals(getString(R.string.gameHistoryFragment)))
            f = new GameHistoryFragment();
        else
            f = new LoadGameFragment();

        final FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, f);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a;

        if (context instanceof Activity) {
            a = (Activity) context;
        }

    }
}
