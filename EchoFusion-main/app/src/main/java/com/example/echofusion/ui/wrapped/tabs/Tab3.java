package com.example.echofusion.ui.wrapped.tabs;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echofusion.MainActivity;
import com.example.echofusion.R;
import com.example.echofusion.tokenTest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Tab3 extends Fragment {
    private String token;

    private ImageView artistPic1, artistPic2, artistPic3, artistPic4, artistPic5;

    private TextView artist1, artist2, artist3, artist4, artist5;
    //private String[] artistList = new String[5];
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab3, container, false);

        Intent in = getActivity().getIntent();
        Bundle b = in.getExtras();
        token = b.getString(token);

        artist1 = (TextView) rootView.findViewById(R.id.artist1);
        artist2 = (TextView) rootView.findViewById(R.id.artist2);
        artist3 = (TextView) rootView.findViewById(R.id.artist3);
        artist4 = (TextView) rootView.findViewById(R.id.artist4);
        artist5 = (TextView) rootView.findViewById(R.id.artist5);

        artistPic1 = (ImageView) rootView.findViewById(R.id.artistPic1);
        artistPic2 = (ImageView) rootView.findViewById(R.id.artistPic2);
        artistPic3 = (ImageView) rootView.findViewById(R.id.artistPic3);
        artistPic4 = (ImageView) rootView.findViewById(R.id.artistPic4);
        artistPic5 = (ImageView) rootView.findViewById(R.id.artistPic5);
        setTextAsync(Tab1.artistList[0], artist1);
        setTextAsync(Tab1.artistList[1], artist2);
        setTextAsync(Tab1.artistList[2], artist3);
        setTextAsync(Tab1.artistList[3], artist4);
        setTextAsync(Tab1.artistList[4], artist5);
        getActivity().runOnUiThread(() -> {
            Picasso.get().load(Tab1.artistPicList[0]).into(artistPic1);
            Picasso.get().load(Tab1.artistPicList[1]).into(artistPic2);
            Picasso.get().load(Tab1.artistPicList[2]).into(artistPic3);
            Picasso.get().load(Tab1.artistPicList[3]).into(artistPic4);
            Picasso.get().load(Tab1.artistPicList[4]).into(artistPic5);
        });


        return rootView;
    }

    private void setTextAsync(final String text, TextView textView) {
        getActivity().runOnUiThread(() -> textView.setText(text));
    }

}