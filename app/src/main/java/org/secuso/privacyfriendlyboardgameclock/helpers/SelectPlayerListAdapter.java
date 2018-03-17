package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.List;

/**
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 */
public class SelectPlayerListAdapter extends ArrayAdapter { //--CloneChangeRequired
    private List mList; //--CloneChangeRequired
    private Context mContext;

    public SelectPlayerListAdapter(Context context, int textViewResourceId,
                                   List list) { //--CloneChangeRequired
        super(context, textViewResourceId, list);
        this.mList = list;
        this.mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        try {
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.selectplayerlist_item_row, null); //--CloneChangeRequired(list_item)
            }
            final Player p = (Player) mList.get(position); //--CloneChangeRequired
            if (p != null) {
                // setting list_item views
                ((TextView) view.findViewById(R.id.textViewName))
                        .setText(p.getName());

                ((ImageView) view.findViewById(R.id.imageViewIcon))
                        .setImageBitmap(p.getIcon());

                ((TextView) view.findViewById(R.id.textViewNumber))
                        .setText("");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }


}