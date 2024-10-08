package com.example.echofusion.ui.wrapped.tabs;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echofusion.R;
import com.squareup.picasso.Picasso;

import java.util.Random;

import okhttp3.Call;
import okhttp3.OkHttpClient;


public class Tab2 extends Fragment {
    private String token;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private Call mCall;
    private EditText albumGuess;
    private String albumAnswer;
    private Button enterGuess;
    private ImageView albumImage;
    private int randomNum;
    private int previousRandNum = -1;
    Random random = new Random();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab2, container, false);
        Intent in = getActivity().getIntent();
        Bundle b = in.getExtras();
        albumGuess = (EditText) rootView.findViewById(R.id.nameOfAlbum);
        enterGuess = (Button) rootView.findViewById(R.id.enterGuess);
        albumImage = (ImageView) rootView.findViewById(R.id.albumImage);

        token = b.getString("token");
        createGame(rootView);
        enterGuess.setOnClickListener(v -> confirmGuess(v));
        return rootView;
    }
    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }

    private void confirmGuess(View view) {
        String guessOfUser = albumGuess.getText().toString();
        if (guessOfUser.equalsIgnoreCase(albumAnswer)) {
            Toast.makeText(getContext(), "YAY! That's correct!", Toast.LENGTH_LONG * 3).show();
            randomNum = random.nextInt(20);
            setTextAsync("", albumGuess);
            createGame(view);
        } else {
            Toast.makeText(getContext(), "Nope! Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void createGame(View view) {
        while (randomNum == previousRandNum) {
            randomNum = random.nextInt(20);
        }
        previousRandNum = randomNum;
        albumAnswer = Tab1.albumNames[randomNum];

        getActivity().runOnUiThread(() -> {
            Picasso.get().load(Tab1.extendedAlbumList[randomNum]).into(albumImage);
        });
    }
}