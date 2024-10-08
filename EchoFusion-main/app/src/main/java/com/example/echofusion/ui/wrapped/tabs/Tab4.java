package com.example.echofusion.ui.wrapped.tabs;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.echofusion.R;

import org.w3c.dom.Text;

import okhttp3.Call;
import okhttp3.OkHttpClient;

public class Tab4 extends Fragment {
    private String token;
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private TextView genre1, genre2, genre3, genre4, genre5,yourTopGenre;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_tab4, container, false);

        Intent in = getActivity().getIntent();
        Bundle b = in.getExtras();
        token = b.getString(token);

        genre1 = (TextView) rootView.findViewById(R.id.genre1);
        genre2 = (TextView) rootView.findViewById(R.id.genre2);
        genre3 = (TextView) rootView.findViewById(R.id.genre3);
        genre4 = (TextView) rootView.findViewById(R.id.genre4);
        genre5 = (TextView) rootView.findViewById(R.id.genre5);
        yourTopGenre = (TextView) rootView.findViewById(R.id.topGenres);

        setTextAsync(Tab1.topFiveGenreArray.get(0), genre1);
        setTextAsync(Tab1.topFiveGenreArray.get(1), genre2);
        setTextAsync(Tab1.topFiveGenreArray.get(2), genre3);
        setTextAsync(Tab1.topFiveGenreArray.get(3), genre4);
        setTextAsync(Tab1.topFiveGenreArray.get(4), genre5);
        setTextAsync("Your Top Genres", yourTopGenre);
        return rootView;
    }
    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }


}