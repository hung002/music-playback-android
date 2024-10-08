package com.example.echofusion.ui.wrapped.tabs;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.echofusion.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class Tab5 extends Fragment {

    private String token;
    private String wrappedId;
    private MediaPlayer songPlayer = new MediaPlayer();
    private TextView song1, song2, song3, song4, song5;
    private TextView artist1, artist2, artist3, artist4, artist5;

    private TextView topGenre;
    private String[] songsList = new String[5];
    private String[] artistList = new String[5];
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private ImageView albumPic, artistPic;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tab5, container, false);

        Intent in = getActivity().getIntent();
        Bundle b = in.getExtras();
        token = b.getString("token");
        wrappedId = b.getString("wrappedId");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        // song names
        song1 = (TextView) rootView.findViewById(R.id.song1);
        song2 = (TextView) rootView.findViewById(R.id.song2);
        song3 = (TextView) rootView.findViewById(R.id.song3);
        song4 = (TextView) rootView.findViewById(R.id.song4);
        song5 = (TextView) rootView.findViewById(R.id.song5);

        // artist names
        artist1 = (TextView) rootView.findViewById(R.id.artist1);
        artist2 = (TextView) rootView.findViewById(R.id.artist2);
        artist3 = (TextView) rootView.findViewById(R.id.artist3);
        artist4 = (TextView) rootView.findViewById(R.id.artist4);
        artist5 = (TextView) rootView.findViewById(R.id.artist5);

        topGenre = (TextView) rootView.findViewById(R.id.genre_text);

        albumPic = (ImageView) rootView.findViewById(R.id.album1);
        artistPic =  (ImageView) rootView.findViewById(R.id.artistPic);

        setTextAsync(Tab1.songsList[0], song1);
        setTextAsync(Tab1.songsList[1], song2);
        setTextAsync(Tab1.songsList[2], song3);
        setTextAsync(Tab1.songsList[3], song4);
        setTextAsync(Tab1.songsList[4], song5);

        setTextAsync(Tab1.artistList[0], artist1);
        setTextAsync(Tab1.artistList[1], artist2);
        setTextAsync(Tab1.artistList[2], artist3);
        setTextAsync(Tab1.artistList[3], artist4);
        setTextAsync(Tab1.artistList[4], artist5);



        getActivity().runOnUiThread(() -> {
            Picasso.get().load(Tab1.artistPicList[0]).into(artistPic);
            Picasso.get().load(Tab1.albumList[0]).into(albumPic);
        });


        setTextAsync(Tab1.topFiveGenreArray.get(0), topGenre);
        return rootView;
    }

    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }
}