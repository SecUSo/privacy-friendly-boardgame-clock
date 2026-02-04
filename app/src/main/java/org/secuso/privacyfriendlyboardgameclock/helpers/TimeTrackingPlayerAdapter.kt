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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.databinding.TimeTrackingPlayerCustomRowBinding
import org.secuso.privacyfriendlyboardgameclock.room.model.Player

/**
 * Created by Quang Anh Dang on 01.12.2017.
 * Tutorial: https://guides.codepath.com/android/Using-the-RecyclerView#creating-the-recyclerview-adapter
 * Tutorial: https://www.youtube.com/watch?v=puyiZKvxBa0
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * Adapter for the player list in Time Tracking Mode
 */
class TimeTrackingPlayerAdapter(
    private val layoutInflater: LayoutInflater,
    var playersList: List<Player>,
    private var onClick: (Int) -> Unit,
    val getPlayerTime: (Long) -> Long,
) : SelectableAdapter<TimeTrackingPlayerAdapter.ViewHolder>() {
    /**
     * Usually involves inflating a layout from XML and returning the holder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see .getItemViewType
     * @see .onBindViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = TimeTrackingPlayerCustomRowBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    /**
     * Involves populating data into the item through holder
     * @param viewHolder   The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get the data model based on position
        val player = playersList[position]

        // Set item views based on your views and data model
        val playerName = viewHolder.playerName
        playerName.text = player.name
        val playerIMGView = viewHolder.playerIMGView
        playerIMGView.setImageBitmap(player.icon)

        val playerTime = viewHolder.playerTime
        val currentTimeMS: Long = getPlayerTime(player.id)
        val currentTimeArray = getTimeStrings(currentTimeMS)
        val currentTimeString =
            currentTimeArray[0] + ":" + currentTimeArray[1] + ":" + currentTimeArray[2] + ":" + currentTimeArray[3]
        playerTime.text = currentTimeString

        viewHolder.binding.root.setOnClickListener { onClick(position) }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount() = playersList.size

    fun getPlayer(position: Int) = playersList[position]

    /**
     *
     * @param time_ms time in milliseconds
     * @return a String Array list of 4 elements, hour, minutes, seconds and milliseconds
     */
    private fun getTimeStrings(time_ms: Long): Array<String> {
        val h = (time_ms / 3600000).toInt()
        val m = (time_ms - h * 3600000).toInt() / 60000
        val s = (time_ms - h * 3600000 - m * 60000).toInt() / 1000

        var ms = "0"
        try {
            ms = time_ms.toString().get(time_ms.toString().length - 3).toString()
        } catch (e: StringIndexOutOfBoundsException) {
        }
        val hh = if (h < 10) "0" + h else h.toString() + ""
        val mm = if (m < 10) "0" + m else m.toString() + ""
        val ss = if (s < 10) "0" + s else s.toString() + ""

        return arrayOf<String>(hh, mm, ss, ms)
    }


    /*  #################################################################
        #                                                               #
        #                       Helper class                            #
        #                                                               #
        #################################################################*/
    class ViewHolder(val binding: TimeTrackingPlayerCustomRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val playerIMGView: ImageView = binding.playerImage
        val playerName: TextView = binding.playerText
        val playerTime: TextView = binding.currentRoundTime
    }
}