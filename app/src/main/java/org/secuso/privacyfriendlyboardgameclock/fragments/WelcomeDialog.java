package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import org.secuso.privacyfriendlyboardgameclock.R;

public class WelcomeDialog extends DialogFragment {

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater i = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(i.inflate(R.layout.dialog_welcome, null));
        builder.setIcon(R.drawable.drawer);
        builder.setTitle(R.string.welcome);
        builder.setNegativeButton(getActivity().getString(R.string.viewHelp), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showHelpFragment();
            }
        });
        builder.setPositiveButton(getActivity().getString(R.string.ok), null);

        return builder.create();
    }

    private void showHelpFragment() {
        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new HelpFragment());
        fragmentTransaction.addToBackStack(getActivity().getString(R.string.helpFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();
    }
}