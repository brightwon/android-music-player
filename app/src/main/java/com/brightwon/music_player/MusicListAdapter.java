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
    private OnItemClickListener mListener;

    public MusicListAdapter(ArrayList<MusicListItem> songItems, OnItemClickListener listener) {
        this.songItems = songItems;
        this.mListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    @NonNull
    @Override
    public MusicListAdapter.SongHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.music_list_item, parent, false);

        return new SongHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        SongHolder holder = (SongHolder) viewHolder;
        final boolean playStatus = songItems.get(position).playStatus;

        holder.artImage.setImageResource(songItems.get(position).albumImg);
        holder.songTitle.setText(songItems.get(position).songTitle);
        holder.songArtist.setText(songItems.get(position).songArtist);

        if(playStatus) {
            holder.playGraph.setVisibility(View.VISIBLE);
        } else {
            holder.playGraph.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v, position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return songItems.size();
    }


    public static class SongHolder extends RecyclerView.ViewHolder {

        ImageView artImage;
        TextView songTitle, songArtist;
        AVLoadingIndicatorView playGraph;

        public SongHolder(View v) {
            super(v);
            artImage = v.findViewById(R.id.song_img);
            songTitle = v.findViewById(R.id.song_title);
            songArtist = v.findViewById(R.id.song_artist);
            playGraph = v.findViewById(R.id.play_status);
        }
    }


}
