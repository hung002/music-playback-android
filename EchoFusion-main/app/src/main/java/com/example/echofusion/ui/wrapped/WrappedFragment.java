package com.example.echofusion.ui.wrapped;

import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echofusion.ItemViewModel;
import com.example.echofusion.databinding.FragmentWrappedBinding;
import com.example.echofusion.tokenTest;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WrappedFragment extends Fragment {

    private ItemViewModel viewModel;
    private FragmentWrappedBinding binding;

    private String token;
    private String wrappedId;
    public static String timeRange = "short_term";
    private MediaPlayer songPlayer = new MediaPlayer();
    private TextView song1, song2, song3, song4, song5;
    private ImageView album1, album2, album3, album4, album5;
    private Button songBtn;
    private String[] songsList = new String[5];
    private String[] albumList = new String[5];
    private String[] urlList = new String[5];

    //for artist tab:
    private TextView artist1, artist2, artist3, artist4, artist5;
    private String[] artistList = new String[5];
    private Call mCall;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();

    //for tabs:
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;

    //for database
    protected static JSONObject profileData;
    protected static JSONArray musicData;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWrappedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        tabLayout = binding.tabLayout;
        viewPager2 = binding.viewPager;
        viewPagerAdapter = new ViewPagerAdapter(requireActivity());
        viewPager2.setAdapter(viewPagerAdapter);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
        //end of TABLAYOUT methods//

        Intent in = requireActivity().getIntent();
        Bundle b = in.getExtras();
        token = b.getString("token");
        wrappedId = b.getString("wrappedId");

        song1 = binding.song1;
        song2 = binding.song2;
        song3 = binding.song3;
        song4 = binding.song4;
        song5 = binding.song5;

        album1 = binding.album1;
        album2 = binding.album2;
        album3 = binding.album3;
        album4 = binding.album4;
        album5 = binding.album5;

        //for artist:
        artist1 = binding.artist1;
        artist2 = binding.artist2;
        artist3 = binding.artist3;
        artist4 = binding.artist4;
        artist5 = binding.artist5;

        /*artistPic1 = binding.artistPic1;
        artistPic2 = binding.artistPic2;
        artistPic3 = binding.artistPic3;
        artistPic4 = binding.artistPic4;
        artistPic5 = binding.artistPic5;*/

        songBtn = binding.songPlayer;
        songBtn.setOnClickListener((v) -> {
            createMediaPlayer(urlList);
        });

        getTopSongs();
        getTopArtists();

        return root;
    }

    private void setTextAsync(final String text, TextView textView) {
        requireActivity().runOnUiThread(() -> textView.setText(text));
    }

    private void createMediaPlayer(String[] audioUrls) {
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
    }

    public void getTopSongs() {

        if (token == null) {
            Toast.makeText(requireActivity(), "You need to get an access token first!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a request to get the users top songs
        String songUrl;
        fetchTimeRange();
        if (timeRange == null || timeRange.equals("long_term")) {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=long_term&offset=0";
        } else if (timeRange.equals("medium_term")) {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=medium_term&offset=0";
        } else {
            songUrl = "https://api.spotify.com/v1/me/top/tracks?time_range=short_term&offset=0";
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
                    profileData = jsonResponse;
                    musicData = items;

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
                        requireActivity().runOnUiThread(() -> {
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
        if (tokenTest.timeRange == "" || tokenTest.timeRange == "long_term") {
            artistUrl = "https://api.spotify.com/v1/me/top/artists?time_range=long_term&offset=0";
        } else if (tokenTest.timeRange == "medium_term") {
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
                    /*Toast.makeText(MainActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();*/
                }
            }
        });
    }

    private void fetchTimeRange() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child(userId).child("settings").child("timeFrame").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        timeRange = snapshot.getValue(String.class);
                    } else {
                        // Set default timeRange value if not found in the database
                        timeRange = "short_term";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
