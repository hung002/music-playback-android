package com.example.echofusion.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.echofusion.R;
import com.example.echofusion.databinding.FragmentAccountBinding;
import com.example.echofusion.databinding.FragmentPastWrappedBinding;
import com.example.echofusion.ui.wrapped.tabs.Tab1;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PastWrappedFragment extends Fragment {

    private FragmentPastWrappedBinding binding;
    private String selectedDate;
    private String selectedKey;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private TextView date, song1, song2, song3, song4, song5, artist1, artist2, artist3, artist4, artist5, genre;
    private Button backBtn;
    private ImageView albumPic, artistPic;

    public PastWrappedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPastWrappedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        date = binding.date;
        song1 = binding.song1;
        song2 = binding.song2;
        song3 = binding.song3;
        song4 = binding.song4;
        song5 = binding.song5;
        artist1 = binding.artist1;
        artist2 = binding.artist2;
        artist3 = binding.artist3;
        artist4 = binding.artist4;
        artist5 = binding.artist5;
        genre = binding.topGenre;
        backBtn = binding.backButton;

        albumPic = binding.album1;
        artistPic =  binding.artistPic;

        Bundle bundle = getArguments();
        if (bundle != null) {
            selectedDate = bundle.getString("selectedDate");
            selectedKey = bundle.getString("selectedKey");
            date.setText(selectedDate);
            setInformation();
        }

        backBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_past_wrapped_to_navigation_account);
        });
        return root;
    }

    private void setInformation() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId).child("past_wrappeds").child(selectedKey).child("data");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        for (int i = 1; i <= 5; i++) {
                            String songName = snapshot.child("song" + i).getValue(String.class);
                            String artistName = snapshot.child("artist" + i).getValue(String.class);

                            switch (i) {
                                case 1:
                                    song1.setText(songName);
                                    artist1.setText(artistName);
                                    break;
                                case 2:
                                    song2.setText(songName);
                                    artist2.setText(artistName);
                                    break;
                                case 3:
                                    song3.setText(songName);
                                    artist3.setText(artistName);
                                    break;
                                case 4:
                                    song4.setText(songName);
                                    artist4.setText(artistName);
                                    break;
                                case 5:
                                    song5.setText(songName);
                                    artist5.setText(artistName);
                                    break;
                            }
                        }
                        String favoriteGenre = snapshot.child("topGenre").getValue(String.class);
                        genre.setText(favoriteGenre);

                        getActivity().runOnUiThread(() -> {
                            String artistUrl = snapshot.child("artistPic").getValue(String.class);
                            String albumUrl = snapshot.child("albumPic").getValue(String.class);
                            Picasso.get().load(artistUrl).into(artistPic);
                            Picasso.get().load(albumUrl).into(albumPic);
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // You can now populate your UI with the pastWrappeds list
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
