package com.example.travelbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class GeminiChatActivity extends AppCompatActivity {

    private EditText inputField;
    private Button sendButton;
    private TextView chatDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gemini_chat);

        inputField = findViewById(R.id.editTextInput);
        sendButton = findViewById(R.id.buttonSend);
        chatDisplay = findViewById(R.id.textViewChat);

        sendButton.setOnClickListener(v -> {
            String userInput = inputField.getText().toString().trim();
            if (userInput.isEmpty()) return;

            chatDisplay.append("You: " + userInput + "\n");
            inputField.setText("");

            GeminiService.sendPrompt(userInput, reply -> {
                runOnUiThread(() -> {
                    chatDisplay.append("AI: " + reply + "\n");
                });
            });
        });
    }


}
