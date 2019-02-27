package com.brightwon.music_player.Model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "song.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " +
                    FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_SONG_TITLE + " TEXT," +
                    FeedEntry.COLUMN_ARTIST + " TEXT," +
                    FeedEntry.COLUMN_ALBUM_ID + " TEXT)";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("DbHelper", "onUpgrade: ");
    }

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "song_list";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_SONG_TITLE  = "title";
        public static final String COLUMN_ARTIST  = "artist";
        public static final String COLUMN_ALBUM_ID = "album_art";
    }

}
