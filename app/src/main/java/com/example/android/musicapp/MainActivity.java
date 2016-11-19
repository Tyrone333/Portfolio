package com.example.android.musicapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;

    int currentSongPlaying = 0;
    int currentPosition = 0;
    boolean songActive = false;
    boolean shuffleActive = false;
    boolean fromClick = false;

    boolean mainList = true;
    boolean goodList = false;
    boolean badList = false;
    boolean favList = false;

    SongAdapter adapter;

    final private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    private String songTime;

    ArrayList<Song> songs = new ArrayList<>();
    ArrayList<Song> favSongs = new ArrayList<>();
    ArrayList<Song> goodSongs = new ArrayList<>();
    ArrayList<Song> badSongs = new ArrayList<>();
    String[] STAR = {"*"};
    Cursor cursor;
    Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

    SharedPreferences mPrefs;
    Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_list);

        try {
            favSongs = (ArrayList<Song>) InternalStorage.readObject(this, "Favourites");
            goodSongs = (ArrayList<Song>) InternalStorage.readObject(this, "Good");
            badSongs = (ArrayList<Song>) InternalStorage.readObject(this, "Bad");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        LinearLayout topView = (LinearLayout) findViewById(R.id.topView);
        topView.setVisibility(LinearLayout.GONE);
//        final ImageButton addToPlaylistButton = (ImageButton) findViewById(R.id.addToPlaylist);

        //Check if u have the permission to READ_EXTERNAL_STORAGE
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        // Here, thisActivity is the current activity
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                // Marshmallow+
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            } else {
                // Pre-Marshmallow
            }
        } else {

            //Get all Songs from the device and add them to an ArrayList
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            cursor = managedQuery(uri, STAR, selection, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String songName = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        songName = refactorWord(songName);

                        String path = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.DATA));


                        String albumName = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                        String artist = cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                        artist = refactorWord(artist);

                        songs.add(new Song(songName, albumName, artist, path));

                    } while (cursor.moveToNext());
                }
            }

            //Check if my songLists are still up to date, delete files that are not anymore on the device
            favSongs = checkSongsUpToDate(favSongs);
            goodSongs = checkSongsUpToDate(goodSongs);
            badSongs = checkSongsUpToDate(badSongs);

            //Setup a onClickListener that will be passed into the adapter for clicks on the Add/Delete Button
            final AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, long l) {
                    ArrayList<Song> songsList = checkWhatSonglist();
                    final Song song = songsList.get(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    if (mainList) {
                        builder.setTitle("Favorites");
                        builder.setIcon(R.drawable.fast_forward48);
                        builder.setMessage("Choose the list you want to add the song: " + song.getSongName());
                        builder.setPositiveButton("Favorite",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (!checkIfSongExists(song, favSongs)) {
                                            favSongs.add(song);
                                        }
                                    }
                                });

                        builder.setNeutralButton("Good",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (!checkIfSongExists(song, goodSongs)) {
                                            goodSongs.add(song);
                                        }
                                    }
                                });

                        builder.setNegativeButton("Bad",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        if (!checkIfSongExists(song, badSongs)) {
                                            badSongs.add(song);
                                        }
                                    }
                                });
                    } else {
                        builder.setTitle("Delete");
                        builder.setIcon(R.drawable.fast_forward48);
                        builder.setMessage("Do you really want to delete the song: " + song.getSongName());
                        builder.setPositiveButton("Delete",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ArrayList<Song> songsList = checkWhatSonglist();
                                        songsList.remove( position );
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        builder.setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Toast.makeText(MainActivity.this, "Canceled", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                    builder.create().show();
                }
            };
            //Setup a onClickListener that will be passed into the adapter for clicks on an Item
            final AdapterView.OnItemClickListener listener2 = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    //Play the song
                    fromClick = true;
                    playSong(position, 0);
                }
            };

            //Setup the Listview for the Songs
            adapter = new SongAdapter(this, songs, listener, listener2, mainList);
            ListView listView = (ListView) findViewById(R.id.list);
            listView.setAdapter(adapter);


            //Listener for the mainButton
            Button mainButton = (Button) findViewById(R.id.mainList);
            mainButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainList = true;
                    favList = false;
                    goodList = false;
                    badList = false;
                    adapter = new SongAdapter(MainActivity.this, songs, listener, listener2, mainList);
                    ListView listView = (ListView) findViewById(R.id.list);
                    listView.setAdapter(adapter);
                }
            });

            //Listener for the favoriteButton
            Button favoriteButton = (Button) findViewById(R.id.favoriteList);
            favoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainList = false;
                    favList = true;
                    goodList = false;
                    badList = false;
                    adapter = new SongAdapter(MainActivity.this, favSongs, listener, listener2, mainList);
                    ListView listView = (ListView) findViewById(R.id.list);
                    listView.setAdapter(adapter);
                }
            });

            //Listener for the goodActivityButton
            Button goodActivityButton = (Button) findViewById(R.id.good_activity_button);
            goodActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create new intent to go to {@link GoodActivity}
                    Intent intent = new Intent(MainActivity.this, GoodActivity.class);
                        // Send the full goodSongs list
                        intent.putExtra("currentSong", goodSongs);

                    // Launch the {@link EditorActivity} to display the data for the current pet.
                    startActivity(intent);
                }
            });

            //Listener for the badActivityButton
            final Button badActivityButton = (Button) findViewById(R.id.bad_activity_button);
            badActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Create new intent to go to {@link GoodActivity}
                    Intent intent = new Intent(MainActivity.this, BadActivity.class);
                    // Send the full goodSongs list
                    intent.putExtra("currentSongBad", badSongs);

                    // Launch the {@link EditorActivity} to display the data for the current pet.
                    startActivity(intent);
                }
            });

            //Listener for the goodButton
            Button goodButton = (Button) findViewById(R.id.feelGoodList);
            goodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainList = false;
                    favList = false;
                    goodList = true;
                    badList = false;
                    adapter = new SongAdapter(MainActivity.this, goodSongs, listener, listener2, mainList);
                    ListView listView = (ListView) findViewById(R.id.list);
                    listView.setAdapter(adapter);
                }
            });

            //Listener for the badButton
            Button badButton = (Button) findViewById(R.id.feelBadList);
            badButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainList = false;
                    favList = false;
                    goodList = false;
                    badList = true;
                    adapter = new SongAdapter(MainActivity.this, badSongs, listener, listener2, mainList);
                    ListView listView = (ListView) findViewById(R.id.list);
                    listView.setAdapter(adapter);
                }
            });

            //Listener for the Shuffle ImageButton
            final ImageButton shuffleButton = (ImageButton) findViewById(R.id.buttonShuffle);
            shuffleButton.getDrawable().setAlpha(40);
            shuffleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (shuffleActive) {
                        shuffleButton.getDrawable().setAlpha(40);
                        shuffleActive = false;
                        Toast.makeText(MainActivity.this, "Shuffle is now off", Toast.LENGTH_LONG).show();
                    } else {
                        shuffleButton.getDrawable().setAlpha(255);
                        shuffleActive = true;
                        Toast.makeText(MainActivity.this, "Shuffle is now on", Toast.LENGTH_LONG).show();
                    }
                }
            });

            //Listener for the Pause/Play Button
            final ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlay);
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (songActive) {
                        LinearLayout topView = (LinearLayout) findViewById( R.id.topView);
                        topView.setVisibility(LinearLayout.GONE);
                        currentPosition = mMediaPlayer.getCurrentPosition();
                        releaseMediaPlayer();
                        playButton.setImageResource(R.drawable.play48);
                    } else {
                        releaseMediaPlayer();
                        fromClick = true;
                        playSong(currentSongPlaying, currentPosition);
                        playButton.setImageResource(R.drawable.pause48);
                    }
                }
            });

            //Listener for the Back Button
            ImageButton backButton = (ImageButton) findViewById(R.id.buttonBack);
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMediaPlayer != null) {
                        int position = mMediaPlayer.getCurrentPosition();
                        if (currentSongPlaying == 0) {
                            return;
                        } else if (position > 3000) {
                            fromClick = true;
                            playSong(currentSongPlaying, 0);
                            return;
                        }
                        fromClick = true;
                        playSong(currentSongPlaying - 1, 0);
                    }
                }
            });

            //Listener for the Forward Button
            ImageButton forwardButton = (ImageButton) findViewById(R.id.buttonForward);
            forwardButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mMediaPlayer != null) {
                        ArrayList<Song> songsList = checkWhatSonglist();
                        if (songsList.size() - 1 == currentSongPlaying) {
                            currentSongPlaying = 0;
                            fromClick = false;
                            playSong(currentSongPlaying, 0);
                            return;
                        }
                        fromClick = false;
                        playSong(currentSongPlaying + 1, 0);
                    }
                }
            });
        }
    }
    //Check if my songLists are still up to date, delete files that are not anymore on the device
    private ArrayList<Song> checkSongsUpToDate(ArrayList<Song> songsList) {
        ArrayList<Song> tempList = new ArrayList<Song>();
        for(int i = 0; songsList.size() > i; i++){
            for(int ii = 0; songs.size() > ii; ii++) {
                if(songsList.get( i ).getPath().equals( songs.get( ii ).getPath())) {
                    tempList.add(songsList.get( i ));
                }
            }
        }
        return tempList;
    }

    //Play Song
    private void playSong(int i, int position) {
        ArrayList<Song> songsList = checkWhatSonglist();
        if (songsList.isEmpty()){return;}
        else {
        ImageButton playButton = (ImageButton) findViewById(R.id.buttonPlay);
        playButton.setImageResource(R.drawable.pause48);
        releaseMediaPlayer();
        if (shuffleActive && !fromClick) {
            Random r = new Random();
            i = r.nextInt(songsList.size() - 1 - -1);
        }
        Song song = songsList.get(i);
        currentSongPlaying = i;
        //Check from which playlist we are playing and get the song back

        // Request audio focus for playback
        int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            TextView title = (TextView) findViewById( R.id.song_name1 );
            TextView artist = (TextView) findViewById( R.id.song_artist1 );
            title.setText( songsList.get( currentSongPlaying ).getSongName() );
            artist.setText( songsList.get( currentSongPlaying ).getArtist() );
            LinearLayout topView = (LinearLayout) findViewById( R.id.topView );
            topView.setVisibility( LinearLayout.VISIBLE );
            //  Start playback.
            startMediaPlayer( song, position );
        }
        }
    }
    private ArrayList<Song> checkWhatSonglist() {
        if (favList) {
            return favSongs;
        } else if (goodList) {
            return goodSongs;
        } else if (badList) {
            return badSongs;
        } else {
            return songs;
        }
    }

    //Start the MediaPlayer
    private void startMediaPlayer(Song song, int position) {
        mMediaPlayer = MediaPlayer.create(MainActivity.this, Uri.parse(song.getPath()));
        mMediaPlayer.seekTo(position);
        mMediaPlayer.start();
        int duration = mMediaPlayer.getDuration();
        setupSeekbar(duration, position);
        songActive = true;
        mMediaPlayer.setOnCompletionListener(mCompletionListener);
    }

    /**
     * This listener gets triggered when the {@link MediaPlayer} has completed
     * playing the audio file.
     */
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            ArrayList<Song> songsList = checkWhatSonglist();
            // Now that the sound file has finished playing, play the next song.
            if (songsList.size() - 1 > currentSongPlaying) {
                fromClick = false;
                playSong(currentSongPlaying + 1, 0);
//                releaseMediaPlayer();
            } else {
                fromClick = false;
                playSong(0, 0);
//                releaseMediaPlayer();
            }
        }
    };

    //All the code for my seekbar
    private void setupSeekbar(int duration, int position) {
        runSeekbar();
        TextView durationText = (TextView) findViewById(R.id.full_duration);
        final TextView currentTime = (TextView) findViewById(R.id.current_duration);
        convertTime(duration);
        durationText.setText(songTime);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setProgress(position);
        seekBar.setMax(duration);
        seekBar.setClickable(true);

        //Seekbar Listener
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                convertTime(progress);
                currentTime.setText(songTime);
                currentPosition = progress;
                if (fromUser && songActive) {
                    mMediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public String convertTime(int time) {
        //convert the song duration into string reading hours, mins seconds
        //int dur = (int) song.get(position).getDuration();

        int hrs = (time / 3600000);
        int mns = (time / 60000) % 60000;
        int scs = time % 60000 / 1000;

        songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);

        return songTime;
    }

    /**
     * This listener gets triggered whenever the audio focus changes
     * (i.e., we gain or lose audio focus because of another app or device).
     */
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    ArrayList<Song> songsList = checkWhatSonglist();
                    // resume playback
                    if (mMediaPlayer == null)
                        startMediaPlayer(songsList.get(currentSongPlaying), currentPosition);
                    else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                    break;

                case AudioManager.AUDIOFOCUS_LOSS:
                    // Lost focus for an unbounded amount of time: stop playback and release media player
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                    currentPosition = mMediaPlayer.getCurrentPosition();
                    releaseMediaPlayer();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    // Lost focus for a short time, but we have to stop
                    // playback. We don't release the media player because playback
                    // is likely to resume
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Lost focus for a short time, but it's ok to keep playing
                    // at an attenuated level
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                    break;
            }
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        // Keep the music up after exit the APP
        if (!songActive) {releaseMediaPlayer();}
        try {
            InternalStorage.writeObject(this, "Favourites", favSongs);
            InternalStorage.writeObject(this, "Good", goodSongs);
            InternalStorage.writeObject(this, "Bad", badSongs);

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Clean up the media player by releasing its resources.
     */
    private void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
            songActive = false;
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
    }

    //Change the seekBar + currentTime while song is played
    private void runSeekbar() {
        // do something long
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentPosition = 0;
                int soundTotal = mMediaPlayer.getDuration();

                while (mMediaPlayer != null && currentPosition < soundTotal) {
                    try {
                        Thread.sleep(300);
                        currentPosition = mMediaPlayer.getCurrentPosition();
                    } catch (Exception e) {
                        return;
                    }
                    SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
                    seekBar.setProgress(currentPosition);
                }
            }

            ;
        };
        new Thread(runnable).start();
    }

    //After asking for permission
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    this.recreate();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //Clean up the Words
    //First letter gets capitalized and the "_" are getting replaced with a space
    private String refactorWord(String title) {
        String firstLetter = title.substring(0, 1).toUpperCase();
        String restOfWordMp3 = "";
        if (title.length()>5){restOfWordMp3 = title.substring(1, title.length() - 4);}
        String restOfWord = title.substring(1);
        String mp3Part = title.substring(title.length() - 4);
        String toReplace = "_";

        if (mp3Part.equals(".mp3")) {
            title = firstLetter + restOfWordMp3;
            title = replaceSignWithSpace(title, toReplace);
            return title;
        } else {
            title = firstLetter + restOfWord;
            title = replaceSignWithSpace(title, toReplace);
            return title;
        }
    }

    //Replace a letter with space and capitalize the word after the space
    private String replaceSignWithSpace(String newTitle, String replacer) {
        int index = newTitle.indexOf(replacer);
        while (index >= 0) {
            if (index == 0) {
                newTitle = newTitle.substring(1);
            } else if (index == newTitle.length() - 1){
                newTitle = newTitle.substring(0, index);
            }else {
                String firstPart = newTitle.substring(0, index);
                String secondPart = newTitle.substring(index + 1);
                secondPart = secondPart.substring(0, 1).toUpperCase() + secondPart.substring(1);
                newTitle = firstPart + " " + secondPart;
            }
            index = newTitle.indexOf(replacer);
        }
        return newTitle;
    }

    //This function checks if the song is already in the List
    private boolean checkIfSongExists(Song song, ArrayList<Song> songsList) {
        boolean boo = false;
        for (int i = 0; songsList.size() > i; i++) {
            if (song.getPath().equalsIgnoreCase(songsList.get(i).getPath())) {
                boo = true;
                break;
            } else {
                boo = false;
            }
        }
        return boo;
    }
}



