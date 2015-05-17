package com.holandago.wbamanager.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by razu on 14/01/15.
 */
public class shopFloorSQLiteHelper extends SQLiteOpenHelper{

    //Database version
    private static final int DATABASE_VERSION = 1;

    //Database name
    private static final String DATABASE_NAME = "shopfloorDB";

    //Books table name
    private static final String TABLE_CACHE = "cacheDB";

    //Books table columns name
    private static final String KEY_ID = "id";
    private static final String KEY_URL = "url";
    private static final String KEY_CACHE = "cache";

    private static final String[] COLUMNS = {KEY_ID,KEY_URL,KEY_CACHE};

    public Context context;

    public shopFloorSQLiteHelper(Context c){
        super(c,DATABASE_NAME,null, DATABASE_VERSION);
        this.context = c;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        // SQL statement to create book table
        String CREATE_BOOK_TABLE = "CREATE TABLE cacheDB ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "url TEXT," +
                "cache TEXT)";

        // create books table
        db.execSQL(CREATE_BOOK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);

        // create fresh books table
        this.onCreate(db);
    }

    public void put(String url, String cache){
        //for logging
        Log.d("CACHE:", url + " : " + cache);

        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put(KEY_URL, url);
        values.put(KEY_CACHE, cache);

        db.insert(TABLE_CACHE, // table
                null, //nullColumnHack
                values); // key/value -> keys = column names/ values = column values

        // 4. close
        db.close();
    }

    public String get(String url) throws URLNotFound{
        if (getAllCaches().get(url)==null) throw new URLNotFound();
        return getAllCaches().get(url);
    }

    public Map<String,String> getAllCaches(){
        HashMap<String,String> map = new HashMap<String, String>();

        // 1. build the query
        String query = "SELECT  * FROM " + TABLE_CACHE;

        // 2. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                map.put(cursor.getString(1),cursor.getString(2));
            } while (cursor.moveToNext());
        }

        Log.d("getAllCache()", map.toString());
        cursor.close();
        db.close();
        // return books
        return map;
    }

    public int update(String url,String cache){
        // 1. get reference to writable DB
        SQLiteDatabase db = this.getWritableDatabase();

        // 2. create ContentValues to add key "column"/value
        ContentValues values = new ContentValues();
        values.put("cache", cache); // get title

        // 3. updating row
        int i = db.update(TABLE_CACHE, //table
                values, // column/value
                KEY_URL+" = ?", // selections
                new String[] { String.valueOf(cache) }); //selection args

        // 4. close
        db.close();

        return i;
    }

    void delete(UserOperations operations){
        //TODO implementar

    }

    public class CacheNotFound extends Exception {

    }

    public class URLNotFound extends Exception {
    }




}
