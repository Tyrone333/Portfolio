package com.example.android.musicapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tyrone3 on 09.09.16.
 */

class SongAdapter extends ArrayAdapter<Song> {
    private AdapterView.OnItemClickListener onClickListener;
    private AdapterView.OnItemClickListener onClickListener2;
    private boolean mMainList;

    SongAdapter(Context context, ArrayList<Song> songs, AdapterView.OnItemClickListener onClickListener,
                AdapterView.OnItemClickListener onClickListener2, boolean mainList) {
        super(context, 0, songs);
        this.onClickListener = onClickListener;
        this.onClickListener2 = onClickListener2;
        mMainList = mainList;

    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        // Check if the existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }
        Song currentSong = getItem(position);

        TextView songNameTextView = (TextView) listItemView.findViewById(R.id.song_name);
        assert currentSong != null;
        songNameTextView.setText(currentSong.getSongName());
        TextView songArtistTextView = (TextView) listItemView.findViewById(R.id.song_artist);
        songArtistTextView.setText(currentSong.getArtist());
        ImageButton addToPlaylistButton = (ImageButton) listItemView.findViewById(R.id.addToPlaylist);
        if (mMainList) {
            addToPlaylistButton.setImageResource(R.drawable.hospital24);
        } else {
            addToPlaylistButton.setImageResource(R.drawable.minus24);
        }
            addToPlaylistButton.setTag(position);
            addToPlaylistButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onClickListener != null) {
                        onClickListener.onItemClick(null, view, position, 0);
                    }
                }
            });

        Button listButton = (Button) listItemView.findViewById(R.id.item_click);
        listButton.setTag(position);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickListener2 != null) {
                    onClickListener2.onItemClick(null, view, position, 0);
                }
            }
        });
        
        return listItemView;
    }
}
