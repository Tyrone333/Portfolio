package com.example.android.musicapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by tyrone3 on 19.11.16.
 */
public class BadActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.good_bad_activity);

        TextView textView = (TextView) findViewById(R.id.good_bad_title_textview);
        textView.setText(R.string.bad_activity_title);
        Intent intent = getIntent();
        ArrayList<Song> goodSongs = intent.getExtras().getParcelableArrayList("currentSongBad");

        Button button =(Button) findViewById(R.id.back_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backIntent = new Intent(BadActivity.this, MainActivity.class);
                startActivity(backIntent);
            }
        });

        if (!goodSongs.isEmpty()) {
            Toast.makeText(this, goodSongs.get(0).getAlbum(), Toast.LENGTH_LONG).show();
        }
    }
}
