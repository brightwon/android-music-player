package com.brightwon.music_player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MusicListAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<MusicListItem> songs;
    private MusicListAdapter.OnItemClickListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.music_list);
        recyclerView.setHasFixedSize(true);

        // define LayoutManager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        songs = new ArrayList<>();
        mListener = new MusicListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(MainActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
            }
        };

        adapter = new MusicListAdapter(songs, mListener);
        recyclerView.setAdapter(adapter);

        test();
    }

    // test code
    private void test() {
        songs.add(new MusicListItem(R.drawable.ic_launcher_background,"test1","ariel",false));
        songs.add(new MusicListItem(R.drawable.ic_launcher_foreground,"test2","base",false));
        songs.add(new MusicListItem(R.drawable.ic_launcher_background,"test3","call",true));
        songs.add(new MusicListItem(R.drawable.ic_launcher_foreground,"test4","david",false));
    }
}
