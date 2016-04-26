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
import privacyfriendlyexample.org.secuso.boardgameclock.activities.MainActivity;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

public class PlayerResultsListAdapter extends ArrayAdapter { //--CloneChangeRequired
    private List mList; //--CloneChangeRequired
    private Context mContext;
    private Game game;

    public PlayerResultsListAdapter(Context context, int textViewResourceId,
                                    List list) { //--CloneChangeRequired
        super(context, textViewResourceId, list);
        this.mList = list;
        this.mContext = context;

        game = ((MainActivity) mContext).getGame();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        try {
            if (view == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = vi.inflate(R.layout.playerlist_item_row, null); //--CloneChangeRequired(list_item)
            }
            final Player p = (Player) mList.get(position); //--CloneChangeRequired
            if (p != null) {
                // setting list_item views
                ((TextView) view.findViewById(R.id.textViewName))
                        .setText(p.getName());

                long round = game.getPlayer_rounds().get(p.getId()) - 1;
                if (game.getPlayers().get(game.getNextPlayerIndex()) == p)
                    round++;

                ((TextView) view.findViewById(R.id.textViewDescription))
                        .setText(mContext.getString(R.string.lastRound) + " " + (game.getPlayer_rounds().get(p.getId()) - 1) + ", " +
                                mContext.getString(R.string.timeLeft) + " " + getTimeLeft(p));

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

        String[] times = getTimeStrings(timeLeft*1000);
        if (times[0].equals("00"))
            if (times[1].equals("00"))
                return times[2] +"s ";
            else
                return times[1] + "m " + times[2] +"s ";
        else
            return times[0] + "h " + times[1] + "m " + times[2] +"s ";
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
}