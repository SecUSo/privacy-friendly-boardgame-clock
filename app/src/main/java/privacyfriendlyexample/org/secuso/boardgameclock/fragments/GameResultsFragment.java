package privacyfriendlyexample.org.secuso.boardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.view.PlayerResultsListAdapter;

public class GameResultsFragment extends Fragment {

    MainActivity activity;
    View rootView;
    ListView players;
    Game game;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();

        rootView = inflater.inflate(R.layout.fragment_game_results, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(activity.getString(R.string.gameResults));
        container.removeAllViews();

        game = ((MainActivity) activity).getHistoryGame();

        ((TextView) rootView.findViewById(R.id.timePlayedText)).setText(getTotalTimePlayed());
        ((TextView) rootView.findViewById(R.id.roundsPlayedText)).setText(String.valueOf(getLastRound()));

        players = (ListView) rootView.findViewById(R.id.players_results_list);
        PlayerResultsListAdapter listAdapter = new PlayerResultsListAdapter(this.getActivity(), R.id.choose_players_list, game.getPlayers());
        players.setAdapter(listAdapter);

        return rootView;
    }

    private String getTotalTimePlayed() {
        long game_time = game.getGame_time();
        long current_game_time = game.getCurrentGameTime();
        long totalTimePlayed = game_time - current_game_time;

        String[] times = getTimeStrings(totalTimePlayed);

        if (times[0].equals("00"))
            if (times[1].equals("00"))
                return times[2] + "s ";
            else
                return times[1] + "m " + times[2] + "s ";
        else
            return times[0] + "h " + times[1] + "m " + times[2] + "s ";

    }

    private String[] getTimeStrings(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        return new String[]{hh, mm, ss};
    }

    private long getLastRound() {
        HashMap<Long, Long> playerRounds = game.getPlayer_rounds();
        long lastRound = Collections.max(playerRounds.values());
        if (lastRound == Collections.min(playerRounds.values()))
            return lastRound;
        else
            return Collections.max(playerRounds.values()) - 1;
    }
    public void setKeyListenerOnView(View v) {
        v.setFocusableInTouchMode(true);
        v.requestFocus();
        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_BACK) && (event.getAction() == KeyEvent.ACTION_DOWN)) {

                    boolean gameHistoryInBackground = false;
                    for (int i = 0; i < getFragmentManager().getBackStackEntryCount(); i++) {
                        if (getFragmentManager().getBackStackEntryAt(i).getName().equals(getString(R.string.gameHistoryFragment)))
                            gameHistoryInBackground = true;
                    }

                    if (!gameHistoryInBackground) {
                        showMainMenu();
                    } else {
                        activity.onBackPressed();
                    }
                }

                return true;
            }

        });
    }

    private void showMainMenu() {

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(null);

        getFragmentManager().popBackStack(getString(R.string.mainMenuFragment), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new MainMenuFragment());
        fragmentTransaction.addToBackStack(getString(R.string.mainMenuFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragmentTransaction.commit();

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
