package com.brightwon.music_player.Model;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.brightwon.music_player.MusicListItem;

import java.util.ArrayList;


public class MusicDataGetter {

    public Uri getAudioPath() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    public ArrayList<MusicListItem> getMusicData(Context context, Uri filePath) {
        ArrayList<MusicListItem> metaList = new ArrayList<>();

        // music metadata to query
        String[] projection = {MediaStore.Audio.Media.IS_MUSIC,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media._ID};

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(
                filePath,
                projection,
                null,
                null,
                null);

        if(cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getInt(0) != 0) {
                    Uri artUri = Uri.parse("content://media/external/audio/albumart");

                    Uri uri = ContentUris.withAppendedId(artUri, Integer.valueOf(cursor.getString(1)));
                    String title = cursor.getString(2);
                    String artist = cursor.getString(3);

                    metaList.add(new MusicListItem(uri, title, artist, false));
                }
            }
            cursor.close();
        } else {
            Toast.makeText(context, "음원을 찾을 수 없어요", Toast.LENGTH_SHORT).show();
        }

        return metaList;
    }
}