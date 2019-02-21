package com.brightwon.music_player;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MusicListItem> songItems;

    public MusicListAdapter(ArrayList<MusicListItem> songItems) {
        this.songItems = songItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_list_item, parent, false);

        return new SongHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        SongHolder songHolder = (SongHolder) viewHolder;
        boolean playStatus = songItems.get(position).playStatus;

        songHolder.artImage.setImageResource(songItems.get(position).albumImg);
        songHolder.songTitle.setText(songItems.get(position).songTitle);
        songHolder.songArtist.setText(songItems.get(position).songArtist);

        if(playStatus) {
            songHolder.playStatus.setVisibility(View.VISIBLE);
        } else {
            songHolder.playStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return songItems.size();
    }


    public class SongHolder extends RecyclerView.ViewHolder {
        ImageView artImage;
        TextView songTitle, songArtist;
        AVLoadingIndicatorView playStatus;

        private SongHolder(@NonNull View itemView) {
            super(itemView);
            artImage = itemView.findViewById(R.id.song_img);
            songTitle = itemView.findViewById(R.id.song_title);
            songArtist = itemView.findViewById(R.id.song_artist);
            playStatus = itemView.findViewById(R.id.play_status);
        }
    }


}
