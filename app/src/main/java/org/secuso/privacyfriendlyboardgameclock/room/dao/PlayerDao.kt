package org.secuso.privacyfriendlyboardgameclock.room.dao

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.secuso.privacyfriendlyboardgameclock.room.model.Player

@Dao
interface PlayerDao {

    @Delete
    fun deletePlayer(player: Player)

    @Query("SELECT _id FROM games INNER JOIN player_game_data ON _id = game_id WHERE player_id = :player")
    fun allGamesOfPlayer(player: Int): List<Int>

    @Query("INSERT INTO players (name, icon) VALUES (:name, :icon)")
    fun addPlayer(name: String, icon: Bitmap?): Long

    @Query("SELECT * FROM players WHERE _id = :player")
    fun getPlayer(player: Int): Player

    @Query("SELECT * FROM players WHERE _id IN (:players)")
    fun getPlayers(players: List<Long>): List<Player>

    @Query("SELECT * FROM players")
    fun allPlayersSync(): List<Player>

    @Query("SELECT * FROM players")
    fun allPlayers(): Flow<List<Player>>


}