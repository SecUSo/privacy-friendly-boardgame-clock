/*
 This file is part of Privacy Friendly Board Game Clock.

 Privacy Friendly Board Game Clock is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly App Example is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly App Example. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendlyboardgameclock.model

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * Model for Player Object
 */
@Entity(tableName = "players")
class Player {
    @PrimaryKey
    @ColumnInfo(name = "_id")
    @JvmField
    var id: Long = 0
    private var date: Long = 0

    @Ignore
    var dateString: String? = null
        private set
    private var name: String? = null
    @JvmField
    var icon: Bitmap? = null

    constructor(id: Long, date: Long, name: String, icon: Bitmap?) {
        this.id = id
        this.date = date
        this.name = name
        this.icon = icon
    }

    constructor()

    fun getName(): String {
        return name!!
    }

    fun setName(name: String) {
        this.name = name
    }

    override fun toString(): String {
        return name!!
    }

    fun setDate(date: Long) {
        this.date = date

        val formatter = SimpleDateFormat("dd.MM.yyyy kk:mm")
        dateString = formatter.format(Date(date))
    }
}
