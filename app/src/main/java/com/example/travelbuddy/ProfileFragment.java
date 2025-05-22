package com.example.travelbuddy;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView userInfo;
    private Button btnLogout, btnUpload;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private SharedPreferences preferences;
    private Uri savedImageUri;
    private static final String PREFS_NAME = "TravelBuddyPrefs";
    private static final String KEY_PROFILE_IMAGE_URI = "profile_image_uri";

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        // Initialize auth state listener to handle session changes
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                // User is signed out, redirect to login activity
                redirectToLogin();
            }
        };

        // Initialize shared preferences for saving app state
        preferences = requireActivity().getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        // Retrieve saved image URI if exists
        String savedUriString = preferences.getString(KEY_PROFILE_IMAGE_URI, null);
        if (savedUriString != null) {
            savedImageUri = Uri.parse(savedUriString);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImage = view.findViewById(R.id.profile_image);
        userInfo = view.findViewById(R.id.tv_user_info);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnUpload = view.findViewById(R.id.btn_upload_image);

        // Check if user is logged in
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            userInfo.setText(user.getEmail()); // or user.getDisplayName()
        } else {
            // If somehow user is not logged in, redirect to login
            redirectToLogin();
            return view;
        }

        // Restore saved profile image if exists
        if (savedImageUri != null) {
            profileImage.setImageURI(savedImageUri);
        }

        // Initialize ActivityResultLauncher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        profileImage.setImageURI(imageUri);

                        // Save the profile image URI to preferences
                        saveProfileImageUri(imageUri);
                    }
                });

        // Launch gallery intent
        btnUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            // Clear saved data when logging out
            clearUserData();

            // Sign out from Firebase
            mAuth.signOut();
            Toast.makeText(getActivity(), "Logged out", Toast.LENGTH_SHORT).show();

            // Redirect to login screen
            redirectToLogin();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add auth state listener when fragment starts
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove auth state listener when fragment stops
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void redirectToLogin() {
        // Create an intent to navigate to the LoginActivity
        Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void saveProfileImageUri(Uri imageUri) {
        if (imageUri != null) {
            // Save the URI string to shared preferences
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(KEY_PROFILE_IMAGE_URI, imageUri.toString());
            editor.apply();

            // Update the saved URI variable
            savedImageUri = imageUri;
        }
    }

    private void clearUserData() {
        // Clear all saved user data from shared preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_PROFILE_IMAGE_URI);
        // You can add more items to clear here
        editor.apply();
    }
}