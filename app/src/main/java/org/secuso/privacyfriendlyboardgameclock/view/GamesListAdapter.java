package org.secuso.privacyfriendlyboardgameclock.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.model.Game;

import java.util.List;

public class GamesListAdapter extends ArrayAdapter { //--CloneChangeRequired
    private List mList; //--CloneChangeRequired
    private Context mContext;

    public GamesListAdapter(Context context, int textViewResourceId,
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
                view = vi.inflate(R.layout.gamelist_item_row, null); //--CloneChangeRequired(list_item)
            }
            final Game g = (Game) mList.get(position); //--CloneChangeRequired
            if (g != null) {
                // setting list_item views
                ((TextView) view.findViewById(R.id.textViewName))
                        .setText(g.getName());

                ((TextView) view.findViewById(R.id.textViewDescription))
                        .setText(g.getDateString() + ", " + g.getPlayers().size() + " " + mContext.getString(R.string.players));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}