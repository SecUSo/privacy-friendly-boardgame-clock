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
package org.secuso.privacyfriendlyboardgameclock.model

import java.text.SimpleDateFormat
import java.util.Date

/**
 * Created by Quang Anh Dang on 24.12.2017.
 * @author Quang Anh Dang
 * Last changed on 18.03.18
 * Model for Game Object
 */
class Game {
    @JvmField
    var id: Long = 0
    @JvmField
    var players: MutableList<Player?>? = null
    @JvmField
    var player_round_times: HashMap<Long?, Long?>? = null
    @JvmField
    var player_rounds: HashMap<Long?, Long?>? = null
    @JvmField
    var name: String? = null
    @JvmField
    var round_time: Long = 0
    @JvmField
    var game_time: Long = 0
    @JvmField
    var reset_round_time: Int = 0 //0 = false, 1 = true
    @JvmField
    var game_time_infinite: Int = 0
    @JvmField
    var game_mode: Int = 0 //0 = clockwise, 1= counter_clockwise, 2=random, 3  = chess
    @JvmField
    var round_time_delta: Long = -1
    @JvmField
    var saved: Int = 0
    @JvmField
    var isLastRound: Int = 0
    var date: Long = 0
        set(date) {
            field = date

            val formatter =
                SimpleDateFormat("dd.MM.yyyy kk:mm")
            dateString = formatter.format(Date(date))
        }
    var dateString: String? = null
        private set
    @JvmField
    var chess_mode: Int = 0
    @JvmField
    var finished: Int = 0
    @JvmField
    var startPlayerIndex: Int = 0
    @JvmField
    var nextPlayerIndex: Int = 0
    @JvmField
    var currentGameTime: Long = 0
}
