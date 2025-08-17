package com.example.watrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TrackedUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<String> trackedUsers = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracked_users);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        loadTrackedUsers();
    }

    private void loadTrackedUsers() {
        db.collection("tracked_users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    trackedUsers.add(document.getString("phoneNumber"));
                }
                userAdapter = new UserAdapter(trackedUsers);
                recyclerView.setAdapter(userAdapter);
            }
        });
    }

    // Adapter Class (Fix: Static ViewHolder)
    private static class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
        private final List<String> users;

        UserAdapter(List<String> users) {
            this.users = users;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String user = users.get(position);
            holder.textView.setText(user);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(holder.itemView.getContext(), UserDetailsActivity.class);
                intent.putExtra("phoneNumber", user);
                holder.itemView.getContext().startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        // Fix: Make ViewHolder static to avoid nesting issues
        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
