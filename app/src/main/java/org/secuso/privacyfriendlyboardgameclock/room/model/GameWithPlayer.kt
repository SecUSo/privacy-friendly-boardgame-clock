package org.secuso.privacyfriendlyboardgameclock.room.model

import androidx.room.Embedded
import androidx.room.Relation

data class GameWithPlayer(
    @Embedded val game: Game,
    @Relation(
        parentColumn = "_id",
        entityColumn = "game_id",
        entity = PlayerGameData::class
    )
    val players: List<PlayerGameData>
): IGame by game