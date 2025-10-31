package org.secuso.privacyfriendlyboardgameclock.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.secuso.privacyfriendlyboardgameclock.room.dao.GameDao
import org.secuso.privacyfriendlyboardgameclock.room.dao.PlayerDao
import org.secuso.privacyfriendlyboardgameclock.room.model.Game
import org.secuso.privacyfriendlyboardgameclock.room.model.Player
import org.secuso.privacyfriendlyboardgameclock.room.model.PlayerGameData
import java.io.File

@Database(
    entities = [Game::class, Player::class, PlayerGameData::class],
    version = BoardGameClockDatabase.VERSION
)
@TypeConverters(Converters::class)
abstract class BoardGameClockDatabase: RoomDatabase() {

    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao

    companion object {
        const val VERSION = 2
        const val DATABASE_NAME = "database.db"

        val MIGRATION_1_2 = Migration(1,2) { database ->
            database.execSQL(
                "CREATE TABLE player_game_data (" +
                        "game_id INTEGER NOT NULL, " +
                        "player_id INTEGER NOT NULL," +
                        "rounds INTEGER NOT NULL," +
                        "round_times INTEGER NOT NULL," +
                        "PRIMARY KEY (game_id, player_id)" +
                    ");"
            )
            database.execSQL(
                "CREATE TABLE games_new (" +
                        "_id INTEGER NOT NULL," +
                        "date INTEGER NOT NULL," +
                        "name TEXT NOT NULL," +
                        "round_time INTEGER NOT NULL," +
                        "game_time INTEGER NOT NULL," +
                        "reset_round_time INTEGER NOT NULL," +
                        "game_mode INTEGER NOT NULL," +
                        "round_time_delta INTEGER NOT NULL," +
                        "current_game_time INTEGER NOT NULL," +
                        "next_player_index INTEGER NOT NULL," +
                        "start_player_index INTEGER NOT NULL," +
                        "finished INTEGER NOT NULL," +
                        "saved INTEGER NOT NULL," +
                        "chess_mode INTEGER NOT NULL," +
                        "is_last_round INTEGER NOT NULL," +
                        "game_time_infinite INTEGER NOT NULL," +
                        "PRIMARY KEY (_id)" +
                    ");"
            )
            database.execSQL(
                "INSERT INTO games_new (" +
                        "_id, date, name, round_time, game_time, reset_round_time, game_mode, round_time_delta," +
                        "current_game_time, next_player_index, finished, saved, chess_mode, is_last_round, game_time_infinite" +
                    ") FROM games VALUES (" +
                        "_id, date, name, round_time, game_time, reset_round_time, game_mode, round_time_delta," +
                        "current_game_time, next_player_index, finished, saved, chess_mode, is_last_round, game_time_infinite" +
                    ");"
            )
            val cursor = database.query("SELECT _id, players, players_times, player_rounds FROM games;")
            val insertStatement = database.compileStatement("INSERT INTO player_game_data (game_id, player_id, rounds, round_times) VALUES (?, ?, ?, ?);")

            while (cursor.moveToNext()) {
                val gameId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
                val playersStr = cursor.getString(cursor.getColumnIndexOrThrow("players")) ?: ""
                val timesStr = cursor.getString(cursor.getColumnIndexOrThrow("players_times")) ?: ""
                val roundsStr = cursor.getString(cursor.getColumnIndexOrThrow("player_rounds")) ?: ""

                val players = playersStr.split(";")
                val times = timesStr.split(";")
                val rounds = roundsStr.split(";")

                val count = players.size

                for (i in 0 until count) {
                    insertStatement.clearBindings()
                    insertStatement.bindLong(1, gameId.toLong())
                    insertStatement.bindString(2, players.getOrNull(i) ?: "")
                    insertStatement.bindLong(3, times.getOrNull(i)?.toLongOrNull() ?: 0L)
                    insertStatement.bindLong(4, rounds.getOrNull(i)?.toLongOrNull() ?: 0L)
                    insertStatement.executeInsert()
                }
            }

            database.execSQL("DROP TABLE games;");
            database.execSQL("ALTER TABLE games_new RENAME TO games");
        }


        private var _instance: BoardGameClockDatabase? = null

        private val roomCallback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }

        private val migrations: List<Migration> = listOf(MIGRATION_1_2)

        fun getInstance(context: Context): BoardGameClockDatabase {
            return getInstance(context, DATABASE_NAME)
        }

        fun getInstance(context: Context, databaseName: String): BoardGameClockDatabase {
            if (_instance == null || DATABASE_NAME != databaseName) {
                _instance = Room.databaseBuilder(context.applicationContext, BoardGameClockDatabase::class.java, databaseName)
                    .allowMainThreadQueries()
                    .addMigrations(migrations)
                    .addCallback(roomCallback)
                    .build()
            }
            return _instance!!
        }

        fun getInstance(context: Context, databaseName: String, file: File): BoardGameClockDatabase {
            if (_instance == null) {
                _instance = Room.databaseBuilder(context.applicationContext, BoardGameClockDatabase::class.java, databaseName)
                    .createFromFile(file)
                    .allowMainThreadQueries()
                    .addMigrations(migrations)
                    .addCallback(roomCallback)
                    .build()
            }
            return _instance!!
        }
    }
}


fun <T: RoomDatabase> RoomDatabase.Builder<T>.addMigrations(migrations: List<Migration>): RoomDatabase.Builder<T> {
    migrations.forEach { this.addMigrations(it) }
    return this
}