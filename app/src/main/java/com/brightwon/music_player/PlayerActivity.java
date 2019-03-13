package com.brightwon.music_player;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import static com.brightwon.music_player.MusicPlayService.mp;

public class PlayerActivity extends AppCompatActivity {

    TextView titleTextView, artistTextView, playTime, curTime;
    ImageView albumArt, playPauseView, backward, forward;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();

        Intent intent = getIntent();
        Uri coverUri = intent.getParcelableExtra("artUri");
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");

        setInfo(coverUri, title, artist);
    }

    /** initializes all view */
    private void initView() {
        titleTextView = findViewById(R.id.player_title);
        artistTextView = findViewById(R.id.player_artist);
        playTime = findViewById(R.id.player_play_time);
        curTime = findViewById(R.id.player_current_time);
        albumArt = findViewById(R.id.player_art);
        playPauseView = findViewById(R.id.player_play_pause);
        backward = findViewById(R.id.player_backward);
        forward = findViewById(R.id.player_forward);
        progress = findViewById(R.id.player_progress);
    }

    /** sets music details */
    private void setInfo(Uri uri, String title, String artist) {
        Glide.with(this).load(uri).into(albumArt);
        titleTextView.setText(title);
        artistTextView.setText(artist);
    }
}
