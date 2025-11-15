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

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * The Adapter for playerlist for manual sequence mode
 */
class SelectPlayerListAdapter(
    private val layoutInflater: LayoutInflater,
    mContext: Context,
    textViewResourceId: Int,
    private val mList: List<Player>
) : ArrayAdapter<Player>(mContext, textViewResourceId, mList) {
    constructor(activity: AppCompatActivity, textViewResourceId: Int, list: List<Player>)
        : this(activity.layoutInflater, activity, textViewResourceId, list)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        try {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.selectplayerlist_item_row, parent, false)
            }
            mList[position].apply {
                view.findViewById<TextView>(R.id.textViewName).text = name
                view.findViewById<ImageView>(R.id.imageViewIcon).setImageBitmap(icon)
                view.findViewById<TextView>(R.id.textViewNumber).text = ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return view!!
    }
}