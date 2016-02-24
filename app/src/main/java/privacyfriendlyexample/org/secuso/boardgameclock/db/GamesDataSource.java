package privacyfriendlyexample.org.secuso.boardgameclock.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.model.Game;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;

public class GamesDataSource {

    private static final String LOG_TAG = GamesDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;

    private String[] columns = {
            DbHelper.GAMES_COL_ID,
            DbHelper.GAMES_COL_NAME,
            DbHelper.GAMES_COL_PLAYERS,
            DbHelper.GAMES_COL_ROUND_TIME,
            DbHelper.GAMES_COL_RESET_ROUND_TIME,
            DbHelper.GAMES_COL_GAME_MODE,
            DbHelper.GAMES_COL_ROUND_TIME_DELTA
    };


    public GamesDataSource(Context context) {
        dbHelper = new DbHelper(context);
        this.context = context;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Game createGame(List<Player> players,
                           String name,
                           int round_time,
                           int reset_round_time,
                           int game_mode,
                           int round_time_delta) {

        ContentValues values = new ContentValues();
        values.put(DbHelper.GAMES_COL_NAME, name);
        values.put(DbHelper.GAMES_COL_ROUND_TIME, round_time);
        values.put(DbHelper.GAMES_COL_RESET_ROUND_TIME, reset_round_time);
        values.put(DbHelper.GAMES_COL_GAME_MODE, game_mode);
        values.put(DbHelper.GAMES_COL_ROUND_TIME_DELTA, round_time_delta);


        // serialize players id's
        String playerIds = "";
        for (Player p : players)
            playerIds = playerIds + p.getId() + ";";
        //remove last semicolon
        playerIds = playerIds.substring(0, playerIds.length() - 1);

        values.put(DbHelper.GAMES_COL_PLAYERS, playerIds);

        System.err.println(values);

        long insertId = database.insert(DbHelper.TABLE_GAMES, null, values);

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, DbHelper.GAMES_COL_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Game game = cursorToGame(cursor);
        cursor.close();

        return game;
    }

    public void deleteGame(Game g) {
        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[]{String.valueOf(g.getId())};

        database.delete(DbHelper.TABLE_GAMES, whereClause, whereArgs);
    }

    private Game cursorToGame(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelper.GAMES_COL_ID);
        int idName = cursor.getColumnIndex(DbHelper.GAMES_COL_NAME);
        int idPlayers = cursor.getColumnIndex(DbHelper.GAMES_COL_PLAYERS);
        int idRound_time = cursor.getColumnIndex(DbHelper.GAMES_COL_ROUND_TIME);
        int idReset_round_time = cursor.getColumnIndex(DbHelper.GAMES_COL_RESET_ROUND_TIME);
        int idGame_mode = cursor.getColumnIndex(DbHelper.GAMES_COL_GAME_MODE);
        int idRound_time_delta = cursor.getColumnIndex(DbHelper.GAMES_COL_ROUND_TIME_DELTA);

        long id = cursor.getLong(idIndex);
        String name = cursor.getString(idName);
        int round_time = cursor.getInt(idRound_time);
        int reset_round_time = cursor.getInt(idReset_round_time);
        int game_mode = cursor.getInt(idGame_mode);
        int round_time_delta = cursor.getInt(idRound_time_delta);

        // deserialize player IDs
        String playerIds = cursor.getString(idPlayers);
        String[] playerIdsArray = playerIds.split(";");
        PlayersDataSource pds = new PlayersDataSource(context);
        pds.open();
        List<Player> players = pds.getPlayersWithIds(playerIdsArray);
        pds.close();

        Game game = new Game();
        game.setId(id);
        game.setName(name);
        game.setRound_time(round_time);
        game.setReset_round_time(reset_round_time);
        game.setGame_mode(game_mode);
        game.setRound_time_delta(round_time_delta);
        game.setPlayers(players);

        return game;
    }

    public List<Game> getAllGames() {
        List<Game> gameList = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Game game;

        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);
            gameList.add(game);
            Log.d(LOG_TAG, "ID: " + game.getId() + ", Name: " + game.getName());
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }
}