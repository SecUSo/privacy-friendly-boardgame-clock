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

package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;

/**
 * Step 1 in creating new Player Process
 * User choose, if he/she prefers to add new player via Contact or Normal
 * Created by Quang Anh Dang on 03.12.2017.
 * https://guides.codepath.com/android/using-dialogfragment
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
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
