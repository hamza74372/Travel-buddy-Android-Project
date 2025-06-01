package com.example.travelbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;
    private OnChatClickListener chatClickListener;

    public interface OnChatClickListener {
        void onChatClicked(String userId);
    }

    public PostAdapter(Context context, List<Post> postList, OnChatClickListener listener) {
        this.context = context;
        this.postList = postList;
        this.chatClickListener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.location.setText(post.getLocation());
        holder.date.setText(post.getDate());
        holder.duration.setText(post.getDuration());
        Glide.with(context).load(post.getImageUrl()).into(holder.imageView);

        holder.chatBtn.setOnClickListener(v -> {
            if (chatClickListener != null) {
                chatClickListener.onChatClicked(post.getUserId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView location, date, duration;
        ImageView imageView;
        Button chatBtn;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            location = itemView.findViewById(R.id.textLocation);
            date = itemView.findViewById(R.id.textDate);
            duration = itemView.findViewById(R.id.textDuration);
            imageView = itemView.findViewById(R.id.postImageView);
            chatBtn = itemView.findViewById(R.id.buttonChat);
        }
    }
}
