package privacyfriendlyexample.org.secuso.boardgameclock.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "database.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_PLAYERS = "players";
    public static final String PLAYERS_COL_ID = "_id";
    public static final String PLAYERS_COL_DATE = "date";
    public static final String PLAYERS_COL_NAME = "name";
    public static final String PLAYERS_COL_ICON = "icon";

    public static final String PLAYERS_SQL_CREATE =
            "CREATE TABLE " + TABLE_PLAYERS +
                    "(" + PLAYERS_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    PLAYERS_COL_DATE + " INTEGER NOT NULL, " +
                    PLAYERS_COL_NAME + " TEXT NOT NULL, " +
                    PLAYERS_COL_ICON + " BLOB NOT NULL);";


    public static final String TABLE_GAMES = "games";
    public static final String GAMES_COL_ID = "_id";
    public static final String GAMES_COL_DATE = "date";
    public static final String GAMES_COL_PLAYERS = "players";
    public static final String GAMES_COL_PLAYERS_ROUNDS = "players_rounds";
    public static final String GAMES_COL_PLAYERS_ROUND_TIMES = "players_times";
    public static final String GAMES_COL_NAME = "name";
    public static final String GAMES_COL_ROUND_TIME = "round_time";
    public static final String GAMES_COL_GAME_TIME = "game_time";
    public static final String GAMES_COL_RESET_ROUND_TIME = "reset_round_time";
    public static final String GAMES_COL_GAME_MODE = "game_mode";
    public static final String GAMES_COL_ROUND_TIME_DELTA = "round_time_delta";
    public static final String GAMES_COL_CURRENT_GAME_TIME = "current_game_time";
    public static final String GAMES_COL_NEXT_PLAYER_INDEX = "next_player_index";
    public static final String GAMES_COL_START_PLAYER_INDEX = "start_player_index";
    public static final String GAMES_COL_SAVED = "saved";
    public static final String GAMES_COL_FINISHED = "finished";


    public static final String GAME_SQL_CREATE =
            "CREATE TABLE " + TABLE_GAMES +
                    "(" + GAMES_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    GAMES_COL_DATE + " INTEGER NOT NULL, " +
                    GAMES_COL_NAME + " TEXT NOT NULL, " +
                    GAMES_COL_PLAYERS + " TEXT NOT NULL, " +
                    GAMES_COL_PLAYERS_ROUND_TIMES + " TEXT NOT NULL, " +
                    GAMES_COL_PLAYERS_ROUNDS + " TEXT NOT NULL, " +
                    GAMES_COL_ROUND_TIME + " INTEGER NOT NULL, " +
                    GAMES_COL_GAME_TIME + " INTEGER NOT NULL, " +
                    GAMES_COL_RESET_ROUND_TIME + " INTEGER NOT NULL, " +
                    GAMES_COL_GAME_MODE + " INTEGER NOT NULL, " +
                    GAMES_COL_ROUND_TIME_DELTA + " INTEGER NOT NULL, " +
                    GAMES_COL_CURRENT_GAME_TIME + " INTEGER NOT NULL, " +
                    GAMES_COL_NEXT_PLAYER_INDEX + " INTEGER NOT NULL, " +
                    GAMES_COL_START_PLAYER_INDEX + " INTEGER NOT NULL, " +
                    GAMES_COL_FINISHED + " INTEGER NOT NULL, " +
                    GAMES_COL_SAVED + " INTEGER NOT NULL);";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(PLAYERS_SQL_CREATE);
            db.execSQL(GAME_SQL_CREATE);
        } catch (Exception ex) {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
