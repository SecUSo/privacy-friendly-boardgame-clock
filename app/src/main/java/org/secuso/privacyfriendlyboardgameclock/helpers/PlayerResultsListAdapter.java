package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameCountDownActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.GameHistoryActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.GameTimeTrackingModeActivity;
import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.List;

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 */
public class PlayerResultsListAdapter extends ArrayAdapter { //--CloneChangeRequired
    private List mList; //--CloneChangeRequired
    private Context mContext;
    private Game game;

    public PlayerResultsListAdapter(Context context, int textViewResourceId,
                                    List list) { //--CloneChangeRequired
        super(context, textViewResourceId, list);
        this.mList = list;
        this.mContext = context;
        if(mContext instanceof GameCountDownActivity){
            game = ((GameCountDownActivity) mContext).getGame();
        }
        else if(mContext instanceof GameHistoryActivity){
            game = ((GameHistoryActivity)mContext).getSelectedGame();
        }
        else if(mContext instanceof GameTimeTrackingModeActivity){
            game = ((GameTimeTrackingModeActivity) mContext).getGame();
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        try {
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.playerresultslist_item_row, null); //--CloneChangeRequired(list_item)
            }
            final Player p = (Player) mList.get(position); //--CloneChangeRequired
            if (p != null) {
                // setting list_item views
                ((TextView) view.findViewById(R.id.textViewName))
                        .setText(p.getName());

                ((TextView) view.findViewById(R.id.textViewDescription))
                        .setText(mContext.getString(R.string.timeLeft) + " " + getTimeLeft(p));

                ((ImageView) view.findViewById(R.id.imageViewIcon))
                        .setImageBitmap(p.getIcon());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private String getTimeLeft(Player p) {

        long timeLeft = game.getPlayer_round_times().get(p.getId());

        String[] times = getTimeStrings(timeLeft);
        if (times[0].equals("00"))
            if (times[1].equals("00"))
                return times[2] + "'" + times[3] + "s ";
            else
                return times[1] + "m " + times[2] + "'" + times[3] + "s ";
        else
            return times[0] + "h " + times[1] + "m " + times[2] + "'" + times[3] + "s ";
    }

    private String[] getTimeStrings(long time_ms) {
        int h = (int) (time_ms / 3600000);
        int m = (int) (time_ms - h * 3600000) / 60000;
        int s = (int) (time_ms - h * 3600000 - m * 60000) / 1000;

        String ms = "0";
        try {
            ms = String.valueOf(String.valueOf(time_ms).charAt(String.valueOf(time_ms).length() - 3));
        } catch (StringIndexOutOfBoundsException e) {
        }
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";

        return new String[]{hh, mm, ss, ms};
    }
}