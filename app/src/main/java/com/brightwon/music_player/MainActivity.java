package com.brightwon.music_player;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.music_list);

        // define RecyclerView instance
        ArrayList<MusicListItem> songs = new ArrayList<>();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        MusicListAdapter adapter = new MusicListAdapter(songs);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        test(songs);
    }

    // test code
    private void test(ArrayList<MusicListItem> songs){
        songs.add(new MusicListItem(R.drawable.ic_launcher_background,"test1","ariel",false));
        songs.add(new MusicListItem(R.drawable.ic_launcher_foreground,"test2","base",false));
        songs.add(new MusicListItem(R.drawable.ic_launcher_background,"test3","call",true));
        songs.add(new MusicListItem(R.drawable.ic_launcher_foreground,"test4","david",false));
    }
}
