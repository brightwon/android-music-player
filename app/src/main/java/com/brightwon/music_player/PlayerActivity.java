package com.brightwon.music_player;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class PlayerActivity extends AppCompatActivity {

    TextView titleTextView, artistTextView, playTime, curTime;
    ImageView albumArt, playPauseView, backward, forward, backBtn;
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

        // back button click listener
        setBackEventClickListener();
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
        backBtn = findViewById(R.id.back_btn);
    }

    /** sets music details */
    private void setInfo(Uri uri, String title, String artist) {
        Glide.with(this).load(uri).into(albumArt);
        titleTextView.setText(title);
        artistTextView.setText(artist);
    }

    /** destroy this Activity with transition Animation */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.no_animation, R.anim.anim_slide_out_bottom);
    }

    /** destroy this Activity with transition Animation */
    public void setBackEventClickListener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.anim_slide_out_bottom);
            }
        });
    }
}
