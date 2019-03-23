package com.brightwon.music_player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MusicListItem> songItems;
    private OnItemClickListener mListener;
    private RecyclerView mRecyclerView;
    private SongHolder preHolder;
    private int prev = -1;
    private Context context;

    MusicListAdapter(ArrayList<MusicListItem> songItems, OnItemClickListener listener) {
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
        mRecyclerView = (RecyclerView) parent;
        context = parent.getContext();
        return new SongHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final MusicListItem currItem = songItems.get(position);
        final SongHolder holder = (SongHolder) viewHolder;
        final int curr = position;

        Glide.with(context).load(songItems.get(position).albumImg).into(holder.artImage);
        holder.songTitle.setText(songItems.get(position).songTitle);
        holder.songArtist.setText(songItems.get(position).songArtist);

        if(currItem.playStatus) {
            holder.playGraph.setVisibility(View.VISIBLE);
            if(currItem.pauseStatus) {
                holder.playGraph.getIndicator().stop();
                holder.playGraph.invalidate();
            }
        } else {
            holder.playGraph.setVisibility(View.GONE);
        }

        // item click event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // click listener for MainActivity
                mListener.onItemClick(v, curr);

                // play animation logic
                animatePlay(holder, curr);
            }
        });
    }

    /** switch on/off music play animation */
    public void animatePlay(SongHolder holder, int curr) {
        if (!songItems.get(curr).playStatus && prev == -1) {
            // first click
            holder.playGraph.setVisibility(View.VISIBLE);
            songItems.get(curr).playStatus = true;

        } else if (songItems.get(curr).playStatus && prev == curr){
            // same item click
            if (holder.playGraph.getIndicator().isRunning()) {
                // is playing.. pause !
                holder.playGraph.getIndicator().stop();
                songItems.get(curr).pauseStatus = true;
            } else {
                // is paused.. replay !
                holder.playGraph.getIndicator().start();
                songItems.get(curr).pauseStatus = false;
            }
            songItems.get(curr).playStatus = true;

        } else {
            // other item click
            preHolder.playGraph.setVisibility(View.GONE);
            songItems.get(prev).playStatus = false;

            holder.playGraph.setVisibility(View.VISIBLE);
            songItems.get(curr).playStatus = true;

        }
        preHolder = (SongHolder) mRecyclerView.findViewHolderForAdapterPosition(curr);
        prev = curr;
    }

    @Override
    public int getItemCount() {
        return songItems.size();
    }


    public static class SongHolder extends RecyclerView.ViewHolder {

        ImageView artImage;
        TextView songTitle, songArtist;
        AVLoadingIndicatorView playGraph;

        SongHolder(View v) {
            super(v);
            artImage = v.findViewById(R.id.song_img);
            songTitle = v.findViewById(R.id.song_title);
            songArtist = v.findViewById(R.id.song_artist);
            playGraph = v.findViewById(R.id.play_status);
        }
    }


}
