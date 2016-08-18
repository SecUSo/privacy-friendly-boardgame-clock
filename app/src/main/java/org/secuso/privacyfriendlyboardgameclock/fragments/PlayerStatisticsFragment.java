package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.db.GamesDataSource;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.secuso.privacyfriendlyboardgameclock.view.GamesListAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerStatisticsFragment extends Fragment {

    MainActivity activity;
    View rootView;
    ListView games;
    Player player;
    List<Game> playerGames;
    GamesDataSource gds;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = (MainActivity) getActivity();
        player = activity.getPlayerForEditing();
        gds = activity.getGamesDataSource();
        playerGames = gds.getGamesOfPlayer(player);

        rootView = inflater.inflate(R.layout.fragment_player_statistics, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(player.getName());
        container.removeAllViews();

        ((TextView) rootView.findViewById(R.id.totalTimePlayedText)).setText(getTotalTimePlayed());
        ((TextView) rootView.findViewById(R.id.completedRoundsText)).setText(String.valueOf(getTotalRoundsPlayed()));

        GamesListAdapter listAdapter = new GamesListAdapter(this.getActivity(), R.id.games_list, playerGames);
        games = (ListView) rootView.findViewById(R.id.games_list);
        games.setAdapter(listAdapter);

        return rootView;
    }

    private String getTotalTimePlayed() {
        long totalTimePlayed = 0;
        int infiniteCount = 0;

        for (Game g : playerGames) {
            if (g.getGame_time_infinite() == 1)
                infiniteCount++;
            else {
                long game_time = g.getGame_time();
                long current_game_time = g.getCurrentGameTime();

                totalTimePlayed += (1000 + game_time - current_game_time);
            }
        }

        totalTimePlayed = totalTimePlayed - infiniteCount * 1000;

        String[] times = getTimeStrings(totalTimePlayed);
        if (times[0].equals("00"))
            if (times[1].equals("00"))
                return times[2] + "s";
            else
                return times[1] + "m " + times[2] + "s";
        else
            return times[0] + "h " + times[1] + "m " + times[2] + "s";

    }

    private String[] getTimeStrings(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;

        String ms = "0";
        try {
            ms = String.valueOf(String.valueOf(time_ms).charAt(String.valueOf(time_ms).length() - 3));
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        return new String[]{hh, mm, ss, ms};
    }

    private int getTotalRoundsPlayed() {
        int totalRoundsPlayed = 0;
        for (Game g : playerGames)
            totalRoundsPlayed += (getLastRound(g) - 1);

        return totalRoundsPlayed;
    }

    private long getLastRound(Game g) {
        HashMap<Long, Long> playerRounds = g.getPlayer_rounds();
        long lastRound = Collections.max(playerRounds.values());
        if (lastRound == Collections.min(playerRounds.values()))
            return lastRound;
        else
            return Collections.max(playerRounds.values()) - 1;
    }

    public void showResults() {

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, new GameResultsFragment());
        fragmentTransaction.addToBackStack(getString(R.string.gameResultsFragment));
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();

    }

    private void refreshFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
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
