package com.example.travelbuddy;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GeminiService {
    // Replace with your actual API key from Google Cloud Console
    private static final String API_KEY = "AIzaSyDzz-ienrYsj8Ir0Cq55hqpu5O7D1LIVOM";

    public static void sendPrompt(String prompt, GeminiCallback callback) {
        new Thread(() -> {
            try {
                URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

                // Build JSON request body
                JSONObject bodyJson = new JSONObject()
                        .put("contents", new JSONArray()
                                .put(new JSONObject()
                                        .put("parts", new JSONArray()
                                                .put(new JSONObject().put("text", prompt)))));

                // Send request body
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(bodyJson.toString().getBytes("UTF-8"));
                    os.flush();
                }

                // Read response or error stream
                InputStream inputStream;
                int responseCode = conn.getResponseCode();
                if (responseCode >= 400) {
                    inputStream = conn.getErrorStream();
                } else {
                    inputStream = conn.getInputStream();
                }

                Scanner scanner = new Scanner(inputStream);
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                if (responseCode >= 400) {
                    // Log and callback error message
                    Log.e("GeminiService", "Error response: " + response);
                    callback.onReply("API Error: " + response);
                    return;
                }

                JSONObject jsonResponse = new JSONObject(response.toString());

                // Parse the AI response text
                String reply = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                callback.onReply(reply);
            } catch (Exception e) {
                Log.e("GeminiService", "Exception: ", e);
                callback.onReply("Exception: " + e.getMessage());
            }
        }).start();
    }

    // Callback interface to return AI response asynchronously
    public interface GeminiCallback {
        void onReply(String reply);
    }
}
