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

    /* music details */
    private Uri albumUri;
    private String title;
    private String artist;

    /* whether music is stop */
    private boolean isStopped;

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
                if (isStopped) {
                    playOthers(context, uri);
                } else {
                    // music is paused. restart!
                    start();
                }
            } else {
                // other click
                playOthers(context, uri);
            }
        }

        previous = position;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        isStopped = true;
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        super.prepare();
        isStopped = false;
    }

    private void playOthers(Context context, Uri uri) throws IOException {
        reset();
        setDataSource(context, uri);
        prepare();
        start();
    }

    /** sets music details from MainActivity */
    public void setMusicDetails(Uri albumUri, String title, String artist) {
        this.albumUri = albumUri;
        this.title = title;
        this.artist = artist;
    }

    public Uri getAlbumUri() {
        return this.albumUri;
    }

    public String getSongTitle() {
        return this.title;
    }

    public String getSongArtist() {
        return this.artist;
    }
}
