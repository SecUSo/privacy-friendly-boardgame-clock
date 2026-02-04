package org.secuso.privacyfriendlyboardgameclock.room.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "player_game_data",
    primaryKeys = ["game_id", "player_id"],
)
data class PlayerGameData(
    @ColumnInfo(name = "game_id") var gameId: Long,
    @ColumnInfo(name = "player_id") var playerId: Long,
    @ColumnInfo(name = "rounds") var rounds: Int,
    @ColumnInfo(name = "round_times") var roundTimes: Long
)