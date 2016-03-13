package privacyfriendlyexample.org.secuso.boardgameclock.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import privacyfriendlyexample.org.secuso.boardgameclock.R;
import privacyfriendlyexample.org.secuso.boardgameclock.model.Player;


public class PlayersDataSource {

    private static final String LOG_TAG = PlayersDataSource.class.getSimpleName();

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    private Context context;

    private String[] columns = {
            DbHelper.PLAYERS_COL_ID,
            DbHelper.PLAYERS_COL_DATE,
            DbHelper.PLAYERS_COL_NAME,
            DbHelper.PLAYERS_COL_PHOTOURI
    };


    public PlayersDataSource(Context context) {
        dbHelper = new DbHelper(context);
        this.context = context;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Player createPlayer(String name, String photoUri) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.PLAYERS_COL_NAME, name);
        values.put(DbHelper.PLAYERS_COL_DATE, System.currentTimeMillis());

        if (photoUri != null)
            values.put(DbHelper.PLAYERS_COL_PHOTOURI, photoUri);
        else
            values.put(DbHelper.PLAYERS_COL_PHOTOURI, resourceToUri(context, R.drawable.ic_launcher));

        long insertId = database.insert(DbHelper.TABLE_PLAYERS, null, values);

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, DbHelper.PLAYERS_COL_ID + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Player player = cursorToPlayer(cursor);
        cursor.close();

        return player;
    }

    private static String resourceToUri (Context context,int resID) {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                context.getResources().getResourcePackageName(resID) + '/' +
                context.getResources().getResourceTypeName(resID) + '/' +
                context.getResources().getResourceEntryName(resID)).toString();
    }

    public void deletePlayer(Player p){
        String whereClause = "_id" + "=?";
        String[] whereArgs = new String[] { String.valueOf(p.getId()) };

        database.delete(DbHelper.TABLE_PLAYERS, whereClause, whereArgs);
    }

    private Player cursorToPlayer(Cursor cursor) {

        int idIndex = cursor.getColumnIndex(DbHelper.PLAYERS_COL_ID);
        int idDate = cursor.getColumnIndex(DbHelper.PLAYERS_COL_DATE);
        int idName = cursor.getColumnIndex(DbHelper.PLAYERS_COL_NAME);
        int idPhotoUri = cursor.getColumnIndex(DbHelper.PLAYERS_COL_PHOTOURI);

        String name = cursor.getString(idName);
        String photoUri = cursor.getString(idPhotoUri);
        long id = cursor.getLong(idIndex);
        long date = cursor.getLong(idDate);

        Player player = new Player();
        player.setId(id);
        player.setDate(date);
        player.setName(name);
        player.setPhotoUri(photoUri);

        return player;
    }

    public List<Player> getPlayersWithIds(String[] playerIds){
        List<Player> playerList = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, null, null, null, null, null);

        cursor.moveToFirst();
        Player player;

        while(!cursor.isAfterLast()) {
            player = cursorToPlayer(cursor);
            String idString = String.valueOf(player.getId());

            if (Arrays.asList(playerIds).contains(idString))
                playerList.add(player);
            cursor.moveToNext();
        }

        cursor.close();

        return playerList;
    }

    public List<Player> getAllPlayers() {
        List<Player> playerList = new ArrayList<>();

        Cursor cursor = database.query(DbHelper.TABLE_PLAYERS,
                columns, null, null, null, null, DbHelper.PLAYERS_COL_NAME + " ASC");

        cursor.moveToFirst();
        Player player;

        while(!cursor.isAfterLast()) {
            player = cursorToPlayer(cursor);
            playerList.add(player);
            cursor.moveToNext();
        }

        cursor.close();

        return playerList;
    }
}