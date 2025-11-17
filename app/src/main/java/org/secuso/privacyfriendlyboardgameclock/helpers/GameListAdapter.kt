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
package org.secuso.privacyfriendlyboardgameclock.helpers

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer

/**
 * Created by Quang Anh Dang on 23.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the custom adapter for the game list from GameHistoryActivity
 */
class GameListAdapter
/**
 *
 * @param activity
 * @param games
 * @param itemClickListener the class which implement ItemClickListener Interface. In this case PlayerManagementActivity
 */(
    private val context: Context,
    private val layoutInflater: LayoutInflater,
    _games: MutableList<GameWithPlayer>,
    private val itemClickListener: ItemClickListener?,
) : SelectableAdapter<GameListAdapter.ViewHolder>() {

    var games = _games
        private set
    constructor(
        activity: Activity,
        games: List<GameWithPlayer>,
        itemClickListener: ItemClickListener?,
    ) : this(
        activity,
        activity.layoutInflater,
        games.toMutableList(),
        itemClickListener,
    )

    /**
     * Usually involves inflating a layout from XML and returning the holder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        layoutInflater.inflate(R.layout.gamelist_item_row, parent, false),
        itemClickListener
    )


    /**
     * Involves populating data into the item through holder
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get the data model based on position
        val game = games[position]
        viewHolder.gameName.text = game.game.name
        viewHolder.gameInfo.text = game.game.dateString + ", " + game.players.size + " " + this.context.getString(
            R.string.players
        )

        // if simple selected or long selected
        if (isSimpleClickedSelected && !isLongClickedSelected) {
            if (isSelected(position)) {
                viewHolder.selectedCheckbox.visibility = View.VISIBLE
                viewHolder.selectedCheckbox.setOnCheckedChangeListener(null)
                viewHolder.selectedCheckbox.setChecked(isSelected(position))
                viewHolder.selectedCheckbox.setOnCheckedChangeListener(viewHolder.checkedBoxListener)
            } else viewHolder.selectedCheckbox.visibility = View.INVISIBLE
        } else if (!isSimpleClickedSelected && isLongClickedSelected) {
            viewHolder.selectedCheckbox.visibility = View.VISIBLE
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(null)
            viewHolder.selectedCheckbox.setChecked(isSelected(position))
            viewHolder.selectedCheckbox.setOnCheckedChangeListener(viewHolder.checkedBoxListener)
        } else {
            viewHolder.selectedCheckbox.visibility = View.INVISIBLE
        }
    }

    /**
     * Remove an item from the recycle
     * @param position position von diesem Item
     */
    fun removeItem(position: Int) {
        games.removeAt(position)
        orderedSelectedItems = ArrayList<Int?>()
        notifyItemRemoved(position)
    }

    /**
     * remove a list of items
     * @param positions a list of position id from items to be removed
     */
    fun removeItems(positions: MutableList<Int>) {
        // Reverse-sort the list
        positions.sortWith(Comparator { lhs, rhs -> rhs - lhs })

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size == 1) {
                removeItem(positions[0])
                positions.removeAt(0)
            } else {
                var count = 1
                while (positions.size > count && positions[count] == positions[count - 1] - 1) {
                    ++count
                }

                if (count == 1) {
                    removeItem(positions[0])
                } else {
                    removeRange(positions[count - 1], count)
                }

                for (i in 0..<count) {
                    positions.removeAt(0)
                }
            }
        }
        orderedSelectedItems = ArrayList<Int?>()
    }

    /**
     * remove a range of items
     * @param positionStart the position to start
     * @param itemCount item to be removed
     */
    private fun removeRange(positionStart: Int, itemCount: Int) {
        for (i in 0..<itemCount) {
            games.removeAt(positionStart)
            orderedSelectedItems.remove(positionStart as Int?)
        }
        notifyItemRangeRemoved(positionStart, itemCount)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = games.size


    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/
    class ViewHolder(itemView: View, itemClickListener: ItemClickListener?) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private var itemClickListener: ItemClickListener?
        val gameName: TextView
        val gameInfo: TextView
        val selectedCheckbox: CheckBox
        val rootView: View?
        val checkedBoxListener: CompoundButton.OnCheckedChangeListener = CompoundButton.OnCheckedChangeListener { _,_ -> onClick(rootView) }

        /**
         *
         * @param itemView
         * @param itemClickListener the class which implement the interface, in this case PlayermanagementActivity
         */
        init {
            this.rootView = itemView
            this.gameName = itemView.findViewById<TextView>(R.id.textViewName)
            this.gameInfo = itemView.findViewById<TextView>(R.id.textViewDescription)
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            selectedCheckbox = itemView.findViewById<CheckBox>(R.id.selectedCheckbox)
            selectedCheckbox.setOnCheckedChangeListener(checkedBoxListener)
            this.itemClickListener = itemClickListener
        }

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        override fun onClick(view: View?) {
            if (itemClickListener != null) itemClickListener!!.onItemClick(
                view,
                getAdapterPosition()
            )
        }

        override fun onLongClick(view: View?): Boolean {
            if (itemClickListener != null) return itemClickListener!!.onItemLongClicked(
                view,
                getAdapterPosition()
            )
            return false
        }
    }
}