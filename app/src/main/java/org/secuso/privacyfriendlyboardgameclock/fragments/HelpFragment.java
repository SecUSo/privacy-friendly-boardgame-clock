package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;

public class HelpFragment extends PreferenceFragment {

    Activity activity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_help);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        return root;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

}
