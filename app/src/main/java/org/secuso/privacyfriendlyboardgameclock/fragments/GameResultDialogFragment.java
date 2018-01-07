package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameCountDownActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.GameHistoryActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerResultsListAdapter;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Quang Anh Dang on 23.12.2017.
 *
 * @author Quang Anh Dang
 */

public class GameResultDialogFragment extends DialogFragment {

    GameCountDownActivity activity;
    View rootView;
    ListView players;
    Game game;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (GameCountDownActivity) getActivity();
        game = activity.getGame();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(game.getName())
                .setPositiveButton(R.string.okay,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                activity.showMainMenu();
                            }
                        }
                );
        LayoutInflater inflater = activity.getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_game_results,null);
        ((TextView)v.findViewById(R.id.timePlayedText)).setText(getTimeLeft());
        ((TextView)v.findViewById(R.id.roundsPlayedText)).setText(String.valueOf(getLastRound()));

        ListView players = (ListView) v.findViewById(R.id.list);
        PlayerResultsListAdapter listAdapter = new PlayerResultsListAdapter(this.getActivity(), R.id.list, game.getPlayers());
        players.setAdapter(listAdapter);

        builder.setView(v);
        return builder.create();
    }

    private String getTimeLeft() {
        String[] times = getTimeStrings(game.getCurrentGameTime());

        if (game.getGame_time_infinite() == 1)
            return activity.getString(R.string.infinite);

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

    private long getLastRound() {
        HashMap<Long, Long> playerRounds = game.getPlayer_rounds();
        long lastRound = Collections.max(playerRounds.values());
        if (lastRound == Collections.min(playerRounds.values()))
            return lastRound;
        else
            return Collections.max(playerRounds.values()) - 1;
    }
}
