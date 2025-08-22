package com.example.watrack.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watrack.R;
import com.example.watrack.model.ActivityLog;
public class ActivityAdapter extends ListAdapter<ActivityLog, ActivityAdapter.VH> {


    public ActivityAdapter() {
        super(new DiffUtil.ItemCallback<ActivityLog>() {
            @Override public boolean areItemsTheSame(@NonNull ActivityLog o, @NonNull ActivityLog n) {
                return o.getText().equals(n.getText()) && o.getTime().equals(n.getTime());
            }
            @Override public boolean areContentsTheSame(@NonNull ActivityLog o, @NonNull ActivityLog n) {
                return areItemsTheSame(o,n);
            }
        });
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.item_activity, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ActivityLog log = getItem(position);
        h.activity.setText(log.getText());
        h.time.setText(log.getTime());
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView activity, time;
        VH(@NonNull View itemView) {
            super(itemView);
            activity = itemView.findViewById(R.id.tvActivity);
            time = itemView.findViewById(R.id.tvTime);
        }
    }
}

