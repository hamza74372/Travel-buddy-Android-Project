package com.example.travelbuddy;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    EditText location, startDate;
    RadioGroup radioGroupDuration;
    ImageView imageViewPreview;
    MaterialCardView imagePreviewCard;
    Uri imageUri;
    MaterialButton btnSelectImage, btnCreatePost;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Initialize views
        location = findViewById(R.id.editTextLocation);
        startDate = findViewById(R.id.editTextStartDate);
        radioGroupDuration = findViewById(R.id.radioGroupDuration);
        imageViewPreview = findViewById(R.id.imageViewPreview);
        imagePreviewCard = findViewById(R.id.imagePreviewCard);
        btnSelectImage = findViewById(R.id.buttonUploadImage);
        btnCreatePost = findViewById(R.id.buttonCreatePost);

        // Set click listeners
        startDate.setOnClickListener(v -> showDatePicker());
        btnSelectImage.setOnClickListener(v -> selectImage());
        btnCreatePost.setOnClickListener(v -> uploadPost());

        // Register ActivityResultLauncher for image picker
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        displaySelectedImage();
                    }
                }
        );
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String dateStr = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
            startDate.setText(dateStr);
        }, year, month, day);

        dialog.show();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void displaySelectedImage() {
        try {
            imageViewPreview.setImageURI(imageUri);
            imagePreviewCard.setVisibility(View.VISIBLE);
            btnSelectImage.setText("Change Image");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPost() {
        String tripLocation = location.getText().toString().trim();
        String tripDate = startDate.getText().toString().trim();

        int selectedDurationId = radioGroupDuration.getCheckedRadioButtonId();
        if (selectedDurationId == -1) {
            Toast.makeText(this, "Please select trip duration!", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadio = findViewById(selectedDurationId);
        String tripDuration = selectedRadio.getText().toString();

        if (tripLocation.isEmpty()) {
            Toast.makeText(this, "Please enter trip location!", Toast.LENGTH_SHORT).show();
            location.requestFocus();
            return;
        }

        if (tripDate.isEmpty()) {
            Toast.makeText(this, "Please select start date!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnCreatePost.setText("Creating Post...");
        btnCreatePost.setEnabled(false);

        String postId = FirebaseFirestore.getInstance().collection("posts").document().getId();
        StorageReference imgRef = FirebaseStorage.getInstance().getReference("postImages/" + postId + ".jpg");

        imgRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Map<String, Object> post = new HashMap<>();
                    post.put("location", tripLocation);
                    post.put("date", tripDate);
                    post.put("duration", tripDuration);
                    post.put("imageUrl", uri.toString());
                    post.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    post.put("timestamp", System.currentTimeMillis());

                    FirebaseFirestore.getInstance().collection("posts").document(postId)
                            .set(post)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error creating post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                resetCreateButton();
                            });
                })
        ).addOnFailureListener(e -> {
            Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            resetCreateButton();
        });
    }

    private void resetCreateButton() {
        btnCreatePost.setText("Create Post");
        btnCreatePost.setEnabled(true);
    }

    private void clearImageSelection() {
        imageUri = null;
        imageViewPreview.setImageDrawable(null);
        imagePreviewCard.setVisibility(View.GONE);
        btnSelectImage.setText("Select Image");
    }
}
