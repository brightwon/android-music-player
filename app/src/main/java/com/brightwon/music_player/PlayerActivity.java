package com.brightwon.music_player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.brightwon.music_player.MainActivity.FEED_BACK_FOR_PLAYER_ACTIVITY;
import static com.brightwon.music_player.MainActivity.mp;

public class PlayerActivity extends AppCompatActivity {

    private TextView titleTextView, artistTextView, playTime, curTime;
    private ImageView albumArt, playPauseView, backward, forward, backBtn, repeatBtn, shuffleBtn;
    private SeekBar progress;

    /* related to update music progress */
    private Handler mHandler;
    private Runnable runnable;

    /* play option status */
    private boolean repeatStatus;
    private boolean shuffleStatus;

    /* music list */
    private ArrayList<MusicListItem> songs;
    private ArrayList<Integer> shuffledList;

    /* current position */
    private int mPosition;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initView();
        initSeekBar();

        Intent intent = getIntent();
        songs = (ArrayList<MusicListItem>) intent.getSerializableExtra("song_list");
        mPosition = intent.getIntExtra("position", -1);

        // set text and image
        setInfo(songs.get(mPosition).albumImg,
                songs.get(mPosition).songTitle,
                songs.get(mPosition).songArtist);

        // set listener
        setCompletionListener();
        setBackEventClickListener();
        setRepeatClickListener();
        setShuffleClickListener();
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

        sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        shuffleStatus = sharedPreferences.getBoolean("shuffle_status", false);
        repeatStatus = sharedPreferences.getBoolean("repeat_status", false);

        // check views status
        switchPlayView();
        switchRepeat();
        switchShuffle();
    }

    /** play previous music */
    private void prevPlay() {
        backward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mPosition = handlePosition(mPosition - 1);
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
                    mPosition = handlePosition(mPosition + 1);
                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                    setInfo(songs.get(mPosition).albumImg,
                            songs.get(mPosition).songTitle,
                            songs.get(mPosition).songArtist);
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
    private int handlePosition(int position) {
        if (position == songs.size()) {
            position = 0;
        } else if (position == -1) {
            position = songs.size() - 1;
        }
        return position;
    }

    /** sets the callback after a music is complete */
    private void setCompletionListener() {
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer MP) {
                try {
                    mPosition = handlePosition(mPosition + 1);
                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                    setInfo(songs.get(mPosition).albumImg,
                            songs.get(mPosition).songTitle,
                            songs.get(mPosition).songArtist);
                    progress.setProgress(0);
                    progress.setMax(mp.getDuration());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** sets repeat button click listener */
    private void setRepeatClickListener() {
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatStatus) {
                    repeatStatus = false;
                    Toast.makeText(PlayerActivity.this, "반복 해제", Toast.LENGTH_SHORT).show();
                } else {
                    repeatStatus = true;
                    Toast.makeText(PlayerActivity.this, "한 곡 반복", Toast.LENGTH_SHORT).show();
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("repeat_status", repeatStatus);
                editor.apply();

                switchRepeat();
            }
        });
    }

    /** sets repeat button click listener */
    private void setShuffleClickListener() {
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleStatus) {
                    shuffleStatus = false;
                    Toast.makeText(PlayerActivity.this, "랜덤 재생 해제", Toast.LENGTH_SHORT).show();
                } else {
                    shuffleStatus = true;
                    // make the new shuffled list
                    shuffledList = makeShuffleList();
                    Toast.makeText(PlayerActivity.this, "랜덤 재생", Toast.LENGTH_SHORT).show();
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("shuffle_status", shuffleStatus);
                editor.apply();

                switchShuffle();
            }
        });
    }

    /** gets shuffle list */
    @SuppressLint("UseSparseArrays")
    private ArrayList makeShuffleList() {
        ArrayList<Integer> posList = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            posList.add(i);
        }
        Collections.shuffle(posList);
        return posList;
    }

    /** switch on/off playPauseView  */
    private void switchPlayView() {
        if (mp.isPlaying()) {
            playPauseView.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.pause));
            songs.get(mPosition).pauseStatus = false;
        } else {
            playPauseView.setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(), R.drawable.play));
            songs.get(mPosition).pauseStatus = true;
        }
    }

    /** switch on/off repeat button */
    private void switchRepeat() {
        if (repeatStatus) {
            repeatBtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.blackText));
        } else {
            repeatBtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.gray));
        }
    }

    /** switch on/off shuffle button */
    private void switchShuffle() {
        if (shuffleStatus) {
            shuffleBtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.blackText));
        } else {
            shuffleBtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.gray));
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
        repeatBtn = findViewById(R.id.player_repeat);
        shuffleBtn = findViewById(R.id.player_shuffle);
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
        intent.putExtra("position", mPosition);
        intent.putExtra("pause_status", songs.get(mPosition).pauseStatus);
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
                intent.putExtra("position", mPosition);
                intent.putExtra("pause_status", songs.get(mPosition).pauseStatus);
                setResult(FEED_BACK_FOR_PLAYER_ACTIVITY, intent);
                finish();
                overridePendingTransition(R.anim.no_animation, R.anim.anim_slide_out_bottom);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        runnable.run();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(runnable);
    }
}
