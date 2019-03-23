package com.brightwon.music_player;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class MusicListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MusicListItem> songItems;
    private OnItemClickListener mListener;
    private RecyclerView mRecyclerView;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final MusicListItem currItem = songItems.get(position);
        final SongHolder holder = (SongHolder) viewHolder;
        final int curr = position;
        final AnimatedVectorDrawable vector =
                (AnimatedVectorDrawable) ContextCompat.getDrawable(context, R.drawable.play_animation);

        Glide.with(context).load(songItems.get(position).albumImg).into(holder.artImage);
        holder.songTitle.setText(songItems.get(position).songTitle);
        holder.songArtist.setText(songItems.get(position).songArtist);
        holder.playGraph.setImageDrawable(vector);

        if(currItem.playStatus) {
            holder.playGraph.setVisibility(View.VISIBLE);
            if(currItem.pauseStatus) {
                vector.stop();
            } else {
                vector.start();
            }
        } else {
            vector.stop();
            holder.playGraph.setVisibility(View.GONE);
        }

        // item click event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // click listener for MainActivity
                mListener.onItemClick(v, curr);

                // play animation logic
                animatePlay(holder, vector, curr);
            }
        });
    }

    /** update previous position */
    void updatePrevPos(int position) {
        this.prev = position;
    }

    /** switch on/off music play animation */
    private void animatePlay(SongHolder holder, AnimatedVectorDrawable vector, int curr) {
        if (!songItems.get(curr).playStatus && prev == -1) {
            // first click
            holder.playGraph.setVisibility(View.VISIBLE);
            songItems.get(curr).playStatus = true;
            vector.start();

        } else if (songItems.get(curr).playStatus && prev == curr){
            // same item click
            if (!songItems.get(curr).pauseStatus) {
                // is playing.. pause !
                vector.stop();
                songItems.get(curr).pauseStatus = true;
            } else {
                // is paused.. replay !
                vector.start();
                songItems.get(curr).pauseStatus = false;
            }
            songItems.get(curr).playStatus = true;

        } else {
            // other item click
            songItems.get(prev).playStatus = false;
            vector.stop();

            holder.playGraph.setVisibility(View.VISIBLE);
            songItems.get(curr).pauseStatus = false;
            songItems.get(curr).playStatus = true;
            vector.start();

        }
        prev = curr;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return songItems.size();
    }


    public static class SongHolder extends RecyclerView.ViewHolder {

        ImageView artImage;
        TextView songTitle, songArtist;
        ImageView playGraph;

        SongHolder(View v) {
            super(v);
            artImage = v.findViewById(R.id.song_img);
            songTitle = v.findViewById(R.id.song_title);
            songArtist = v.findViewById(R.id.song_artist);
            playGraph = v.findViewById(R.id.play_status);
        }
    }


}
