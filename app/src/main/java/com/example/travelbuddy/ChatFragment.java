package com.example.travelbuddy;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = view.findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        userList.add(new User("1", "John Doe", "Hey there!"));
        userList.add(new User("2", "Emma Watson", "Are we meeting today?"));
        userList.add(new User("3", "Alex Smith", "Check this out"));

        adapter = new ChatListAdapter(userList, getContext(), user -> {
            Intent intent = new Intent(getContext(), ChatDetailActivity.class);
            intent.putExtra("userName", user.getName());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        return view;
    }
}
