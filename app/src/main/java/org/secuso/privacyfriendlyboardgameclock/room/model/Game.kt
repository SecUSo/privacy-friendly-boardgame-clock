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
    @PrimaryKey
    @ColumnInfo(name = "_id") var id: Long = 0,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "round_time") var roundTime: Long = 0,
    @ColumnInfo(name = "game_time") var gameTime: Long = 0,
    @ColumnInfo(name = "reset_round_time") var resetRoundTime: Int = 0, //0 = false, 1 = true
    @ColumnInfo(name = "game_mode") var gameMode: Int = 0, //0 = clockwise, 1= counter_clockwise, 2=random, 3  = chess
    @ColumnInfo(name = "round_time_delta") var roundTimeDelta: Long = -1,
    @ColumnInfo(name = "current_game_time") var currentGameTime: Long = 0,
    @ColumnInfo(name = "next_player_index") var nextPlayerIndex: Int = 0,
    @ColumnInfo(name = "start_player_index") var startPlayerIndex: Int = 0,
    @ColumnInfo(name = "finished") var finished: Int = 0,
    @ColumnInfo(name = "saved") var saved: Int = 0,
    @ColumnInfo(name = "chess_mode") var chessMode: Int = 0,
    @ColumnInfo(name = "is_last_round") var isLastRound: Int = 0,
    @ColumnInfo(name = "game_time_infinite") var gameTimeInfinite: Int = 0
) {
    @ColumnInfo(name = "date") var date: Long = 0
        set(date) {
            field = date
            dateString = SimpleDateFormat("dd.MM.yyyy kk:mm").format(Date(date))
        }
    @Ignore var dateString: String = ""
        private set
}
