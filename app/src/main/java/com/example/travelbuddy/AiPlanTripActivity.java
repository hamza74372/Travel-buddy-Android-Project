package com.example.travelbuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AiPlanTripActivity extends AppCompatActivity {

    private CheckBox cbAdventure, cbBeaches, cbTrekking, cbCalm, cbCultural;
    private RadioGroup rgCompanions, rgBudget;
    private Spinner spinnerDuration;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_plan_trip);

        // Initialize UI elements
        cbAdventure = findViewById(R.id.cb_adventure);
        cbBeaches = findViewById(R.id.cb_beaches);
        cbTrekking = findViewById(R.id.cb_trekking);
        cbCalm = findViewById(R.id.cb_calm);
        cbCultural = findViewById(R.id.cb_cultural);

        rgCompanions = findViewById(R.id.rg_travel_companions);
        rgBudget = findViewById(R.id.rg_budget);

        spinnerDuration = findViewById(R.id.spinner_duration);
        btnSubmit = findViewById(R.id.btn_submit);

        // Setup Spinner data
        setupDurationSpinner();

        // Submit button click listener
        btnSubmit.setOnClickListener(v -> collectAndSendData());
    }

    private void setupDurationSpinner() {
        ArrayList<String> durations = new ArrayList<>();
        durations.add("1-3 days");
        durations.add("4-7 days");
        durations.add("1-2 weeks");
        durations.add("More than 2 weeks");

        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                durations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(adapter);
    }

    private void collectAndSendData() {
        // Collect Travel Types
        ArrayList<String> travelTypes = new ArrayList<>();
        if (cbAdventure.isChecked()) travelTypes.add("Adventure");
        if (cbBeaches.isChecked()) travelTypes.add("Beaches");
        if (cbTrekking.isChecked()) travelTypes.add("Trekking");
        if (cbCalm.isChecked()) travelTypes.add("Calm & Relaxing");
        if (cbCultural.isChecked()) travelTypes.add("Cultural");

        if (travelTypes.isEmpty()) {
            Toast.makeText(this, "Please select at least one travel type", Toast.LENGTH_SHORT).show();
            return;
        }

        // Travel companions
        int selectedCompanionId = rgCompanions.getCheckedRadioButtonId();
        if (selectedCompanionId == -1) {
            Toast.makeText(this, "Please select travel companion", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedCompanion = findViewById(selectedCompanionId);
        String companion = selectedCompanion.getText().toString();

        // Budget
        int selectedBudgetId = rgBudget.getCheckedRadioButtonId();
        if (selectedBudgetId == -1) {
            Toast.makeText(this, "Please select your budget", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selectedBudget = findViewById(selectedBudgetId);
        String budget = selectedBudget.getText().toString();

        // Duration
        String duration = spinnerDuration.getSelectedItem().toString();

        // Prepare data string (or JSON) to send to AI
        String userInput = "Travel Types: " + travelTypes.toString() +
                "\nTravel Companion: " + companion +
                "\nBudget: " + budget +
                "\nDuration: " + duration;

        // For testing, show in Toast (replace with AI call later)
        Toast.makeText(this, userInput, Toast.LENGTH_LONG).show();

        // TODO: Call your AI function here with userInput and get suggestions
        callAI(userInput);
    }

    private void callAI(String userInput) {
        // Placeholder for AI integration code
        // You can send userInput to your AI API and get response here

        // For now, just simulate AI response
        String aiResponse = "Suggested places in Pakistan based on your preferences:\n- Hunza Valley\n- Skardu\n- Fairy Meadows";

        // Show AI response (you can display it in a dialog or new activity)
        Toast.makeText(this, aiResponse, Toast.LENGTH_LONG).show();
    }
}
