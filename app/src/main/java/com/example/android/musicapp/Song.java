package com.example.android.musicapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tyrone3 on 09.09.16.
 */

public class Song implements Parcelable {

    private String mSongName;
    private String mAlbum;
    private String mArtist;
    private String mPath;

    public Song(String songName, String album, String artist, String path){
        mSongName = songName;
        mAlbum = album;
        mArtist = artist;
        mPath = path;
    }

    protected Song(Parcel in) {
        mSongName = in.readString();
        mAlbum = in.readString();
        mArtist = in.readString();
        mPath = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public String getSongName() {return mSongName;};
    public String getAlbum() {return mAlbum;};
    public String getArtist() {return mArtist;};
    public String getPath() {return mPath;};

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mSongName);
        parcel.writeString(mAlbum);
        parcel.writeString(mArtist);
        parcel.writeString(mPath);
    }
}
