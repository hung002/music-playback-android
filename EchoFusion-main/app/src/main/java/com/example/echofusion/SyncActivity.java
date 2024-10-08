package com.example.echofusion;


import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

//import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.CloseableHttpResponse;
import com.example.echofusion.ui.wrapped.WrappedFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class SyncActivity extends AppCompatActivity {

    public static final String CLIENT_ID = "5260461b5d7e414d9093f85038fc4c56";
    public static final String REDIRECT_URI = "com.example.spotifysdk://auth";

    public static final int AUTH_TOKEN_REQUEST_CODE = 0;
    public static final int AUTH_CODE_REQUEST_CODE = 1;

    private String wrappedId;
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    protected static String mAccessToken, mAccessCode;
    private Call mCall;
    Button timeButton;

    TextView userName;
    TextView welcome;
    TextView textTime;
    Button logout;
    Button sync;
    public static String timeRange;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        Intent in = getIntent();
        Bundle temp = in.getExtras();
        Bundle temp2 = new Bundle();

        Bundle tokenBundle = new Bundle();

        logout = findViewById(R.id.logout);
        sync = findViewById(R.id.spotifySync);
        userName = findViewById(R.id.userName);
        welcome = findViewById(R.id.welcome);
        textTime = findViewById(R.id.text_time_frame1);
        timeButton = findViewById(R.id.btn_time_frame1);

        displayName();

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getToken();
            }
        });

        logout.setOnClickListener(v -> {
            mAuth.signOut();
            finish();
            startActivity(new Intent(SyncActivity.this, LoginActivity.class));
        });
        timeButton.setOnClickListener(v -> {
            showTimeDialog();
        });
    }

    /**
     * Get token from Spotify
     * This method will open the Spotify login activity and get the token
     * What is token?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getToken() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN);
        AuthorizationClient.openLoginActivity(SyncActivity.this, AUTH_TOKEN_REQUEST_CODE, request);
    }

    /**
     * Get code from Spotify
     * This method will open the Spotify login activity and get the code
     * What is code?
     * https://developer.spotify.com/documentation/general/guides/authorization-guide/
     */
    public void getCode() {
        final AuthorizationRequest request = getAuthenticationRequest(AuthorizationResponse.Type.CODE);
        AuthorizationClient.openLoginActivity(SyncActivity.this, AUTH_CODE_REQUEST_CODE, request);
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
            getCode();
        } else if (AUTH_CODE_REQUEST_CODE == requestCode) {
            mAccessCode = response.getCode();
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
                Toast.makeText(SyncActivity.this, "Failed to fetch data, watch Logcat for more details",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final JSONObject jsonObject = new JSONObject(response.body().string());

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        String userId = user.getUid();
                        String userName = jsonObject.getString("display_name");
                        updateUserProfile(userId, userName);
                    }

                    finish();
                    Intent intent = new Intent(SyncActivity.this, MainActivity.class);
                    intent.putExtra("token", mAccessToken);
                    intent.putExtra("wrappedId", wrappedId);
                    startActivity(intent);
                } catch (JSONException e) {
                    Log.d("JSON", "Failed to parse data: " + e);
                    Toast.makeText(SyncActivity.this, "Failed to parse data, watch Logcat for more details",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Updates the user profile in Firebase with the correct name
     * @param userId User ID
     * @param userName User name
     */
    private void updateUserProfile(String userId, String userName) {
        mDatabase.child(userId).child("profile").child("name").setValue(userName)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firebase", "User profile updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Firebase", "User profile update failed: " + e.getMessage());
                    }
                });
        addWrappedToPastWrappeds();
    }

    private void addWrappedToPastWrappeds() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference wrappedRef = mDatabase.child(userId).child("past_wrappeds").push();
            wrappedId = wrappedRef.getKey();
            wrappedRef.setValue(getWrappedItem());
        }
    }

    private HashMap<String, Object> getWrappedItem() {
        HashMap<String, Object> wrappedItem = new HashMap<>();
        wrappedItem.put("title", "Wrapped"); // Set your title here
        wrappedItem.put("date", getCurrentDate()); // Set current date

        return wrappedItem;
    }

    private String getCurrentDate() {
        // Get current date
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void displayName() {

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child(userId).child("profile").child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        if (name != null || !name.equals("")) {
                            welcome.setText("Welcome " + name);
                        } else {
                            welcome.setText("Welcome user!");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
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
    private void showTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_time, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button year_timeSpan = dialogView.findViewById(R.id.year);
        Button half_year_timeSpan = dialogView.findViewById(R.id.half_year);
        Button month_timeSpan = dialogView.findViewById(R.id.month);

        year_timeSpan.setOnClickListener(v -> {
            setTimeRange("long_term");
            updateTimeInDatabase();
            textTime.setText("Time Frame: 1 year");
            dialog.dismiss();
        });
        half_year_timeSpan.setOnClickListener(v -> {
            setTimeRange("medium_term");
            updateTimeInDatabase();
            textTime.setText("Time Frame: 6 months");
            dialog.dismiss();
        });
        month_timeSpan.setOnClickListener(v -> {
            setTimeRange("short_term");
            updateTimeInDatabase();
            textTime.setText("Time Frame: 1 month");
            dialog.dismiss();
        });

        dialog.show();
    }
    private void updateTimeInDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child(userId).child("settings").child("timeFrame").setValue(timeRange)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Time frame updated
                            Toast.makeText(this, "Time frame updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to update time frame
                            Toast.makeText(this, "Failed to update time frame: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setTimeRange(String range) {
        timeRange = range;
    }

}