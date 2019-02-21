package com.brightwon.music_player;

public class MusicListItem {

    int albumImg;
    String songTitle;
    String songArtist;
    boolean playStatus;

    public MusicListItem(int albumImg, String songTitle, String songArtist, boolean playStatus){
        this.albumImg = albumImg;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
        this.playStatus = playStatus;
    }
}
