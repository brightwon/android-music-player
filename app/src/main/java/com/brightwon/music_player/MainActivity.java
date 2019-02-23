package com.brightwon.music_player;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
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

public class MainActivity extends AppCompatActivity {

    static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 5555;
    String permission = Manifest.permission.READ_EXTERNAL_STORAGE;

    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<MusicListItem> songs;
    private MusicListAdapter.OnItemClickListener mListener;

    private MusicDataGetter model = new MusicDataGetter();

    private Toolbar toolBar;
    private ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initAppBar();
        initRecycler();

        mListener = new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

            }
        };

        // set adapter
        adapter = new MusicListAdapter(songs, mListener);
        recyclerView.setAdapter(adapter);
    }


    public void getMusicList() {
        ArrayList<MusicListItem> list = model.getMusicData(this, model.getAudioPath());
        songs.addAll(list);
        adapter.notifyDataSetChanged();
    }

    // define app bar
    public void initAppBar() {
        toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        actionBar = getSupportActionBar();
    }

    // define recyclerView
    public void initRecycler() {
        songs = new ArrayList<>();

        recyclerView = findViewById(R.id.music_list);
        recyclerView.setHasFixedSize(true);

        // define LayoutManager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

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
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                // have permission
                getMusicList();
            } else {
                // not have permission
                requestReadExternalStoragePermission();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // get permission
    private void requestReadExternalStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // permission denied before
            Toast.makeText(this, "앱을 사용하려면 접근 권한을 수락해 주세요", Toast.LENGTH_SHORT).show();
        } else {
            // request the permission.
            ActivityCompat.requestPermissions(this, new String[]{permission},
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
        }
    }

    // set action after permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE : {
                // If request is cancelled, the result arrays are empty
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // have permission
                    getMusicList();
                } else {
                    Toast.makeText(this, "음원을 가져올 수 없어요", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}