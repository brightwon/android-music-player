package com.brightwon.music_player.Model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.brightwon.music_player.MusicListItem;

import java.util.ArrayList;

public class MusicDataHelper {

    private Context mContext;

    public MusicDataHelper(Context context) {
        this.mContext = context;
    }

    /** get external storage uri */
    public Uri getAudioPath() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    /** get music list from local storage */
    public ArrayList<MusicListItem> getMusicData(Uri filePath) {
        // type of meta data to get
        String[] projection = {
                MediaStore.Audio.Media.IS_MUSIC,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST};

        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(
                filePath,
                projection,
                null,
                null,
                null);

        ArrayList<MusicListItem> metaList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) != 0) {
                    Uri artUri = Uri.parse("content://media/external/audio/albumart");

                    int id = cursor.getInt(1);
                    Uri uri = ContentUris.withAppendedId(artUri, cursor.getInt(2));
                    String title = cursor.getString(3);
                    String artist = cursor.getString(4);

                    // save list in SQLite
                    insertData(id, String.valueOf(uri), title, artist);

                    metaList.add(new MusicListItem(uri, title, artist, false));
                }
            }
            cursor.close();
        } else {
            Toast.makeText(mContext, "음원을 찾을 수 없어요", Toast.LENGTH_SHORT).show();
        }

        return metaList;
    }

    /** saves the music list in SQLite */
    private void insertData(int id, String uri, String title, String artist) {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DbHelper.FeedEntry.COLUMN_ID, id);
        values.put(DbHelper.FeedEntry.COLUMN_ALBUM_ID, uri);
        values.put(DbHelper.FeedEntry.COLUMN_SONG_TITLE, title);
        values.put(DbHelper.FeedEntry.COLUMN_ARTIST, artist);

        db.insert(DbHelper.FeedEntry.TABLE_NAME, null, values);
        db.close();
    }

    /** reads the music list from SQLite */
    public ArrayList<MusicListItem> selectData() {
        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                DbHelper.FeedEntry.COLUMN_ALBUM_ID,
                DbHelper.FeedEntry.COLUMN_SONG_TITLE,
                DbHelper.FeedEntry.COLUMN_ARTIST
        };

        Cursor cursor = db.query(
                DbHelper.FeedEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        ArrayList<MusicListItem> songs = new ArrayList<>();
        while(cursor.moveToNext()) {
            Uri uri = Uri.parse(cursor.getString(0));
            String title = cursor.getString(1);
            String artist = cursor.getString(2);

            songs.add(new MusicListItem(uri, title, artist, false));
        }
        cursor.close();

        return songs;
    }
}