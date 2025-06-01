package com.example.travelbuddy;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private List<User> users;
    private Context context;
    private OnUserClickListener listener;

    public ChatListAdapter(List<User> users, Context context, OnUserClickListener listener) {
        this.users = users;
        this.context = context;
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, lastMessage;
        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textName);
            lastMessage = itemView.findViewById(R.id.textLastMessage);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUserClick(users.get(getAdapterPosition()));
            });
        }
    }

    @NonNull
    @Override
    public ChatListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_chat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        holder.name.setText(user.getName());
        holder.lastMessage.setText(user.getLastMessage());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
