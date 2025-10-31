package org.secuso.privacyfriendlyboardgameclock.room.dao

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import org.secuso.privacyfriendlyboardgameclock.room.model.Player

@Dao
interface PlayerDao {

    @Delete
    fun deletePlayer(player: Player)

    @Query("SELECT _id FROM games INNER JOIN player_game_data ON _id = game_id WHERE player_id = :player")
    fun allGamesOfPlayer(player: Int): List<Int>

    @Insert(entity = Player::class)
    fun addPlayer(name: String, icon: Bitmap): Player

    @Query("SELECT * FROM players WHERE _id = :player")
    fun getPlayer(player: Int): Player

    @Query("SELECT * FROM players WHERE _id IN (:players)")
    fun getPlayers(players: List<Int>): List<Player>

    @Query("SELECT * FROM players")
    fun allPlayers(): List<Player>


}