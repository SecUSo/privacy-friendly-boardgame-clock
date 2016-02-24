package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import privacyfriendlyexample.org.secuso.boardgameclock.R;

public class ResumeGameFragment extends Fragment {

    Activity activity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_resume_game, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.action_resume_game);
        container.removeAllViews();
        return rootView;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

}
