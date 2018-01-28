package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.PlayerManagementActivity;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.GameListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.GamesListAdapterOld;
import org.secuso.privacyfriendlyboardgameclock.helpers.ItemClickListener;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerManagementStatisticsFragment extends DialogFragment implements ItemClickListener {

    private PlayerManagementActivity activity;
    private View rootView;
    private ListView games;
    private Player player;
    private List<Game> playerGames;
    private GamesDataSourceSingleton gds;
    private GameListAdapter gameListAdapter;

    public PlayerManagementStatisticsFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static PlayerManagementStatisticsFragment newInstance(String title){
        PlayerManagementStatisticsFragment frag = new PlayerManagementStatisticsFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (PlayerManagementActivity) getActivity();
        player = activity.getPlayerToEdit();
        gds = GamesDataSourceSingleton.getInstance(activity);
        playerGames = gds.getGamesOfPlayer(player);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(player.getName())
                .setPositiveButton(R.string.okay,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.onBackPressed();
                            }
                        }
                );

        rootView = activity.getLayoutInflater().inflate(R.layout.fragment_player_statistics, null);

        ((TextView) rootView.findViewById(R.id.totalTimePlayedText)).setText(getTotalTimePlayed());
        ((TextView) rootView.findViewById(R.id.completedRoundsText)).setText(String.valueOf(getTotalRoundsPlayed()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView gamesRecycleView = rootView.findViewById(R.id.game_list);
        gamesRecycleView.setHasFixedSize(true);
        gameListAdapter= new GameListAdapter(this.getActivity(), playerGames, this);
        gamesRecycleView.setAdapter(gameListAdapter);
        gamesRecycleView.setLayoutManager(layoutManager);
        // Set max height for list
        /*ViewGroup.LayoutParams params = gamesRecycleView.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.recycle_view_popup_height);
        gamesRecycleView.setLayoutParams(params);*/

        builder.setView(rootView);
        return builder.create();
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
                // Time for time tracking mode is different
                if(g.getGame_mode() == TAGHelper.TIME_TRACKING)
                    totalTimePlayed += 1000 + current_game_time;
                else totalTimePlayed += (1000 + game_time - current_game_time);
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

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public boolean onItemLongClicked(View view, int position) {
        return false;
    }
}
