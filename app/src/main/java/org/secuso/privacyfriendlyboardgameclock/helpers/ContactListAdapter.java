package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import org.secuso.privacyfriendlyboardgameclock.R;
import org.secuso.privacyfriendlyboardgameclock.database.PlayersDataSourceSingleton;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Quang Anh Dang on 01.12.2017.
 * Tutorial: https://guides.codepath.com/android/Using-the-RecyclerView#creating-the-recyclerview-adapter
 * Tutorial: https://www.youtube.com/watch?v=puyiZKvxBa0
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 */
public class ContactListAdapter extends SelectableAdapter<ContactListAdapter.ViewHolder> {
    private Activity activity;
    private ItemClickListener itemClickListener;
    private PlayersDataSourceSingleton pdss;
    // Because RecyclerView.Adapter in its current form doesn't natively
    // support cursors, we wrap a CursorAdapter that will do all the job
    // for us.
    SimpleCursorAdapter mCursorAdapter;

    /**
     *
     * @param activity
     * @param itemClickListener the class which implement ItemClickListener Interface. In this case PlayerManagementActivity
     * @param cursor the simple cursor
     */
    public ContactListAdapter(Activity activity, ItemClickListener itemClickListener, SimpleCursorAdapter cursor) {
        super();
        this.activity = activity;
        this.itemClickListener = itemClickListener;
        this.pdss = PlayersDataSourceSingleton.getInstance(activity);
        this.mCursorAdapter = cursor;
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
    public ContactListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(ContactListAdapter.ViewHolder viewHolder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position); //EDITED: added this line as suggested in the comments below, thanks :)
        mCursorAdapter.bindView(viewHolder.itemView, activity, mCursorAdapter.getCursor());
        if(isSimpleClickedSelected && !isLongClickedSelected){
            viewHolder.selectedCheckbox.setVisibility(View.VISIBLE);
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(null);
            viewHolder.selectedCheckbox.setChecked(isSelected(position));
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(viewHolder.checkedBoxListener);
        }
        else viewHolder.selectedCheckbox.setVisibility(View.INVISIBLE);

    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public SimpleCursorAdapter getCursorAdapter() {
        return mCursorAdapter;
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
        private CheckBox selectedCheckbox;
        private View rootView;
        private CompoundButton.OnCheckedChangeListener checkedBoxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                onClick(rootView);
            }
        };

        /**
         *
         * @param itemView
         * @param itemClickListener the class which implement the interface
         */
        public ViewHolder(View itemView, ItemClickListener itemClickListener) {
            super(itemView);
            this.rootView = itemView;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            playerIMGView = (ImageView) itemView.findViewById(R.id.player_image);
            playerTextView = (TextView) itemView.findViewById(R.id.player_text);
            selectedCheckbox = itemView.findViewById(R.id.selectedCheckbox);
            selectedCheckbox.setOnCheckedChangeListener(checkedBoxListener);
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