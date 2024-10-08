package com.example.echofusion;

import static android.app.PendingIntent.getActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

//import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.CloseableHttpResponse;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.media.MediaPlayer;

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

public class tokenTest extends AppCompatActivity {

    public static final String CLIENT_ID = "5260461b5d7e414d9093f85038fc4c56";
    public static final String REDIRECT_URI = "com.example.spotifysdk://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    public static String timeRange = "short_term";
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    protected static String mAccessToken, mAccessCode;
    private Call mCall;
    private String[] songsList = new String[5];
    private String[] albumList = new String[5];
    private String[] artistList = new String[5];
    private ArrayList<String> topFiveGenreArray = new ArrayList<>();
    private HashMap<String, Integer> genreCount = new HashMap<>();
    private TextView tokenTextView, codeTextView, profileTextView;
    private TextView artist1, artist2, artist3, artist4, artist5;
    private TextView song1, song2, song3, song4, song5;
    private ImageView album1;
    private MediaPlayer songPlayer = new MediaPlayer();
    private String[] urlList = new String[5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.token_fragment);

        Intent in = getIntent();
        Bundle temp = in.getExtras();
        Bundle temp2 = new Bundle();

        Bundle tokenBundle = new Bundle();

        // Initialize the views
        tokenTextView = (TextView) findViewById(R.id.token_text_view);
        codeTextView = (TextView) findViewById(R.id.code_text_view);
        profileTextView = (TextView) findViewById(R.id.response_text_view);


        // artist names
        artist1 = (TextView) findViewById(R.id.artist1);
        artist2 = (TextView) findViewById(R.id.artist2);
        artist3 = (TextView) findViewById(R.id.artist3);
        artist4 = (TextView) findViewById(R.id.artist4);
        artist5 = (TextView) findViewById(R.id.artist5);

        // song names
        song1 = (TextView) findViewById(R.id.song1);
        song2 = (TextView) findViewById(R.id.song2);
        song3 = (TextView) findViewById(R.id.song3);
        song4 = (TextView) findViewById(R.id.song4);
        song5 = (TextView) findViewById(R.id.song5);

        // Set the click listeners for the buttons
        //Initialize the album covers
        album1 = (ImageView) findViewById(R.id.album1);

        // Initialize the buttons
        Button tokenBtn = (Button) findViewById(R.id.token_btn);
        Button songBtn = (Button) findViewById(R.id.song_btn);
        Button emailButton = findViewById(R.id.email_button);
        Button timeButton = findViewById(R.id.timeSpan);


        // Set the click listeners for the buttons

        tokenBtn.setOnClickListener((v) -> {
            getToken();
        });

