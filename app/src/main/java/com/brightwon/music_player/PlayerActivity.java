package com.brightwon.music_player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.brightwon.music_player.MainActivity.FEED_BACK_FOR_PLAYER_ACTIVITY;
import static com.brightwon.music_player.MainActivity.mp;

public class PlayerActivity extends AppCompatActivity {

    /* views on the screen */
    private TextView titleTextView, artistTextView, playTime, curTime;
    private ImageView albumArt, playPauseView, backward, forward, backBtn;
    private SeekBar progress;

    /* related to update music progress */
    private Handler mHandler;
    private Runnable runnable;

    /* music list */
    ArrayList<MusicListItem> songs;

    /* current position */
    int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();

        Intent intent = getIntent();
        songs = (ArrayList<MusicListItem>) intent.getSerializableExtra("song_list");
        mPosition = intent.getIntExtra("position", -1);

        // set text and image
        setInfo(songs.get(mPosition).albumImg,
                songs.get(mPosition).songTitle,
                songs.get(mPosition).songArtist);

        // back button click event
        setBackEventClickListener();

        // set seekBar settings
        initSeekBar();

        // check views status
        switchPlayView();

        // music playback click event
        playPause();
        nextPlay();
        prevPlay();

        // set thread for updating seekBar
        mHandler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                progress.setProgress(mp.getCurrentPosition());
                mHandler.postDelayed(this, 1000);
            }
        };

        // updates seekBar for every seconds
        runnable.run();
    }

    /** play previous music */
    private void prevPlay() {
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPosition = handlePosition(songs, mPosition - 1);
                    setInfo(songs.get(mPosition).albumImg,
                            songs.get(mPosition).songTitle,
                            songs.get(mPosition).songArtist);
                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                    switchPlayView();
                    toZeroSeek();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** play next music */
    private void nextPlay() {
        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPosition = handlePosition(songs, mPosition + 1);
                    setInfo(songs.get(mPosition).albumImg,
                            songs.get(mPosition).songTitle,
                            songs.get(mPosition).songArtist);
                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                    switchPlayView();
                    toZeroSeek();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** play click event */
    private void playPause() {
        playPauseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                    switchPlayView();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** adjusts out of range position */
    private int handlePosition(ArrayList songs, int position) {
        if (position == songs.size()) {
            position = 0;
        } else if (position == -1) {
            position = songs.size() - 1;
        }
        return position;
    }

    /** switch on/off playPauseView  */
    private void switchPlayView() {
        if (mp.isPlaying()) {
            playPauseView.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.pause));
        } else {
            playPauseView.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.play));
        }
    }

    /** sets zero seekBar and current time */
    private void toZeroSeek() {
        progress.setProgress(0);
        curTime.setText("00:00");
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
    private void setInfo(String uri, String title, String artist) {
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
        Intent intent = new Intent();
        intent.putExtra("album_uri", songs.get(mPosition).albumImg);
        intent.putExtra("position", mPosition);
        setResult(FEED_BACK_FOR_PLAYER_ACTIVITY, intent);
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.no_animation, R.anim.anim_slide_out_bottom);
    }

    /** destroy this Activity with transition Animation */
    public void setBackEventClickListener() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("album_uri", songs.get(mPosition).albumImg);
                intent.putExtra("position", mPosition);
                setResult(FEED_BACK_FOR_PLAYER_ACTIVITY, intent);
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.anim_slide_out_bottom);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // stop thread
        mHandler.removeCallbacks(runnable);
    }
}
