package org.secuso.privacyfriendlyboardgameclock.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "player_game_data",
    foreignKeys = [
        ForeignKey(
            entity = Game::class,
            parentColumns = ["_id"],
            childColumns = ["game_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Player::class,
            parentColumns = ["_id"],
            childColumns = ["player_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("game_id"), Index("player_id")]
)
data class PlayerGameData(
    @ColumnInfo(name = "game_id") var gameId: Int,
    @ColumnInfo(name = "player_id") var playerId: Int,
    @ColumnInfo(name = "rounds") var rounds: Int,
    @ColumnInfo(name = "round_times") var roundTimes: Int
)