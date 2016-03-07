package privacyfriendlyexample.org.secuso.boardgameclock.view;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

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
                view = vi.inflate(R.layout.listview_item_row, null); //--CloneChangeRequired(list_item)
            }
            final Game g = (Game) mList.get(position); //--CloneChangeRequired
            if (g != null) {
                // setting list_item views
                ((TextView) view.findViewById(R.id.textViewName))
                        .setText(g.getName());

                ((TextView) view.findViewById(R.id.textViewDescription))
                        .setText(String.valueOf(g.getId()));

                ((ImageView) view.findViewById(R.id.imageViewIcon))
                        .setVisibility(View.INVISIBLE);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }
}