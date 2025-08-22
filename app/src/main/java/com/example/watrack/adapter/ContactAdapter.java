package com.example.watrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watrack.R;
import com.example.watrack.model.Contact;

public class ContactAdapter extends ListAdapter<Contact, ContactAdapter.VH> {

    public interface Listener {
        void onClick(Contact c);
        void onMore(View anchor, Contact c);
    }

    private final Listener listener;

    public ContactAdapter(Listener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Contact> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Contact>() {
                @Override
                public boolean areItemsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull Contact oldItem, @NonNull Contact newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.isOnline() == newItem.isOnline()
                            && oldItem.getLastSeen().equals(newItem.getLastSeen())
                            && oldItem.getDuration().equals(newItem.getDuration())
                            && oldItem.getAvatarResId() == newItem.getAvatarResId();
                }
            };

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Contact c = getItem(position);
        holder.bind(c, listener);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, lastSeen, duration;
        ImageView avatar;
        ImageButton btnMore;

        VH(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contact_name);
            lastSeen = itemView.findViewById(R.id.contact_last_seen);
            duration = itemView.findViewById(R.id.contact_duration);
            avatar = itemView.findViewById(R.id.contact_avatar);
            btnMore = itemView.findViewById(R.id.contact_more);
        }

        void bind(Contact c, Listener listener) {
            name.setText(c.getName());
            lastSeen.setText(c.getLastSeen());
            duration.setText(c.getDuration());
            avatar.setImageResource(c.getAvatarResId());

            itemView.setOnClickListener(v -> listener.onClick(c));
            btnMore.setOnClickListener(v -> listener.onMore(btnMore, c));
        }
    }
}
