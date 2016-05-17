package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import privacyfriendlyexample.org.secuso.boardgameclock.R;

public class BackupDialog extends DialogFragment {

    Activity activity;
    Button importBackupButton, exportBackupButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.dialog_backup, container, false);

        getDialog().setTitle(getString(R.string.backup));
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        importBackupButton = (Button) rootView.findViewById(R.id.importBackupButton);
        exportBackupButton = (Button) rootView.findViewById(R.id.exportBackupButton);

        int readExtStoragePermissionCheck = ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExtStoragePermissionCheck = ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (readExtStoragePermissionCheck == PackageManager.PERMISSION_GRANTED)
            importBackupButton.setOnClickListener(importBackup);
        else
            importBackupButton.setVisibility(View.INVISIBLE);

        if (writeExtStoragePermissionCheck == PackageManager.PERMISSION_GRANTED)
            exportBackupButton.setOnClickListener(exportBackup);
        else
            exportBackupButton.setVisibility(View.INVISIBLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_menu_rotate);
    }

    OnClickListener importBackup = new OnClickListener() {
        @Override
        public void onClick(View v) {
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

        }
    };

    private void importBackup() {

        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/privacyfriendlyexample.org.secuso.boardgameclock/databases/database.db";
                    String backupDBPath = "pfa-boardgameclock-database.db";
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
                        Toast.makeText(getActivity(), R.string.importDatabaseBackupSuccess, Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getActivity(), R.string.noBackupFound, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }
    }

    OnClickListener exportBackup = new OnClickListener() {

        @Override
        public void onClick(View v) {
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

        }

    };

    private void exportBackup() {
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "/data/privacyfriendlyexample.org.secuso.boardgameclock/databases/database.db";
                    String backupDBPath = "pfa-boardgameclock-database.db";
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
                        Toast.makeText(getActivity(), getString(R.string.exportDatabaseBackupSuccess) + " " + backupDB.getPath(), Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
            }
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
