package com.brightwon.music_player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.concurrent.TimeUnit;

import static com.brightwon.music_player.MusicPlayService.mp;

public class PlayerActivity extends AppCompatActivity {

    private TextView titleTextView, artistTextView, playTime, curTime;
    private ImageView albumArt, playPauseView, backward, forward, backBtn;
    private SeekBar progress;

    private Handler mHandler = new Handler();

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

        // set seekBar settings
        initSeekBar();

        // set thread for updating seekBar
        chaseSeekBar();
    }

    /** sets SeekBar settings */
    private void initSeekBar() {
        progress.setMax(mp.getDuration());
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int current, boolean fromUser) {
                curTime.setText(convertTime(current));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mp.seekTo(progress.getProgress());
            }
        });
    }

    /** updates seekBar for every seconds */
    private void chaseSeekBar() {
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setProgress(mp.getCurrentPosition());
                mHandler.postDelayed(this, 1000);
            }
        });
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
        playTime.setText(convertTime(mp.getDuration()));
    }

    /** converts milliseconds to "mm:ss" format */
    @SuppressLint("DefaultLocale")
    private String convertTime(int ms) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(ms),
                TimeUnit.MILLISECONDS.toSeconds(ms) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
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
