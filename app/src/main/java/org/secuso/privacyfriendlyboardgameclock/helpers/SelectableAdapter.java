package org.secuso.privacyfriendlyboardgameclock.helpers;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Quang Anh Dang on 10.12.2017.
 * Privacy Friendly Boardgame Clock is licensed under the GPLv3.
 * Copyright (C) 2016-2017  Karola Marky
 * @author Quang Anh Dang
 * This is the parent class for all other Adapter which use RecycleView
 */

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    @SuppressWarnings("unused")
    private static final String TAG = SelectableAdapter.class.getSimpleName();
    boolean isLongClickedSelected = false;
    boolean isSimpleClickedSelected = false;

    private SparseBooleanArray selectedItems;
    List<Integer> orderedSelectedItems = new ArrayList<>();

    public SelectableAdapter() {
        selectedItems = new SparseBooleanArray();
        orderedSelectedItems = new ArrayList<>();
    }

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    public boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        // Keep a list of which elements to notify
        List<Integer> selectedItemsPositions = new ArrayList<>(selectedItems.size());
        for(int i  = 0; i < selectedItems.size(); i++){
            selectedItemsPositions.add(selectedItems.keyAt(i));
        }
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
            if(orderedSelectedItems.contains(position))
                orderedSelectedItems.remove((Integer) position);
        } else {
            selectedItems.put(position, true);
            selectedItemsPositions.add(position);
            orderedSelectedItems.add(position);
        }
        // if item is longclicked selected, notify only 1 item changed
        if(isLongClickedSelected && !isSimpleClickedSelected) notifyItemChanged(position);
        // if item is simple selected, notify all selected item cause the selected player number has to be updated
        else if (!isLongClickedSelected && isSimpleClickedSelected){
            for(Integer i: selectedItemsPositions){
                notifyItemChanged(i);
            }
        }
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        orderedSelectedItems = new ArrayList<>();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return List of selected items ids
     */
    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public SparseBooleanArray getSelectedItemsAsSparseBooleanArray(){
        return selectedItems;
    }

    public boolean isLongClickedSelected() {
        return isLongClickedSelected;
    }


    public boolean isSimpleClickedSelected() {
        return isSimpleClickedSelected;
    }

    public void setLongClickedSelected(boolean longClickedSelected) {
        isLongClickedSelected = longClickedSelected;
    }

    public void setSimpleClickedSelected(boolean simpleClickedSelected) {
        isSimpleClickedSelected = simpleClickedSelected;
    }
}