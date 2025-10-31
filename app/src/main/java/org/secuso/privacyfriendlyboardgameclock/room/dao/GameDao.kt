package org.secuso.privacyfriendlyboardgameclock.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer

@Dao
interface GameDao {

    @Query("SELECT * FROM games")
    fun allGames(): List<GameWithPlayer>

    @Query("SELECT * FROM games WHERE _id = :game")
    fun getGame(game: Int)

    @Query("SELECT * FROM games WHERE _id IN (:games)")
    fun getGames(games: List<Int>): List<GameWithPlayer>

    @Insert
    fun addGame(game: GameWithPlayer)

    @Update
    fun updateGame(game: GameWithPlayer)

    @Delete
    fun deleteGame(game: GameWithPlayer)

    @Query("SELECT * FROM games WHERE saved = 1")
    fun allSavedGames(): List<GameWithPlayer>

    @Query("SELECT * FROM games WHERE finished = 1")
    fun allFinishedGames(): List<GameWithPlayer>

}