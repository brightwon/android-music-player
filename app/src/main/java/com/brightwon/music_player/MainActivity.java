package com.brightwon.music_player;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.brightwon.music_player.Model.MusicDataHelper;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements FloatingViewListener {

    /* related to permissions */
    static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 5555;
    static final int MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 6666;
    String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;

    /* requestCode from PlayerActivity */
    static final int FEED_BACK_FOR_PLAYER_ACTIVITY = 7777;

    /* related to recyclerView items */
    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private ArrayList<MusicListItem> songs;

    /* current position */
    int mPosition;

    /* model instance for handle a data */
    private MusicDataHelper model = new MusicDataHelper(this);

    /* whether a floatingView exists on the screen */
    private boolean isFloat = false;

    /* related to floatingView */
    private CircleImageView iconView;
    private FloatingViewManager manager;
    private FloatingViewManager.Options options;

    /* MusicPlayer object */
    static MusicPlayer mp;

    private SharedPreferences sharedPreferences;

    /* play option status */
    private boolean repeatStatus;
    private boolean shuffleStatus;

    private ArrayList<Integer> shuffledList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppBar();
        initRecycler();

        mp = new MusicPlayer();
        sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        shuffleStatus = sharedPreferences.getBoolean("shuffle_status", false);
        repeatStatus = sharedPreferences.getBoolean("repeat_status", false);

        // item click event
        MusicListAdapter.OnItemClickListener mListener = new MusicListAdapter.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onItemClick(View v, int position) {
                // check if have permission
                if (Settings.canDrawOverlays(getApplicationContext())) {
                    mPosition = position;
                    String albumUri = songs.get(mPosition).albumImg;
                    int songId = songs.get(mPosition).id;

                    // check if floatingView already exists
                    if (!isFloat && iconView == null) {
                        iconView = (CircleImageView) LayoutInflater.from(getApplicationContext()).
                                inflate(R.layout.floating_play_widget, null, false);
                        manager.addViewToWindow(iconView, options);
                        isFloat = true;
                    }

                    // set floatingView image
                    Glide.with(getApplicationContext()).load(albumUri).
                            override(200,200).into(iconView);

                    // set floatingView click event
                    setFloatingViewClickListener();

                    // play or pause the music
                    try {
                        mp.playMusic(getApplicationContext(), songId, mPosition);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    // request the permission
                    requestSystemAlertWindowPermission();
                }
            }
        };

        // set adapter
        adapter = new MusicListAdapter(songs, mListener);
        recyclerView.setAdapter(adapter);

        // get music list from DB
        ArrayList<MusicListItem> list = model.selectData();
        songs.addAll(list);
        adapter.notifyDataSetChanged();

        // make shuffled list
        if (shuffleStatus) shuffledList = makeShuffleList();

        // initialize FloatingView settings
        initFloatingView();
    }

    /** starts the PlayerActivity */
    public void setFloatingViewClickListener() {
        iconView.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                // start PlayerActivity
                Intent intent = new Intent(getApplicationContext(), PlayerActivity.class);
                intent.putExtra("position", mPosition);
                intent.putExtra("song_list", songs);
                intent.putExtra("shuffle_list", shuffledList);
                startActivityForResult(intent, FEED_BACK_FOR_PLAYER_ACTIVITY);
                overridePendingTransition(R.anim.anim_slide_in_bottom, R.anim.no_animation);

                // hide FloatingView
                disappearView();
                songs.get(mPosition).playStatus = false;
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

    /** sets disappear animation for the floatingView */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void disappearView() {
        iconView.animate().scaleX(0).scaleY(0).setDuration(1000).withEndAction(new Runnable() {
            @Override
            public void run() {
                iconView.setVisibility(View.GONE);
            }
        });
    }

    /** sets appear animation for the floatingView */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void appearView() {
        iconView.animate().scaleX(1).scaleY(1).setDuration(750).withStartAction(new Runnable() {
            @Override
            public void run() {
                iconView.setVisibility(View.VISIBLE);
            }
        });
    }

    /** sets the floatingView settings */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initFloatingView() {

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        // sets the trash icon size and alpha
        manager = new FloatingViewManager(getApplicationContext(), this);
        Bitmap bitmap = ((BitmapDrawable) getDrawable(R.drawable.stop)).getBitmap();
        Drawable mDrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap,
                210, 210, true));
        mDrawable.setAlpha(150);
        manager.setFixedTrashIconImage(mDrawable);
        manager.setSafeInsetRect(FloatingViewManager.findCutoutSafeArea(this));

        // sets the floatingView location
        // horizontal : right side, vertical : 5% from bottom
        options = new FloatingViewManager.Options();
        options.floatingViewX = metrics.widthPixels;
        options.floatingViewY = (int) (metrics.heightPixels * 0.05);
    }

    /** called after finish the FloatingView */
    @Override
    public void onFinishFloatingView() {
        mp.stop();
        manager.removeAllViewToWindow();
        iconView = null;
        isFloat = false;
        songs.get(mPosition).playStatus = false;
        adapter.notifyItemChanged(mPosition);
    }

    @Override
    protected void onStart() {
        setCompletionListener();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // exit music and floatingView
        manager.removeAllViewToWindow();
        mp.release();
    }

    @Override
    public void onTouchFinished(boolean isFinishing, int x, int y) {

    }

    /** gets the SYSTEM_ALERT_WINDOW permission */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestSystemAlertWindowPermission() {
        Toast.makeText(this, "권한을 추가해 주세요", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW :
                // check the permission
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "이제 음악을 재생할 수 있어요!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "권한을 추가해야 재생할 수 있어요!", Toast.LENGTH_SHORT).show();
                }
                break;
            case FEED_BACK_FOR_PLAYER_ACTIVITY :
                mPosition = data.getIntExtra("position", -1);
                songs.get(mPosition).pauseStatus = data.getBooleanExtra("pause_status", false);
                songs.get(mPosition).playStatus = true;
                adapter.updatePrevPos(mPosition);
                adapter.notifyDataSetChanged();

                repeatStatus = data.getBooleanExtra("repeat_status", false);
                shuffleStatus = data.getBooleanExtra("shuffle_status", false);
                shuffledList = (ArrayList) data.getSerializableExtra("shuffle_list");

                // set floatingView image
                Glide.with(getApplicationContext()).load(songs.get(mPosition).albumImg).
                        override(200,200).into(iconView);
                appearView();
                recyclerView.scrollToPosition(mPosition);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** gets the music list from local storage in device. */
    public void getMusicList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, storagePermission);
        // permission check
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            ArrayList<MusicListItem> list = model.getMusicData(model.getAudioPath());
            songs.addAll(list);
            adapter.notifyDataSetChanged();
        } else {
            requestReadExternalStoragePermission();
        }
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
                    songs.get(mPosition).playStatus = false;
                    songs.get(mPosition).pauseStatus = false;
                    if (repeatStatus) {
                        mPosition = handlePosition(mPosition);
                    } else if (shuffleStatus) {
                        int tmpPos = shuffledList.indexOf(mPosition);
                        mPosition = shuffledList.get(handlePosition(tmpPos + 1));
                    } else {
                        mPosition = handlePosition(mPosition + 1);
                    }
                    songs.get(mPosition).playStatus = true;
                    adapter.updatePrevPos(mPosition);
                    adapter.notifyDataSetChanged();

                    recyclerView.smoothScrollToPosition(mPosition);
                    Glide.with(getApplicationContext()).load(songs.get(mPosition).albumImg).
                            override(200,200).into(iconView);

                    mp.playMusic(getApplicationContext(), songs.get(mPosition).id, mPosition);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** requests the READ_EXTERNAL_STORAGE permission */
    private void requestReadExternalStoragePermission() {
        // check the permission
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                storagePermission)) {
            // if permission denied before
            Toast.makeText(this, "미디어 접근 권한을 수락해 주세요", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{storagePermission},
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
        }
    }

    /** sets the response after permission request */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE : {
                // check the permission
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한을 가져왔어요! 다시 추가해 보세요", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "음원을 가져올 수 없어요!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    /** initializes the appBar */
    public void initAppBar() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    /** initializes the recyclerView */
    public void initRecycler() {
        songs = new ArrayList<>();

        recyclerView = findViewById(R.id.music_list);
        recyclerView.setHasFixedSize(true);

        // initializes the LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // add item stroke
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    /** creates the toolBar menu item */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    /** creates the toolBar menu click event */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_btn) {
            getMusicList();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}