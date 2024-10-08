package com.example.echofusion.ui.account;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.echofusion.LoginActivity;
import com.example.echofusion.R;
import com.example.echofusion.databinding.FragmentAccountBinding;
import com.google.android.material.textfield.TextInputEditText;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountFragment extends Fragment {

    private String timeRange;
    private TextView textEmail;
    private TextInputEditText textPassword;
    private TextView textName;
    private TextView textTime;
    private FragmentAccountBinding binding;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private String userId;

    private Button editPassword;
    private Button deleteAccount;
    private Button logout;
    private Button pastWrappedsButton;

    private List<String> pastWrappedList = new ArrayList<>();
    private Map<String, String> pastWrappedMap = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAccountBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textEmail = binding.textEmail;
        textPassword = binding.textPassword;
        textName = binding.textWelcome;
        textTime = binding.textTimeFrame;
        editPassword = binding.editPassword;
        deleteAccount = binding.deleteAccount;
        logout = binding.logout;
        pastWrappedsButton = binding.btnPastWrappeds;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        user = mAuth.getCurrentUser();
        userId = user.getUid();


        CheckBox showPasswordCheckBox = binding.checkShowPassword;

        // Retrieve user information
        String userName = getUserName(); // Method to retrieve user's name
        String userEmail = getUserEmail(); // Method to retrieve user's email
        String userPassword = getUserPassword(); // Method to retrieve user's password

        // Set user's email and password to the corresponding fields
        textName.setText(userName);
        textEmail.setText(userEmail);
        textPassword.setText(userPassword);

        fetchTimeRange();
        fetchPastWrappedList();

        // Set initial input type for password field
        textPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        // Set onClickListener for password visibility toggle
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                textPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                textPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        editPassword.setOnClickListener(v -> {
            updateUserPassword();
        });

        deleteAccount.setOnClickListener(v -> {
            deleteAccount();
        });

        logout.setOnClickListener(v -> {
            mAuth.signOut();
            getActivity().finish();
            startActivity(new Intent(getActivity(), LoginActivity.class));
        });

        pastWrappedsButton.setOnClickListener(v -> {
            showPastWrappedDialog();
        });


        return root;
    }



    private void updateUserPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        EditText passwordBox = dialogView.findViewById(R.id.passwordBox);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newPassword = passwordBox.getText().toString();

                FirebaseUser user = mAuth.getCurrentUser();
                String userId = user.getUid();
                if (user != null) {
                    // Get user's current email
                    String email = user.getEmail();
                    if (email != null) {
                        // Get the credential
                        AuthCredential credential = EmailAuthProvider.getCredential(email, LoginActivity.pass);
                        // Reauthenticate user
                        user.reauthenticate(credential).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Change password
                                user.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        // Password
                                        Toast.makeText(getContext(), "Update Successful", Toast.LENGTH_SHORT).show();
                                        mDatabase.child(userId).child("profile").child("password").setValue(newPassword);
                                        textPassword.setText(newPassword);
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(getContext(), "Update Failed" + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), "Reauthentication Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view1 -> dialog.dismiss());
        if (dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_delete, null);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    String email = user.getEmail();
                    if (email != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(email, LoginActivity.pass);
                        // Reauthenticate user
                        user.reauthenticate(credential)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Delete account
                                        user.delete()
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        // Account deleted
                                                        mDatabase.child(user.getUid()).removeValue().addOnCompleteListener(task2 -> {
                                                            if (task.isSuccessful()) {
                                                                getActivity().finish();
                                                                dialog.dismiss();
                                                                startActivity(new Intent(getContext(), LoginActivity.class));
                                                            } else {
                                                                Toast.makeText(getContext(), "Failed to delete user data", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(getContext(), "Action Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                }

            }
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view1 -> dialog.dismiss());
        if (dialog.getWindow() != null){
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }

    /*private void showTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
    }*/

    private void updateTimeInDatabase() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            mDatabase.child(userId).child("settings").child("timeFrame").setValue(timeRange)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Time frame updated
                            Toast.makeText(getActivity(), "Time frame updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Failed to update time frame
                            Toast.makeText(getActivity(), "Failed to update time frame: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setTimeRange(String range) {
        timeRange = range;
    }

    private String getUserName() {
        if (user != null) {
            mDatabase.child(userId).child("profile").child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            textName.setText("Welcome, " + name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
        return textName.getText().toString();
    }

    private String getUserEmail() {
        if (user != null) {
            mDatabase.child(userId).child("profile").child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String email = snapshot.getValue(String.class);
                        if (email != null) {
                            textEmail.setText(email);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
        return textEmail.getText().toString();
    }

    private String getUserPassword() {
        if (user != null) {
            mDatabase.child(userId).child("profile").child("password").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String password = snapshot.getValue(String.class);
                        if (password != null) {
                            textPassword.setText(password);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
        return textPassword.getText().toString();
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

                    if (timeRange.equals("long_term")) {
                        textTime.setText("Time Frame: 1 year");
                    } else if (timeRange.equals("medium_term")) {
                        textTime.setText("Time Frame: 6 months");
                    } else {
                        textTime.setText("Time Frame: 1 month");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        }
    }

    private void showPastWrappedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_past_wrapped, null);

        Spinner spinner = dialogView.findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, pastWrappedList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        btnConfirm.setOnClickListener(view -> {
            String selectedDate = spinner.getSelectedItem().toString();
            String selectedKey = pastWrappedMap.get(selectedDate);
            PastWrappedFragment pastWrappedFragment = new PastWrappedFragment();
            Bundle bundle = new Bundle();
            bundle.putString("selectedDate", selectedDate);
            bundle.putString("selectedKey", selectedKey);
            pastWrappedFragment.setArguments(bundle);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_account_to_navigation_past_wrapped, bundle);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void fetchPastWrappedList() {
        mDatabase.child(userId).child("past_wrappeds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pastWrappedList.clear(); // Clear the list before adding new items
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String date = ds.child("date").getValue(String.class); // Assuming the key is the date
                    String key = ds.getKey();
                    pastWrappedList.add(date);
                    pastWrappedMap.put(date, key);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(getContext(), "Failed to fetch past wrapped data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}