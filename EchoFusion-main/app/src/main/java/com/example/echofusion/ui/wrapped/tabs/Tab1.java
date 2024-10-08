package com.example.echofusion.ui.wrapped.tabs;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.echofusion.MainActivity;
import com.example.echofusion.R;
import com.example.echofusion.SyncActivity;
import com.example.echofusion.tokenTest;
import com.example.echofusion.ui.wrapped.WrappedFragment;
import com.example.echofusion.SyncActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Tab1 extends Fragment {

    private String token;
    private String wrappedId;
    private String timeRange;

    private MediaPlayer songPlayer = new MediaPlayer();
    private TextView song1, song2, song3, song4, song5;
    private ImageView album1, album2, album3, album4, album5;
    private Button songBtn;
    protected static String[] songsList = new String[5];
    protected static String[] albumList = new String[5];
    private String[] urlList = new String[5];
    protected static String[] albumNames = new String[20];
    protected static String[] extendedAlbumList = new String[20];
    private Call mCall;
    protected static String[] artistList = new String[5];
    protected static String[] artistPicList = new String[5];
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    protected static ArrayList<String> topFiveGenreArray = new ArrayList<>();
    protected static String[] topFiveGenres = new String[5];
    private HashMap<String, Integer> genreCount = new HashMap<>();
    private boolean songPlaying = false;
    private int pausedPosition = 0;
    private boolean paused = false;
    private boolean clickedOnce = false;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tab1, container, false);

        Intent in = getActivity().getIntent();
        Bundle b = in.getExtras();
        token = b.getString("token");
        wrappedId = b.getString("wrappedId");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        timeRange = SyncActivity.timeRange;

        // song names
        song1 = (TextView) rootView.findViewById(R.id.song1);
        song2 = (TextView) rootView.findViewById(R.id.song2);
        song3 = (TextView) rootView.findViewById(R.id.song3);
        song4 = (TextView) rootView.findViewById(R.id.song4);
        song5 = (TextView) rootView.findViewById(R.id.song5);

        // Set the click listeners for the buttons
        //Initialize the album covers
        album1 = (ImageView) rootView.findViewById(R.id.album1);
        album2 = (ImageView) rootView.findViewById(R.id.album2);
        album3 = (ImageView) rootView.findViewById(R.id.album3);
        album4 = (ImageView) rootView.findViewById(R.id.album4);
        album5 = (ImageView) rootView.findViewById(R.id.album5);

        songBtn = (Button) rootView.findViewById(R.id.song_player);

        getTopSongs();
        getTopArtists();
        getTopGenres();

        songBtn.setOnClickListener((v) -> {
            if (!clickedOnce) {
                clickedOnce = true;
                createMediaPlayer(urlList);
            } else {
                togglePauseResume();
            }
        });

        addWrappedDataToDatabase();
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

    // method creates media player and plays song
    private void createMediaPlayer(String[] audioUrls) {
        songPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        playSong(audioUrls,0);
    }
    private void playSong(final String[] audioUrls, final int currentIndex) {
        if (currentIndex < audioUrls.length) {
            try {
                if (!paused) {
                    songPlayer.reset();
                    songPlayer.setDataSource(audioUrls[currentIndex]);
                    songPlayer.prepareAsync();
                    songPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            songPlayer.start();
                        }
                    });
                    songPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            playSong(audioUrls, currentIndex + 1);
                        }
                    });
                } else {
                    songPlayer.seekTo(pausedPosition);
                    songPlayer.start();
                    paused = false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void togglePauseResume() {
        if (songPlayer.isPlaying()) {
            songPlayer.pause();
            paused = true;
            pausedPosition = songPlayer.getCurrentPosition();
        } else if (paused) {
            songPlayer.start();
            paused = false;
        }
    }

    public void getTopSongs() {

        if (token == null) {
            Toast.makeText(getContext(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the users top songs
        String songUrl;
        if (timeRange == "" || timeRange == "long_term") {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=long_term&offset=0&limit=20";
        } else if (timeRange == "medium_term") {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&offset=0&limit=20";
        } else {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=short_term&offset=0&limit=20";
        }

        final Request sRequest = new Request.Builder()
                .url(songUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();




        mCall = mOkHttpClient.newCall(sRequest);


        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch top songs: " + e);

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {

                    final JSONObject jsonResponse = new JSONObject(response.body().string());
                    JSONArray items = jsonResponse.getJSONArray("items");
                    int numItems = items.length(); // Get the actual number of items returned by the API

                    for (int i = 0; i < 20; i++) {
                        // Your existing loop code here
                        if (i < 5) {
                            songsList[i] = items.getJSONObject(i).getString("name");
                            urlList[i] = items.getJSONObject(i).getString("preview_url");
                            JSONArray images = items.getJSONObject(i).getJSONObject("album").getJSONArray("images");
                            if (images.length() > 0) {
                                albumList[i] = images.getJSONObject(0).getString("url");
                            } else {
                                albumList[i] = "";
                            }
                        }
                        albumNames[i] = items.getJSONObject(i).getJSONObject("album").getString("name");
                        JSONArray images = items.getJSONObject(i).getJSONObject("album").getJSONArray("images");
                        if (images.length() > 0) {
                            extendedAlbumList[i] = images.getJSONObject(0).getString("url");
                        } else {
                            extendedAlbumList[i] = "";
                        }
                    }

                    setTextAsync(songsList[0], song1);
                    setTextAsync(songsList[1], song2);
                    setTextAsync(songsList[2], song3);
                    setTextAsync(songsList[3], song4);
                    setTextAsync(songsList[4], song5);


                    if (album1 != null) {
                        getActivity().runOnUiThread(() -> {
                            Picasso.get().load(albumList[0]).into(album1);
                            Picasso.get().load(albumList[1]).into(album2);
                            Picasso.get().load(albumList[2]).into(album3);
                            Picasso.get().load(albumList[3]).into(album4);
                            Picasso.get().load(albumList[4]).into(album5);
                        });
                    } else {
                        Log.e("Error", "ImageView album is null");
                    }

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse top songs: " + e);
                    //Toast.makeText(MainActivity.this, "Failed to parse top songs, watch Logcat for more details",
                    //      Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void getTopArtists() {
        // Create a request to get the user top artists
        String artistUrl;
        if (timeRange == "" || timeRange == "long_term") {
            artistUrl = "https://api.spotify.com/v1/me/top/artists?time_range=long_term&offset=0";
        } else if (timeRange == "medium_term") {
            artistUrl = "https://api.spotify.com/v1/me/top/artists?time_range=medium_term&offset=0";
        } else {
            artistUrl = "https://api.spotify.com/v1/me/top/artists?time_range=short_term&offset=0";
        }
        final Request request = new Request.Builder()
                .url(artistUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        //cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch top artists: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonResponseArtist = new JSONObject(response.body().string());
                    JSONArray items = jsonResponseArtist.getJSONArray("items");
                    int numItems = items.length(); // Get the actual number of items returned by the API

                    for (int i = 0; i < 5; i++) {
                        // Your existing loop code here
                        JSONArray images = items.getJSONObject(i).getJSONArray("images");
                        if (images.length() > 0) {
                            artistList[i] = items.getJSONObject(i).getString("name");
                            artistPicList[i] = images.getJSONObject(0).getString("url");
                        } else {
                            artistPicList[i] = "";
                        }
                    }




                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    /*Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();*/
                }
            }
        });
    }
    public void getTopGenres() {
        // Create a request to get the user top genres
        String genreUrl;
        if (timeRange == "" || timeRange == "long_term") {
            genreUrl = "https://api.spotify.com/v1/me/top/artists?limit=50&time_range=long_term&offset=0";
        } else if (timeRange == "medium_term") {
            genreUrl = "https://api.spotify.com/v1/me/top/artists?&limit=50&time_range=medium_term&offset=0";
        } else {
            genreUrl = "https://api.spotify.com/v1/me/top/artists?limit=50&time_range=short_term&offset=0";
        }
        final Request request = new Request.Builder()
                .url(genreUrl)
                .addHeader("Authorization", "Bearer " + token)
                .build();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch top genres: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonResponseArtist = new JSONObject(response.body().string());
                    JSONArray items = jsonResponseArtist.getJSONArray("items");

                    for (int i = 0; i < items.length(); i++) {
                        JSONArray genresArray = items.getJSONObject(i).getJSONArray("genres");
                        for (int j = 0; j < genresArray.length(); j++) {
                            String genre = genresArray.getString(j);
                            genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                        }
                    }

                    topFiveGenreArray = getTopGenres(genreCount, 5);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(getContext(), "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public static ArrayList<String> getTopGenres(HashMap<String, Integer> map, int num) {
        // Create a priority queue to store genres based on their counts
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                (a, b) -> b.getValue() - a.getValue());

        // Add all entries from the genreCounts HashMap to the priority queue
        pq.addAll(map.entrySet());

        ArrayList<String> topGenres = new ArrayList<>();

        // Add the top N genres to the list
        for (int i = 0; i < num && !pq.isEmpty(); i++) {
            topGenres.add(pq.poll().getKey());
            topFiveGenres[i] = pq.poll().getKey();
        }

        return topGenres;
    }

    private void addWrappedDataToDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference wrappedRef = mDatabase.child(userId).child("past_wrappeds").child(wrappedId).child("data");

            HashMap<String, Object> wrappedItem = new HashMap<>();
            wrappedItem.put("song1", songsList[0]);
            wrappedItem.put("song2", songsList[1]);
            wrappedItem.put("song3", songsList[2]);
            wrappedItem.put("song4", songsList[3]);
            wrappedItem.put("song5", songsList[4]);

            wrappedItem.put("artist1", artistList[0]);
            wrappedItem.put("artist2", artistList[1]);
            wrappedItem.put("artist3", artistList[2]);
            wrappedItem.put("artist4", artistList[3]);
            wrappedItem.put("artist5", artistList[4]);

//            wrappedItem.put("topGenre", topFiveGenreArray.get(0));
            wrappedItem.put("topGenre", topFiveGenres[0]);
            wrappedItem.put("albumPic", albumList[0]);
            wrappedItem.put("artistPic", artistPicList[0]);

            // Set the value of the wrapped item
            wrappedRef.setValue(wrappedItem);
        }
    }

}
