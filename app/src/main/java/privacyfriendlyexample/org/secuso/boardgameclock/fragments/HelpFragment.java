package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;

/**
 * Created by yonjuni on 12.01.16.
 */
public class HelpFragment extends PreferenceFragment {

    Activity activity;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((MainActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_help);

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = super.onCreateView(inflater, container, savedInstanceState);
        container.removeAllViews();
        addPreferencesFromResource(R.xml.preferences);

        return root;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

}
