package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameCountDownActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.PlayerManagementActivity;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerResultsListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;

/**
 * Step 1 in creating new Player Process
 * User choose, if he/she prefers to add new player via Contact or Normal
 * Created by Quang Anh Dang on 03.12.2017.
 * https://guides.codepath.com/android/using-dialogfragment
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 *
 */

public class PlayerManagementChooseModeFragment extends DialogFragment {
    Activity activity;

    public PlayerManagementChooseModeFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static PlayerManagementChooseModeFragment newInstance(String title) {
        PlayerManagementChooseModeFragment frag = new PlayerManagementChooseModeFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(R.string.dialog_choose_new_player)
                .setItems(new CharSequence[]{getString(R.string.new_player), getString(R.string.contact)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        createNewPlayer();
                                        break;
                                    case 1:
                                        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(activity,
                                                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                                            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, TAGHelper.REQUEST_READ_CONTACT_CODE);
                                        else if (ContextCompat.checkSelfPermission(activity,
                                                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
                                            addPlayerFromContacts();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        })
                .setPositiveButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }
                );
        return builder.create();
    }

    private void createNewPlayer() {
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if(prev != null) ft.remove(prev);
        ft.addToBackStack(null);

        // Create and show the dialog
        PlayerManagementCreateNewFragment createNewPlayerFragment = new PlayerManagementCreateNewFragment();
        createNewPlayerFragment.show(ft, "dialog");
    }

    private void addPlayerFromContacts() {
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag("dialog");
        if(prev != null) ft.remove(prev);
        ft.addToBackStack(null);

        // Create and show the dialog
        PlayerManagementContactListFragment createNewPlayerFragment = new PlayerManagementContactListFragment();
        createNewPlayerFragment.show(ft, "dialog");
    }


}
