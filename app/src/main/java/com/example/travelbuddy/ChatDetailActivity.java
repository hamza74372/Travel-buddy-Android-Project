package com.example.travelbuddy;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messages;
    private EditText inputMessage;
    private ImageButton sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        recyclerView = findViewById(R.id.recyclerMessages);
        inputMessage = findViewById(R.id.editMessage);
        sendButton = findViewById(R.id.buttonSend);

        messages = new ArrayList<>();
        messages.add(new Message("Hey! How are you?", false));
        messages.add(new Message("I'm good, how about you?", true));
        messages.add(new Message("Doing well! Ready for the trip?", false));
        messages.add(new Message("Absolutely!", true));

        adapter = new MessageAdapter(messages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        sendButton.setOnClickListener(v -> {
            String msg = inputMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                messages.add(new Message(msg, true));
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
                inputMessage.setText("");
            }
        });

        String userName = getIntent().getStringExtra("userName");
        setTitle(userName);
    }
}
