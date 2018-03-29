/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Board Game Clock is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Board Game Clock. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlyboardgameclock.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.BaseActivity;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.helpers.PlayerResultsListAdapter;
import org.secuso.privacyfriendlyboardgameclock.helpers.TAGHelper;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Quang Anh Dang on 23.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This Dialog shows by touching the "Show Result" Button after a game is finished.
 * Normally the game result is automatically shown after finishing a game
 */

public class GameResultDialogFragment extends DialogFragment {

    BaseActivity activity;
    View rootView;
    ListView players;
    Game game;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        GamesDataSourceSingleton gds = GamesDataSourceSingleton.getInstance(getActivity());
        activity = (BaseActivity) getActivity();
        if(gds == null){
            ((BaseActivity)getActivity()).showMainMenu();
        }else{
            if(((BaseActivity)getActivity()).checkIfSingletonDataIsCorrupt()) return null;
        }

        game = gds.getGame();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(game.getName())
                .setPositiveButton(R.string.backToMainMenu,
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

        // Set TextView for game mode
        TextView gameModeTV = v.findViewById(R.id.gameModeText);
        String[] gameModes = getResources().getStringArray(R.array.game_modes);
        switch (game.getGame_mode()){
            case TAGHelper.CLOCKWISE:
                gameModeTV.setText(gameModes[TAGHelper.CLOCKWISE]);
                break;
            case TAGHelper.COUNTER_CLOCKWISE:
                gameModeTV.setText(gameModes[TAGHelper.COUNTER_CLOCKWISE]);
                break;
            case TAGHelper.RANDOM:
                gameModeTV.setText(gameModes[TAGHelper.RANDOM]);
                break;
            case TAGHelper.MANUAL_SEQUENCE:
                gameModeTV.setText(gameModes[TAGHelper.MANUAL_SEQUENCE]);
                break;
            case TAGHelper.TIME_TRACKING:
                gameModeTV.setText(gameModes[TAGHelper.TIME_TRACKING]);
                break;
            default:
                break;
        }

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
