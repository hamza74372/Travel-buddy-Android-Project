package com.example.travelbuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private FirebaseFirestore db;

    public ExploreFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        adapter = new PostAdapter(getContext(), postList, this::openChatWithUser);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadPosts();

        return view;
    }

    private void loadPosts() {
        db.collection("posts").get().addOnSuccessListener(queryDocumentSnapshots -> {
            postList.clear();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Post post = doc.toObject(Post.class);
                postList.add(post);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void openChatWithUser(String otherUserId) {
        // Save selected user ID
        SharedPreferences prefs = requireContext().getSharedPreferences("chat_prefs", Context.MODE_PRIVATE);
        prefs.edit().putString("receiverId", otherUserId).apply();

        // Switch to ChatFragment via Bottom Navigation
        BottomNavigationView navView = getActivity().findViewById(R.id.bottom_navigation);
        navView.setSelectedItemId(R.id.nav_chat);  // Replace with your actual menu item ID
    }
}
