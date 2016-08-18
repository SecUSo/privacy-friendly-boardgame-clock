package org.secuso.privacyfriendlyboardgameclock.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.secuso.privacyfriendlyboardgameclock.activities.MainActivity;
import org.secuso.privacyfriendlyboardgameclock.model.Game;
import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GamesDataSource {

    private SQLiteDatabase database;
    private DbHelper dbHelper;
    private Context context;
    private Game game;
    private PlayersDataSource pds;

    private String[] columns = {
            DbHelper.GAMES_COL_ID,
            DbHelper.GAMES_COL_DATE,
            DbHelper.GAMES_COL_NAME,
            DbHelper.GAMES_COL_PLAYERS,
            DbHelper.GAMES_COL_PLAYERS_ROUND_TIMES,
            DbHelper.GAMES_COL_PLAYERS_ROUNDS,
            DbHelper.GAMES_COL_ROUND_TIME,
            DbHelper.GAMES_COL_RESET_ROUND_TIME,
            DbHelper.GAMES_COL_GAME_MODE,
            DbHelper.GAMES_COL_ROUND_TIME_DELTA,
            DbHelper.GAMES_COL_GAME_TIME,
            DbHelper.GAMES_COL_CURRENT_GAME_TIME,
            DbHelper.GAMES_COL_NEXT_PLAYER_INDEX,
            DbHelper.GAMES_COL_START_PLAYER_INDEX,
            DbHelper.GAMES_COL_SAVED,
            DbHelper.GAMES_COL_FINISHED,
            DbHelper.GAMES_COL_GAME_TIME_INFINITE,
            DbHelper.GAMES_COL_CHESS_MODE,
            DbHelper.GAMES_COL_IS_LAST_ROUND
    };


    public GamesDataSource(Context context) {
        dbHelper = new DbHelper(context);

        this.pds = ((MainActivity) context).getPlayersDataSource();
        this.context = context;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Game createGame(long date,
                           List<Player> players,
                           HashMap<Long, Long> player_round_times,
                           HashMap<Long, Long> player_rounds,
                           String name,
                           long round_time,
                           long game_time,
                           int reset_round_time,
                           int game_mode,
                           long round_time_delta,
                           long currentGameTime,
                           int nextPlayerIndex,
                           int startPlayerIndex,
                           int saved, int finished, int game_time_infinite, int chess_mode, int isLastRound) {

        final ContentValues values = new ContentValues();
        values.put(DbHelper.GAMES_COL_NAME, name);
        values.put(DbHelper.GAMES_COL_DATE, date);
        values.put(DbHelper.GAMES_COL_GAME_TIME, game_time);
        values.put(DbHelper.GAMES_COL_ROUND_TIME, round_time);
        values.put(DbHelper.GAMES_COL_RESET_ROUND_TIME, reset_round_time);
        values.put(DbHelper.GAMES_COL_GAME_MODE, game_mode);
        values.put(DbHelper.GAMES_COL_ROUND_TIME_DELTA, round_time_delta);
        values.put(DbHelper.GAMES_COL_CURRENT_GAME_TIME, currentGameTime);
        values.put(DbHelper.GAMES_COL_NEXT_PLAYER_INDEX, nextPlayerIndex);
        values.put(DbHelper.GAMES_COL_START_PLAYER_INDEX, startPlayerIndex);
        values.put(DbHelper.GAMES_COL_SAVED, saved);
        values.put(DbHelper.GAMES_COL_FINISHED, finished);
        values.put(DbHelper.GAMES_COL_GAME_TIME_INFINITE, game_time_infinite);
        values.put(DbHelper.GAMES_COL_CHESS_MODE, chess_mode);
        values.put(DbHelper.GAMES_COL_IS_LAST_ROUND, isLastRound);


        // serialize players id's
        String playerIds = "";
        for (Player p : players)
            playerIds = playerIds + p.getId() + ";";
        //remove last semicolon
        playerIds = playerIds.substring(0, playerIds.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS, playerIds);

        // serialize player round times
        String playerRoundTimes = "";
        for (Player p : players)
            playerRoundTimes = playerRoundTimes + player_round_times.get(p.getId()) + ";";
        //remove last semicolon
        playerRoundTimes = playerRoundTimes.substring(0, playerRoundTimes.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS_ROUND_TIMES, playerRoundTimes);

        // serialize player rounds
        String playerRounds = "";
        for (Player p : players)
            playerRounds = playerRounds + player_rounds.get(p.getId()) + ";";
        //remove last semicolon
        playerRounds = playerRounds.substring(0, playerRounds.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS_ROUNDS, playerRounds);

        long insertId = database.insert(DbHelper.TABLE_GAMES, null, values);
        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, DbHelper.GAMES_COL_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        game = cursorToGame(cursor);
        cursor.close();

        return game;
    }

    public Game getGameWithId(final String gameId) {

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            Game g = cursorToGame(cursor);
            String idString = String.valueOf(g.getId());

            if (gameId.equals(idString))
                game = g;

            cursor.moveToNext();
        }

        cursor.close();

        return game;
    }

    private String[] getGameIdsWithPlayerInvolved(final long playerId) {

        final List<String> gameIds = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, null, null, null, null, DbHelper.GAMES_COL_DATE + " DESC");

        cursor.moveToFirst();
        Game game;

        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);

            for (Player player : game.getPlayers()) {
                if (player.getId() == playerId)
                    gameIds.add(String.valueOf(game.getId()));
            }
            cursor.moveToNext();
        }

        cursor.close();

        return gameIds.toArray(new String[gameIds.size()]);
    }

    public void deleteGamesWithPlayer(final long playerId) {

        String whereClause = "_id" + "=?";
        String[] whereArgs = getGameIdsWithPlayerInvolved(playerId);
        for (String whereArg : whereArgs)
            database.delete(DbHelper.TABLE_GAMES, whereClause, new String[]{whereArg});

    }

    public void saveGame(final Game game) {

        ContentValues values = new ContentValues();
        values.put(DbHelper.GAMES_COL_SAVED, game.getSaved());
        values.put(DbHelper.GAMES_COL_CURRENT_GAME_TIME, game.getCurrentGameTime());
        values.put(DbHelper.GAMES_COL_NEXT_PLAYER_INDEX, game.getNextPlayerIndex());
        values.put(DbHelper.GAMES_COL_START_PLAYER_INDEX, game.getStartPlayerIndex());
        values.put(DbHelper.GAMES_COL_FINISHED, game.getFinished());
        values.put(DbHelper.GAMES_COL_IS_LAST_ROUND, game.getIsLastRound());

        // serialize players id's
        String playerIds = "";
        for (Player p : game.getPlayers())
            playerIds = playerIds + p.getId() + ";";


        //remove last semicolon
        playerIds = playerIds.substring(0, playerIds.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS, playerIds);

        // serialize player round times
        String playerRoundTimes = "";
        for (Player p : game.getPlayers())
            playerRoundTimes = playerRoundTimes + game.getPlayer_round_times().get(p.getId()) + ";";

        //remove last semicolon
        playerRoundTimes = playerRoundTimes.substring(0, playerRoundTimes.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS_ROUND_TIMES, playerRoundTimes);

        // serialize player rounds
        String playerRounds = "";
        for (Player p : game.getPlayers())
            playerRounds = playerRounds + game.getPlayer_rounds().get(p.getId()) + ";";

        //remove last semicolon
        playerRounds = playerRounds.substring(0, playerRounds.length() - 1);
        values.put(DbHelper.GAMES_COL_PLAYERS_ROUNDS, playerRounds);

        int result = database.update(DbHelper.TABLE_GAMES, values, "_id=?", new String[]{String.valueOf(game.getId())});
    }

    public void deleteGame(final Game g) {

        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[]{String.valueOf(g.getId())};

        database.delete(DbHelper.TABLE_GAMES, whereClause, whereArgs);

    }

    private Game cursorToGame(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(DbHelper.GAMES_COL_ID);
        int idDate = cursor.getColumnIndex(DbHelper.GAMES_COL_DATE);
        int idName = cursor.getColumnIndex(DbHelper.GAMES_COL_NAME);
        int idPlayers = cursor.getColumnIndex(DbHelper.GAMES_COL_PLAYERS);
        int idPlayersRoundTimes = cursor.getColumnIndex(DbHelper.GAMES_COL_PLAYERS_ROUND_TIMES);
        int idPlayersRounds = cursor.getColumnIndex(DbHelper.GAMES_COL_PLAYERS_ROUNDS);
        int idRound_time = cursor.getColumnIndex(DbHelper.GAMES_COL_ROUND_TIME);
        int idGame_time = cursor.getColumnIndex(DbHelper.GAMES_COL_GAME_TIME);
        int idReset_round_time = cursor.getColumnIndex(DbHelper.GAMES_COL_RESET_ROUND_TIME);
        int idGame_mode = cursor.getColumnIndex(DbHelper.GAMES_COL_GAME_MODE);
        int idRound_time_delta = cursor.getColumnIndex(DbHelper.GAMES_COL_ROUND_TIME_DELTA);
        int idCurrent_game_time = cursor.getColumnIndex(DbHelper.GAMES_COL_CURRENT_GAME_TIME);
        int idNext_player_index = cursor.getColumnIndex(DbHelper.GAMES_COL_NEXT_PLAYER_INDEX);
        int idStart_player_index = cursor.getColumnIndex(DbHelper.GAMES_COL_START_PLAYER_INDEX);
        int idSaved = cursor.getColumnIndex(DbHelper.GAMES_COL_SAVED);
        int idFinished = cursor.getColumnIndex(DbHelper.GAMES_COL_FINISHED);
        int idGame_time_infinite = cursor.getColumnIndex(DbHelper.GAMES_COL_GAME_TIME_INFINITE);
        int idChess_mode = cursor.getColumnIndex(DbHelper.GAMES_COL_CHESS_MODE);
        int idIs_last_round = cursor.getColumnIndex(DbHelper.GAMES_COL_IS_LAST_ROUND);

        long id = cursor.getLong(idIndex);
        long date = cursor.getLong(idDate);
        String name = cursor.getString(idName);
        long round_time = cursor.getInt(idRound_time);
        long game_time = cursor.getInt(idGame_time);
        int reset_round_time = cursor.getInt(idReset_round_time);
        int game_mode = cursor.getInt(idGame_mode);
        long round_time_delta = cursor.getInt(idRound_time_delta);
        long current_game_time = cursor.getLong(idCurrent_game_time);
        int next_player_index = cursor.getInt(idNext_player_index);
        int start_player_index = cursor.getInt(idStart_player_index);
        int saved = cursor.getInt(idSaved);
        int finished = cursor.getInt(idFinished);
        int game_time_infinite = cursor.getInt(idGame_time_infinite);
        int chess_mode = cursor.getInt(idChess_mode);
        int is_last_round = cursor.getInt(idIs_last_round);

        // deserialize player IDs
        String playerIds = cursor.getString(idPlayers);
        String[] playerIdsArray = playerIds.split(";");
        List<Player> players = pds.getPlayersWithIds(playerIdsArray);

        // deserialize player round times
        String playerTimes = cursor.getString(idPlayersRoundTimes);
        String[] playerTimesArray = playerTimes.split(";");
        HashMap<Long, Long> player_round_times = new HashMap<>();

        for (int i = 0; i < playerIdsArray.length; i++) {
            player_round_times.put(Long.valueOf(playerIdsArray[i]), Long.valueOf(playerTimesArray[i]));
        }

        //deserialize player rounds
        String playerRounds = cursor.getString(idPlayersRounds);
        String[] playerRoundsArray = playerRounds.split(";");
        HashMap<Long, Long> player_rounds = new HashMap<>();
        for (int i = 0; i < playerIdsArray.length; i++) {
            player_rounds.put(Long.valueOf(playerIdsArray[i]), Long.valueOf(playerRoundsArray[i]));
        }

        Game game = new Game();
        game.setId(id);
        game.setDate(date);
        game.setName(name);
        game.setRound_time(round_time);
        game.setGame_time(game_time);
        game.setPlayer_round_times(player_round_times);
        game.setPlayer_rounds(player_rounds);
        game.setReset_round_time(reset_round_time);
        game.setGame_mode(game_mode);
        game.setRound_time_delta(round_time_delta);
        game.setPlayers(players);
        game.setNextPlayerIndex(next_player_index);
        game.setStartPlayerIndex(start_player_index);
        game.setCurrentGameTime(current_game_time);
        game.setSaved(saved);
        game.setFinished(finished);
        game.setGame_time_infinite(game_time_infinite);
        game.setChess_mode(chess_mode);
        game.setIsLastRound(is_last_round);

        return game;
    }

    public List<Game> getAllGames() {
        List<Game> gameList = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, null, null, null, null, DbHelper.GAMES_COL_DATE + " DESC");

        cursor.moveToFirst();
        Game game;

        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);
            gameList.add(game);
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }

    public List<Game> getSavedGames() {
        List<Game> gameList = new ArrayList<>();

        String whereClause = "saved" + "=?";
        String[] whereArgs = new String[]{"1"};

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, whereClause, whereArgs, null, null, DbHelper.GAMES_COL_DATE + " DESC");


        cursor.moveToFirst();
        Game game;

        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);
            gameList.add(game);
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }

    public List<Game> getFinishedGames() {
        List<Game> gameList = new ArrayList<>();

        String whereClause = "finished" + "=?";
        String[] whereArgs = new String[]{"1"};

        Cursor cursor = database.query(DbHelper.TABLE_GAMES,
                columns, whereClause, whereArgs, null, null, DbHelper.GAMES_COL_DATE + " DESC");

        cursor.moveToFirst();
        Game game;

        while (!cursor.isAfterLast()) {
            game = cursorToGame(cursor);
            gameList.add(game);
            cursor.moveToNext();
        }

        cursor.close();

        return gameList;
    }

    public List<Game> getGamesOfPlayer(Player p) {
        List<Game> gameList = new ArrayList<>();

        for (Game g : getFinishedGames()) {
            System.err.println(g.getPlayers());
            for (Player player : g.getPlayers())
                if (player.getId() == p.getId())
                    gameList.add(g);
        }

        System.err.println(p);
        System.err.println(gameList);

        return gameList;
    }
}