        songBtn.setOnClickListener((v) -> {
            //getTopSongs();
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(tokenTest.this, MainActivity.class);
                intent.putExtra("token", mAccessToken); // Assuming mAccessToken is your token value
                startActivity(intent);
            }
        });
        timeButton.setOnClickListener(v -> showDialog());
    }

    // method creates media player and plays song
    /*private void createMediaPlayer(String[] audioUrls) {
        songPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            playSong(audioUrls, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // method plays songs
    private void playSong(final String[] audioUrls, final int currentIndex) throws IOException {
        if (currentIndex >= 0 && currentIndex < audioUrls.length) {
            try {
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
                        try {
                            playSong(audioUrls, currentIndex + 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/
    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(tokenTest.this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(tokenTest.this, AUTH_CODE_REQUEST_CODE, request);
    }

    /**
     * When the app leaves this activity to momentarily get a token/code, this function
     * fetches the result of that external activity to get the response from Spotify
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, data);

        // Check which request code is present (if any)
        if (AUTH_TOKEN_REQUEST_CODE == requestCode) {
            mAccessToken = response.getAccessToken();
            setTextAsync(mAccessToken, tokenTextView);
            getCode();
        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
            setTextAsync(mAccessCode, codeTextView);
            onGetUserProfileClicked();
        }
    }

    /**
     * Get user profile
     * This method will get the user profile using the token
     */
    public void onGetUserProfileClicked() {
        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the user profile
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

        cancelCall();
        mCall = mOkHttpClient.newCall(request);

        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("HTTP", "Failed to fetch data: " + e);
                Toast.makeText(tokenTest.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());
                    setTextAsync(jsonObject.toString(3), profileTextView);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(tokenTest.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Creates a UI thread to update a TextView in the background
     * Reduces UI latency and makes the system perform more consistently
     *
     * @param text the text to set
     * @param textView TextView object to update
     */
    private void setTextAsync(final String text, TextView textView) {
        runOnUiThread(() -> textView.setText(text));

    }
    private void sendEmail() {
        // Check if there is an email app installed
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // Only email apps should handle this

        // Set email address, subject, and body
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"anthonygentile276@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "HELP");
        intent.putExtra(Intent.EXTRA_TEXT, "ROCKET LEAGUE?");

        // Verify if there's an email app that can handle this intent
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // If no email app is available, show a toast or handle it accordingly
            Toast.makeText(this, "No email app installed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Get authentication request
     *
     * @param type the type of the request
     * @return the authentication request
     */
    private AuthorizationRequest getAuthenticationRequest(AuthorizationResponse.Type type) {
        return new AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
                .setShowDialog(false)
                .setScopes(new String[] { "user-top-read", "user-modify-playback-state" }) // <--- Change the scope of your requested token here
                .setCampaign("your-campaign-token")
                .build();
    }

    /**
     * Gets the redirect Uri for Spotify
     *
     * @return redirect Uri object
     */
    private Uri getRedirectUri() {
        return Uri.parse(REDIRECT_URI);
    }

    private void cancelCall() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        cancelCall();
        super.onDestroy();
    }

    // Create a request to get the user profile
    final Request request = new Request.Builder()
            .url("https://api.spotify.com/v1/me")
            .addHeader("Authorization", "Bearer " + mAccessToken)
            .build();


    /*public void getTopSongs() {

        if (mAccessToken == null) {
            Toast.makeText(this, "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the users top songs
        String songUrl;
        if (timeRange == "" || timeRange == "long_term") {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=long_term&offset=0";
        } else if (timeRange == "medium_term") {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&offset=0";
        } else {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=short_term&offset=0";
        }

        final Request sRequest = new Request.Builder()
                .url(songUrl)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();



        cancelCall();
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

                    for (int i = 0; i < 5; i++) {
                        songsList[i] = items.getJSONObject(i).getString("name");
                        urlList[i] = items.getJSONObject(i).getString("preview_url");
                        JSONArray images = items.getJSONObject(i).getJSONObject("album").getJSONArray("images");
                        if (images.length() > 0) {
                            // Get the URL of the first image (thumbnail size) from the album images array
                            albumList[i] = images.getJSONObject(0).getString("url");
                        } else {
                            // If no image available, set a default image URL or handle as needed
                            albumList[i] = ""; // Or set a default image URL
                        }
                    }

                    setTextAsync(songsList[0], song1);
                    setTextAsync(songsList[1], song2);
                    setTextAsync(songsList[2], song3);
                    setTextAsync(songsList[3], song4);
                    setTextAsync(songsList[4], song5);


                    if (album1 != null) {
                        runOnUiThread(() -> {
                            Picasso.get().load(albumList[0]).into(album1);
                        });
                    } else {
                        Log.e("Error", "ImageView album is null");
                    }
                    createMediaPlayer(urlList);

                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse top songs: " + e);
                    //Toast.makeText(MainActivity.this, "Failed to parse top songs, watch Logcat for more details",
                    //      Toast.LENGTH_SHORT).show();
                }
            }
        });
    }*/
    // gets top artists
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
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
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

                    for (int i = 0; i < 5; i++) {
                        artistList[i] = items.getJSONObject(i).getString("name");

                    }

                    setTextAsync(artistList[0], artist1);
                    setTextAsync(artistList[1], artist2);
                    setTextAsync(artistList[2], artist3);
                    setTextAsync(artistList[3], artist4);
                    setTextAsync(artistList[4], artist5);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(tokenTest.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getTopGenres() {
        // Create a request to get the user top artists
        final Request request = new Request.Builder()
                .url("https://api.spotify.com/v1/me/top/artists?limit=50")
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();
        cancelCall();
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
                    Toast.makeText(tokenTest.this, "Failed to parse data, watch Logcat for more details",
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
        }

        return topGenres;
    }

    // creates dialog to show time range options
     /* protected void showDialog(){

        Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(true);

        View view  = getActivity().getLayoutInflater().inflate(R.layout.time_dialog, null);
        dialog.setContentView(view);

        Button year_timeSpan = view.findViewById(R.id.year);
        Button half_year_timeSpan = view.findViewById(R.id.half_year);
        Button month_timeSpan = view.findViewById(R.id.month);
        year_timeSpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeRange = "long_term";

            }
        });
        half_year_timeSpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeRange = "medium_term";

            }
        });
        month_timeSpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeRange = "short_term";
            }
        });
        dialog.show();
    };*/
    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setCancelable(true);

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_time, null);
        dialog.setContentView(view);

        Button year_timeSpan = view.findViewById(R.id.year);
        Button half_year_timeSpan = view.findViewById(R.id.half_year);
        Button month_timeSpan = view.findViewById(R.id.month);

        year_timeSpan.setOnClickListener(v -> setTimeRange("long_term"));
        half_year_timeSpan.setOnClickListener(v -> setTimeRange("medium_term"));
        month_timeSpan.setOnClickListener(v -> setTimeRange("short_term"));

        dialog.show();
    }

    private void setTimeRange(String range) {
        timeRange = range;
    }
}

