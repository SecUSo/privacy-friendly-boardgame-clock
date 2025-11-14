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
package org.secuso.privacyfriendlyboardgameclock.room.model

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
 * Model for Game Object
 */
@Entity(tableName = "games")
data class Game(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id") override var id: Long = 0,
    @ColumnInfo(name = "name") override var name: String? = null,
    @ColumnInfo(name = "round_time") override var roundTime: Long = 0,
    @ColumnInfo(name = "game_time") override var gameTime: Long = 0,
    @ColumnInfo(name = "reset_round_time") override var resetRoundTime: Int = 0, //0 = false, 1 = true
    @ColumnInfo(name = "game_mode") override var gameMode: Int = 0, //0 = clockwise, 1= counter_clockwise, 2=random, 3  = chess
    @ColumnInfo(name = "round_time_delta") override var roundTimeDelta: Long = -1,
    @ColumnInfo(name = "current_game_time") override var currentGameTime: Long = 0,
    @ColumnInfo(name = "next_player_index") override var nextPlayerIndex: Int = 0,
    @ColumnInfo(name = "start_player_index") override var startPlayerIndex: Int = 0,
    @ColumnInfo(name = "finished") override var finished: Int = 0,
    @ColumnInfo(name = "saved") override var saved: Int = 0,
    @ColumnInfo(name = "chess_mode") override var chessMode: Int = 0,
    @ColumnInfo(name = "is_last_round") override var isLastRound: Int = 0,
    @ColumnInfo(name = "game_time_infinite") override var gameTimeInfinite: Int = 0
): IGame {
    @ColumnInfo(name = "date") var date: Long = System.currentTimeMillis()
        set(date) {
            field = date
            dateString = SimpleDateFormat("dd.MM.yyyy kk:mm").format(Date(date))
        }
    @Ignore var dateString: String = ""
        private set
}

interface IGame {
    var id: Long
    var name: String?
    var roundTime: Long
    var gameTime: Long
    var resetRoundTime: Int
    var gameMode: Int
    var roundTimeDelta: Long
    var currentGameTime: Long
    var nextPlayerIndex: Int
    var startPlayerIndex: Int
    var finished: Int
    var saved: Int
    var chessMode: Int
    var isLastRound: Int
    var gameTimeInfinite: Int
}
