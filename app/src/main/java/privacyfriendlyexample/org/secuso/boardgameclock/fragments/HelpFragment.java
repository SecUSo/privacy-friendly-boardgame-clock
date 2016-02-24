package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import privacyfriendlyexample.org.secuso.boardgameclock.R;

/**
 * Created by yonjuni on 12.01.16.
 */
public class HelpFragment extends Fragment {

    Activity activity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.action_help);
        container.removeAllViews();
        return rootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

}
