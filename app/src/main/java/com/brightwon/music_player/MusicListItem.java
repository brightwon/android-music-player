package com.brightwon.music_player;

import java.io.Serializable;

public class MusicListItem implements Serializable {

    int id;
    String albumImg;
    String songTitle;
    String songArtist;
    boolean playStatus;
    boolean pauseStatus;

    public MusicListItem(int id, String albumImg, String songTitle, String songArtist, boolean playStatus, boolean pauseStatus){
        this.id = id;
        this.albumImg = albumImg;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.playStatus = playStatus;
        this.pauseStatus = pauseStatus;
    }

    public String getSongTitle(){
        return songTitle;
    }

    public String getSongArtist(){
        return songArtist;
    }
}
