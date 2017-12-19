package org.secuso.privacyfriendlyboardgameclock.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.secuso.privacyfriendlyboardgameclock.model.Player;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PlayersDataSourceSingleton {

    private static PlayersDataSourceSingleton instance;
    private SQLiteDatabase database;
    private DbHelper dbHelper;

    private Context context;

    private String[] columns = {
            DbHelper.PLAYERS_COL_ID,
            DbHelper.PLAYERS_COL_DATE,
            DbHelper.PLAYERS_COL_NAME,
            DbHelper.PLAYERS_COL_ICON
    };


    public PlayersDataSourceSingleton(Context context) {
        dbHelper = new DbHelper(context);
    }

    /**
     *
     * @param context pass the ApplicationContext the first time created, or null
     * @return
     */
    public static PlayersDataSourceSingleton getInstance(Context context){
        if(instance == null){
            synchronized (GamesDataSourceSingleton.class){
                if(instance == null && context != null){
                    instance = new PlayersDataSourceSingleton(context);
                }
            }
        }
        return instance;
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Player createPlayer(String name, Bitmap playerIcon) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.PLAYERS_COL_NAME, name);
        values.put(DbHelper.PLAYERS_COL_DATE, System.currentTimeMillis());
        values.put(DbHelper.PLAYERS_COL_ICON, getBytes(playerIcon));

        long insertId = database.insert(DbHelper.TABLE_PLAYERS, null, values);

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, DbHelper.PLAYERS_COL_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Player player = cursorToPlayer(cursor);
        cursor.close();

        return player;
    }

    public void updatePlayer(Player p) {
        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[]{String.valueOf(p.getId())};

        ContentValues values = new ContentValues();
        values.put(DbHelper.PLAYERS_COL_NAME, p.getName());
        values.put(DbHelper.PLAYERS_COL_ICON, getBytes(p.getIcon()));

        database.update(DbHelper.TABLE_PLAYERS, values, whereClause, whereArgs);
    }

    public void deletePlayer(Player p) {
        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[]{String.valueOf(p.getId())};

        database.delete(DbHelper.TABLE_PLAYERS, whereClause, whereArgs);
    }

    private Player cursorToPlayer(Cursor cursor) {

        int idIndex = cursor.getColumnIndex(DbHelper.PLAYERS_COL_ID);
        int idDate = cursor.getColumnIndex(DbHelper.PLAYERS_COL_DATE);
        int idName = cursor.getColumnIndex(DbHelper.PLAYERS_COL_NAME);
        int idIcon = cursor.getColumnIndex(DbHelper.PLAYERS_COL_ICON);

        String name = cursor.getString(idName);
        byte[] iconArray = cursor.getBlob(idIcon);
        Bitmap icon = getImage(iconArray);

        long id = cursor.getLong(idIndex);
        long date = cursor.getLong(idDate);

        Player player = new Player();
        player.setId(id);
        player.setDate(date);
        player.setName(name);
        player.setIcon(icon);

        return player;
    }

    public List<Player> getPlayersWithIds(String[] playerIds) {
        Player[] playerArray = new Player[playerIds.length];

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Player player;

        while (!cursor.isAfterLast()) {
            player = cursorToPlayer(cursor);
            String idString = String.valueOf(player.getId());

            for (int i = 0; i < playerIds.length; i++)
                if (idString.equals(playerIds[i]))
                    playerArray[i] = player;

            cursor.moveToNext();
        }

        cursor.close();

        return Arrays.asList(playerArray);
    }

    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, null, null, null, null, DbHelper.PLAYERS_COL_NAME + " ASC");

        cursor.moveToFirst();
        Player player;

        while (!cursor.isAfterLast()) {
            player = cursorToPlayer(cursor);
            playerList.add(player);
            cursor.moveToNext();
        }

        cursor.close();

        return playerList;
    }
}