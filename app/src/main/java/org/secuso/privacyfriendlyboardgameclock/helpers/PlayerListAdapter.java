package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Quang Anh Dang on 01.12.2017.
 * https://guides.codepath.com/android/Using-the-RecyclerView#creating-the-recyclerview-adapter
 * https://www.youtube.com/watch?v=puyiZKvxBa0
 * TODO DOC
 */
public class PlayerListAdapter extends SelectableAdapter<PlayerListAdapter.ViewHolder> {
    private List<Player> playersList;
    private Activity activity;
    private ItemClickListener itemClickListener;
    private PlayersDataSourceSingleton pdss;

    /**
     *
     * @param activity
     * @param playersList
     * @param itemClickListener the class which implement ItemClickListener Interface. In this case PlayerManagementActivity
     */
    public PlayerListAdapter(Activity activity, List<Player> playersList, ItemClickListener itemClickListener) {
        super();
        this.playersList = playersList;
        this.activity = activity;
        this.itemClickListener = itemClickListener;
        this.pdss = PlayersDataSourceSingleton.getInstance(activity);
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
    public PlayerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate the custom layout
        View playersView = inflater.inflate(R.layout.player_management_custom_row, parent, false);

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
    public void onBindViewHolder(PlayerListAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Player player = playersList.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.playerTextView;
        textView.setText(player.getName());
        ImageView imageView = viewHolder.playerIMGView;
        imageView.setImageBitmap(player.getIcon());

        // Highlight the item with blue if it's simple selected
        if(isSimpleClickedSelected && !isLongClickedSelected){
            viewHolder.longClickedSelectedOverlay.setVisibility(View.INVISIBLE);
            if(isSelected(position)){
                viewHolder.simpleClickedSelectedOverlay.setVisibility(View.VISIBLE);
                viewHolder.selectedPlayerNumber.setText(getSelectedItems().indexOf(position)+1+".");
            }
            else{
                viewHolder.simpleClickedSelectedOverlay.setVisibility(View.INVISIBLE);
                viewHolder.selectedPlayerNumber.setText("");
            }
        }
        // Highlight the item with grey if it's long selected
        else if (!isSimpleClickedSelected && isLongClickedSelected){
            viewHolder.simpleClickedSelectedOverlay.setVisibility(View.INVISIBLE);
            viewHolder.longClickedSelectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        }
        else{
            viewHolder.simpleClickedSelectedOverlay.setVisibility(View.INVISIBLE);
            viewHolder.longClickedSelectedOverlay.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Remove an item from the recycle
     * @param position position von diesem Item
     */
    public void removeItem(int position) {
        pdss.deletePlayer(playersList.get(position));
        playersList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * remove a list of items
     * @param positions a list of position id from items to be removed
     */
    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {
                int count = 1;
                while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                    ++count;
                }

                if (count == 1) {
                    removeItem(positions.get(0));
                } else {
                    removeRange(positions.get(count - 1), count);
                }

                for (int i = 0; i < count; ++i) {
                    positions.remove(0);
                }
            }
        }
    }

    /**
     * remove a range of items
     * @param positionStart the position to start
     * @param itemCount item to be removed
     */
    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            pdss.deletePlayer(playersList.get(positionStart));
            playersList.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return (playersList != null ? playersList.size() : 0);
    }

    public List<Player> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(List<Player> playersList) {
        this.playersList = playersList;
    }

    public Player getPlayer(int posision){
        return playersList.get(posision);
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private ImageView playerIMGView;
        private TextView playerTextView;
        private ItemClickListener itemClickListener;
        private View longClickedSelectedOverlay;
        private View simpleClickedSelectedOverlay;
        private TextView selectedPlayerNumber;

        /**
         *
         * @param itemView
         * @param itemClickListener the class which implement the interface, in this case PlayermanagementActivity
         */
        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            playerIMGView = (ImageView) itemView.findViewById(R.id.player_image);
            playerTextView = (TextView) itemView.findViewById(R.id.player_text);
            simpleClickedSelectedOverlay = itemView.findViewById(R.id.simpleClicked_selected_overlay);
            longClickedSelectedOverlay = itemView.findViewById(R.id.longClicked_selected_overlay);
            selectedPlayerNumber = itemView.findViewById(R.id.selectedPlayerNumberTextView);
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