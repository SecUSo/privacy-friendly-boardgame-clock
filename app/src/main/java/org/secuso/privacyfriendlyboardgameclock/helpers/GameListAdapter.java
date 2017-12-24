package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.GamesDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Quang Anh Dang on 23.12.2017.
 *
 * @author Quang Anh Dang
 */
public class GameListAdapter extends SelectableAdapter<GameListAdapter.ViewHolder> {
    private List<Game> gamesList;
    private Activity activity;
    private ItemClickListener itemClickListener;
    private GamesDataSourceSingleton gdss;

    /**
     *
     * @param activity
     * @param gamesList
     * @param itemClickListener the class which implement ItemClickListener Interface. In this case PlayerManagementActivity
     */
    public GameListAdapter(Activity activity, List<Game> gamesList, ItemClickListener itemClickListener) {
        super();
        this.gamesList = gamesList;
        this.activity = activity;
        this.itemClickListener = itemClickListener;
        this.gdss = GamesDataSourceSingleton.getInstance(activity);
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
     */
    @Override
    public GameListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = activity.getLayoutInflater();

        // Inflate the custom layout
        View gamesView = inflater.inflate(R.layout.gamelist_item_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(gamesView,itemClickListener);
        return viewHolder;
    }


    /**
     * Involves populating data into the item through holder
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(GameListAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Game game = gamesList.get(position);
        if(game != null){
            TextView gameName = viewHolder.gameName;
            gameName.setText(game.getName());
            TextView gameInfo = viewHolder.gameInfo;
            gameInfo.setText(game.getDateString() + ", " + game.getPlayers().size() + " " + getContext().getString(R.string.players));

            // Highlight the item with grey if it's long selected
            viewHolder.longClickedSelectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    /**
     * Remove an item from the recycle
     * @param position position von diesem Item
     */
    public void removeItem(int position) {
        gdss.deleteGame(gamesList.get(position));
        gamesList.remove(position);
        orderedSelectedItems = new ArrayList<>();
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
        orderedSelectedItems = new ArrayList<>();
    }

    /**
     * remove a range of items
     * @param positionStart the position to start
     * @param itemCount item to be removed
     */
    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            gdss.deleteGame(gamesList.get(positionStart));
            gamesList.remove(positionStart);
            orderedSelectedItems.remove((Integer)positionStart);
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
        return (gamesList != null ? gamesList.size() : 0);
    }

    public List<Game> getPlayersList() {
        return gamesList;
    }

    public void setPlayersList(List<Game> gamesList) {
        this.gamesList = gamesList;
    }

    public Game getGame(int posision){
        return gamesList.get(posision);
    }

    /**
     *
     * @return an ordered list of selected Players
     */
    public List<Game> getOrderdSelectedGames (){
        List<Game> result = new ArrayList<>();
        for(Integer i: orderedSelectedItems){
            result.add(getGame(i));
        }
        return result;
    }

    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        private ItemClickListener itemClickListener;
        private View longClickedSelectedOverlay;
        private TextView gameName;
        private TextView gameInfo;

        /**
         *
         * @param itemView
         * @param itemClickListener the class which implement the interface, in this case PlayermanagementActivity
         */
        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.gameName = itemView.findViewById(R.id.textViewName);
            this.gameInfo = itemView.findViewById(R.id.textViewDescription);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            longClickedSelectedOverlay = itemView.findViewById(R.id.longClicked_selected_overlay);
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