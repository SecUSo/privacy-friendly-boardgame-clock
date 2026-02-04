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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.secuso.privacyfriendlyboardgameclock.R
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * This is the adapter for the player result list
 */
class PlayerResultsListAdapter(
    private val layoutInflater: LayoutInflater,
    mContext: Context,
    textViewResourceId: Int,
    private val mList: List<Pair<Player, PlayerGameData>>
) : ArrayAdapter<Pair<Player, PlayerGameData>>(mContext, textViewResourceId, mList) {

    constructor(activity: AppCompatActivity, textViewResourceId: Int, list: List<Pair<Player, PlayerGameData>>)
            : this(activity.layoutInflater, activity, textViewResourceId, list)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView

        try {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.playerresultslist_item_row,parent, false)
            }
            val (player, data) = mList[position]
            // setting list_item views
            view.findViewById<TextView>(R.id.textViewName).text = player.name
            view.findViewById<TextView>(R.id.textViewDescription).text = context.getString(R.string.timeLeft) + " " + getTimeLeft(data.roundTimes)
            view.findViewById<ImageView>(R.id.imageViewIcon).setImageBitmap(player.icon)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view!!
    }

    private fun getTimeLeft(timeLeft: Long): String {
        val times = getTimeStrings(timeLeft)
        return if (times[0] == "00") {
            if (times[1] == "00") times[2] + "'" + times[3] + "s "
            else times[1] + "m " + times[2] + "'" + times[3] + "s "
        } else times[0] + "h " + times[1] + "m " + times[2] + "'" + times[3] + "s "
    }

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
}