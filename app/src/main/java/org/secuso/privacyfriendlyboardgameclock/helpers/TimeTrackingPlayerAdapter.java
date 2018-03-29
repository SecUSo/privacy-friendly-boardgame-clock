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

package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.activities.GameTimeTrackingModeActivity;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.List;

/**
 * Created by Quang Anh Dang on 01.12.2017.
 * Tutorial: https://guides.codepath.com/android/Using-the-RecyclerView#creating-the-recyclerview-adapter
 * Tutorial: https://www.youtube.com/watch?v=puyiZKvxBa0
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * Adapter for the player list in Time Tracking Mode
 */
public class TimeTrackingPlayerAdapter extends SelectableAdapter<TimeTrackingPlayerAdapter.ViewHolder> {
    private GameTimeTrackingModeActivity activity;
    private List<Player> playerList;
    private ItemClickListener itemClickListener;

    /**
     *
     * @param activity
     * @param playersList
     * @param itemClickListener the class which implement ItemClickListener Interface. In this case PlayerManagementActivity
     */
    public TimeTrackingPlayerAdapter(GameTimeTrackingModeActivity activity, List<Player> playersList, ItemClickListener itemClickListener) {
        super();
        this.activity = activity;
        this.playerList = playersList;
        this.itemClickListener = itemClickListener;
    }

    public Context getContext() {
        return activity;
    }

    /**
     * Usually involves inflating a layout from XML and returning the holder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override
    public TimeTrackingPlayerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate the custom layout
        View playersView = inflater.inflate(R.layout.time_tracking_player_custom_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(playersView,itemClickListener);
        return viewHolder;
    }

    /**
     * Involves populating data into the item through holder
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(TimeTrackingPlayerAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Player player = playerList.get(position);

        // Set item views based on your views and data model
        TextView playerName = viewHolder.playerName;
        playerName.setText(player.getName());
        ImageView playerIMGView = viewHolder.playerIMGView;
        playerIMGView.setImageBitmap(player.getIcon());

        TextView playerTime = viewHolder.playerTime;
        long currentTimeMS = activity.getPlayerTime().get(player.getId());
        String[] currentTimeArray = getTimeStrings(currentTimeMS);
        String currentTimeString = currentTimeArray[0] + ":" +  currentTimeArray[1] + ":" + currentTimeArray[2] + ":" + currentTimeArray[3];
        playerTime.setText(currentTimeString);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return (playerList != null ? playerList.size() : 0);
    }

    public List<Player> getPlayersList() {
        return playerList;
    }

    public void setPlayersList(List<Player> playersList) {
        this.playerList = playersList;
    }

    public Player getPlayer(int posision){
        return playerList.get(posision);
    }

    /**
     *
     * @param time_ms time in milliseconds
     * @return a String Array list of 4 elements, hour, minutes, seconds and milliseconds
     */
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


    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private ImageView playerIMGView;
        private TextView playerName;
        private TextView playerTime;
        private ItemClickListener itemClickListener;

        /**
         *
         * @param itemView
         * @param itemClickListener the class which implement the interface, in this case PlayermanagementActivity
         */
        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            playerIMGView = itemView.findViewById(R.id.player_image);
            playerName = itemView.findViewById(R.id.player_text);
            playerTime = itemView.findViewById(R.id.current_round_time);
            this.itemClickListener = itemClickListener;
        }

        public void setItemClickListener(ItemClickListener itemClickListener){
            this.itemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            if(itemClickListener != null)
                itemClickListener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(itemClickListener != null)
                return itemClickListener.onItemLongClicked(view,getAdapterPosition());
            return false;
        }
    }
}