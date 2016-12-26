package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;

public class AboutFragment extends Fragment {

    Activity activity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(R.string.action_about);

        TextView secusoLink = (TextView) rootView.findViewById(R.id.secuso_link);
        TextView githubLink = (TextView) rootView.findViewById(R.id.github_link);

        secusoLink.setMovementMethod(LinkMovementMethod.getInstance());
        githubLink.setMovementMethod(LinkMovementMethod.getInstance());

        container.removeAllViews();
        return rootView;
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