package org.secuso.privacyfriendlyboardgameclock.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import org.secuso.privacyfriendlyboardgameclock.room.model.Game
import org.secuso.privacyfriendlyboardgameclock.room.model.GameWithPlayer
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData

@Dao
interface GameDao {

    @Query("SELECT * FROM games")
    fun allGames(): List<GameWithPlayer>

    @Query("SELECT * FROM games WHERE _id = :game")
    fun getGame(game: Long): GameWithPlayer?

    @Query("SELECT * FROM games WHERE _id IN (:games)")
    fun getGames(games: List<Long>): List<GameWithPlayer>

    @Insert
    fun addGame(game: Game)

    @Insert(entity = PlayerGameData::class)
    fun addPlayersToGame(players: List<PlayerGameData>)

    @Update
    fun updateGame(game: Game)

    @Update
    fun updatePlayerData(players: List<PlayerGameData>)

    @Delete
    fun deleteGame(game: Game)

    @Query("SELECT * FROM games WHERE saved = 1")
    fun allSavedGames(): List<GameWithPlayer>

    @Query("SELECT * FROM games WHERE finished = 1")
    fun allFinishedGames(): List<GameWithPlayer>

    @Query("SELECT * FROM games WHERE _id = (SELECT MAX(_id) FROM games)")
    fun getLastGame(): List<Game>

}