package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.db.GamesDataSource;


public class MainMenuFragment extends Fragment {

    Activity activity;
    GamesDataSource gds;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(R.string.action_main);
        container.removeAllViews();

        Button loadGameButton = (Button) rootView.findViewById(R.id.resumeGameButton);

        gds = ((MainActivity) getActivity()).getGamesDataSource();
        if (gds.getSavedGames().size() == 0) {
            loadGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_grey));
            loadGameButton.setOnClickListener(null);
        }
        else {
            loadGameButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.button_darkblue));
            loadGameButton.setOnClickListener(resumeGameListener);
        }
        return rootView;
    }

    View.OnClickListener resumeGameListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, new LoadGameFragment());
            fragmentTransaction.addToBackStack(getString(R.string.loadGameFragment));
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

            fragmentTransaction.commit();
        }
    };

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public void setKeyListenerOnView(View v) {
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.exit)
                            .setMessage(R.string.exitQuestion)
                            .setIcon(android.R.drawable.ic_menu_help)

                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("EXIT", true);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();

                    return true;
                } else
                    return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setKeyListenerOnView(getView());
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
