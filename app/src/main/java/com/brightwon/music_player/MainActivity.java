package com.brightwon.music_player;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brightwon.music_player.Model.MusicDataHelper;

import java.util.ArrayList;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static com.brightwon.music_player.MusicPlayService.EXTRA_CUTOUT_SAFE_AREA;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 5555;
    static final int MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 6666;

    String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;

    private MusicPlayService mService;
    private boolean mBound = false;

    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private ArrayList<MusicListItem> songs;

    private MusicDataHelper model = new MusicDataHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppBar();
        initRecycler();

        MusicListAdapter.OnItemClickListener mListener = new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // check if the Service is running
                if (isServiceRunning(MusicPlayService.class)) {
                    // change the floatingView image
                    if (mService.isFloat) {
                        mService.setFloatingViewImg(songs.get(position).albumImg);
                    } else {
                        mService.setNewFloatingView(songs.get(position).albumImg);
                    }
                    // handle the music playback
                    initAndPlay(position);
                } else {
                    startMusicService(position);
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
    }

    /** starts the floatingView and play the music in Service */
    @TargetApi(Build.VERSION_CODES.O)
    public void startMusicService(int position) {
        // check the permission
        if (Settings.canDrawOverlays(this)) {
            Uri artUri = songs.get(position).albumImg;

            Intent intent = new Intent(getApplicationContext(), MusicPlayService.class);
            intent.putExtra("artUri", artUri);
            intent.putExtra("title", songs.get(position).songTitle);
            intent.putExtra("artist", songs.get(position).songArtist);
            intent.putExtra("id", songs.get(position).id);
            intent.putExtra("position", position);
            intent.putExtra(EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(this));

            // binds the Service
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            requestSystemAlertWindowPermission();
        }
    }

    /** calls the methods in MusicPlayService */
    private void initAndPlay(int position) {
        mService.setFloatingViewClickListener();
        mService.setMusicDetails(songs.get(position).albumImg,
                songs.get(position).songTitle,
                songs.get(position).songArtist);
        mService.playMusic(getApplicationContext(), songs.get(position).id, position);
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /** defines callback for the Service binding */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.LocalBinder binder = (MusicPlayService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            mService.getMainActivity(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

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

    /** check if Service is running */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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

    /** unbinds Service */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onStart() {
        if (isServiceRunning(MusicPlayService.class)) {
            mService.appearView();
        }
        super.onStart();
    }
}