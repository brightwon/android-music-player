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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.brightwon.music_player.Model.MusicDataGetter;

import java.util.ArrayList;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

import static com.brightwon.music_player.MusicPlayService.EXTRA_CUTOUT_SAFE_AREA;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 5555;
    static final int MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW = 6666;

    String storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE;

    private String TAG = "MainActivity";

    private MusicPlayService mService;
    private boolean mBound = false;

    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private ArrayList<MusicListItem> songs;

    private MusicDataGetter model = new MusicDataGetter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppBar();
        initRecycler();

        MusicListAdapter.OnItemClickListener mListener = new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // music play and float floating view
                if (isServiceRunning(MusicPlayService.class)) {
                    // change floatingView image
                    if (mService.isFloat) {
                        mService.setFloatingViewImg(songs.get(position).albumImg);
                    } else {
                        mService.setNewFloatingView(songs.get(position).albumImg);
                    }
                } else {
                    startFloatingView(position);
                }
            }
        };

        // set adapter
        adapter = new MusicListAdapter(songs, mListener);
        recyclerView.setAdapter(adapter);
    }

    /**
     *  these methods are related to start floating view (and music play in service Class)
     *  1. startFloatingView
     *  2. requestSystemAlertWindowPermission
     *  3. onActivityResult
     */
    // start Floating View and music play(in MusicPlayService.class)
    // only when have SYSTEM_ALERT_WINDOW permission
    @TargetApi(Build.VERSION_CODES.O)
    public void startFloatingView(int position) {
        if (Settings.canDrawOverlays(this)) {
            // have SYSTEM_ALERT_WINDOW permission
            Uri artUri = songs.get(position).albumImg;

            Intent intent = new Intent(getApplicationContext(), MusicPlayService.class);
            intent.setData(artUri);
            intent.putExtra("title", songs.get(position).songTitle);
            intent.putExtra("artist", songs.get(position).songArtist);
            intent.putExtra(EXTRA_CUTOUT_SAFE_AREA, FloatingViewManager.findCutoutSafeArea(this));

            ContextCompat.startForegroundService(this, intent);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } else {
            // have not SYSTEM_ALERT_WINDOW permission
            requestSystemAlertWindowPermission();
        }
    }

    // get SYSTEM_ALERT_WINDOW permission
    @TargetApi(Build.VERSION_CODES.M)
    private void requestSystemAlertWindowPermission() {
        // request the SYSTEM_ALERT_WINDOW permission
        Toast.makeText(this, "권한을 추가해 주세요", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SYSTEM_ALERT_WINDOW :
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "이제 음악을 재생할 수 있어요!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "권한을 추가해야 재생할 수 있어요!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     *  defines callback for service binding.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayService.LocalBinder binder = (MusicPlayService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };


    /**
     *  these methods are related to get music list from local storage in device.
     *  1. getMusicList
     *  2. requestReadExternalStoragePermission
     *  3. onRequestPermissionsResult
     */
    // get music list only when have READ_EXTERNAL_STORAGE permission
    public void getMusicList() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, storagePermission);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            // have READ_EXTERNAL_STORAGE permission
            ArrayList<MusicListItem> list = model.getMusicData(this, model.getAudioPath());
            songs.addAll(list);
            adapter.notifyDataSetChanged();
        } else {
            // not have READ_EXTERNAL_STORAGE permission
            requestReadExternalStoragePermission();
        }
    }

    // get READ_EXTERNAL_STORAGE permission
    private void requestReadExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                storagePermission)) {
            // READ_EXTERNAL_STORAGE permission denied before
            Toast.makeText(this, "미디어 접근 권한을 수락해 주세요", Toast.LENGTH_SHORT).show();
        } else {
            // request READ_EXTERNAL_STORAGE permission
            ActivityCompat.requestPermissions(this, new String[]{storagePermission},
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
        }
    }

    // set action after request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE : {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // have READ_EXTERNAL_STORAGE permission
                    Toast.makeText(this, "권한을 가져왔어요! 다시 추가해 보세요", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "음원을 가져올 수 없어요!", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    /**
     *  check if a service is running
     */
    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    /**
     *  initialize AppBar and RecyclerView
     */
    // define app bar
    public void initAppBar() {
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    // define recyclerView
    public void initRecycler() {
        songs = new ArrayList<>();

        recyclerView = findViewById(R.id.music_list);
        recyclerView.setHasFixedSize(true);

        // define LayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }


    /**
     *  create tool bar menu and menu item click event
     */
    // app bar menu create
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    // app bar menu click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_btn) {
            getMusicList();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}