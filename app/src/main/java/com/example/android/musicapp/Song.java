package com.example.android.musicapp;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by tyrone3 on 09.09.16.
 */

public class Song implements Serializable {

    @SerializedName("song_name")
    private String mSongName;
    @SerializedName("album")
    private String mAlbum;
    @SerializedName("artist")
    private String mArtist;
    @SerializedName("path")
    private String mPath;

    public Song(String songName, String album, String artist, String path){
        mSongName = songName;
        mAlbum = album;
        mArtist = artist;
        mPath = path;
    }

    public String getSongName() {return mSongName;};
    public String getAlbum() {return mAlbum;};
    public String getArtist() {return mArtist;};
    public String getPath() {return mPath;};
}
