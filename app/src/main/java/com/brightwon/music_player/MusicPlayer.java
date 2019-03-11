package com.brightwon.music_player;

import android.content.ContentUris;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

public class MusicPlayer extends MediaPlayer {

    /* recyclerView position of music being played */
    private int previous = -1;
    private int current = -1;

    public MusicPlayer() {
        this.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    /** Plays the Music */
    public void playMusic(Context context, int id, int position) throws IOException {
        current = position;
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        if (isPlaying()) {
            // playing
            if (current == previous) {
                pause();
            } else {
                // other click
                playOthers(context, uri);
            }
        } else {
            // not playing
            if (previous == -1) {
                // first start
                setDataSource(context, uri);
                prepare();
                start();
            } else if (previous == current) {
                // music is paused. restart!
                start();
            } else {
                // other click
                playOthers(context, uri);
            }
        }

        previous = position;
    }

    private void playOthers(Context context, Uri uri) throws IOException {
        reset();
        setDataSource(context, uri);
        prepare();
        start();
    }


}
