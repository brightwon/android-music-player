package com.brightwon.music_player;

import android.net.Uri;

public class MusicListItem {

    Uri albumImg;
    String songTitle;
    String songArtist;
    int id;
    boolean playStatus;

    public MusicListItem(int id, Uri albumImg, String songTitle, String songArtist, boolean playStatus){
        this.id = id;
        this.albumImg = albumImg;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.playStatus = playStatus;
    }

    public String getSongTitle(){
        return songTitle;
    }

    public String getSongArtist(){
        return songArtist;
    }
}
