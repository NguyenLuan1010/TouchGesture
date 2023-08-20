package com.dyson.tech.touchgesture.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.model.Apps;

import java.util.ArrayList;
import java.util.List;

public class GesturesListAdapter extends RecyclerView.Adapter<GesturesListAdapter.GestureListViewHolder> {

    private List<Apps> appsList;
    private List<Apps> filteredDataList;
    private final ActionClickGestureItem listener;

    public GesturesListAdapter(ActionClickGestureItem listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAppsList(List<Apps> appsList) {
        this.appsList = appsList;
        this.filteredDataList = appsList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        List<Apps> filteredList = new ArrayList<>();

        for (Apps data : appsList) {
            if (getAppName(data.getName()).toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(data);
            }
        }
        filteredDataList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GestureListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gesture_item, parent, false);
        return new GesturesListAdapter.GestureListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GestureListViewHolder holder, int position) {
        Apps app = filteredDataList.get(position);
        holder.tvAppName.setText(getAppName(app.getName()));
        holder.imgApp.setImageDrawable(app.getIcon());
        holder.gestureLayout.setOnClickListener(view -> {
            listener.onClickLayout(app);
        });
        holder.imgDelete.setOnClickListener(view -> {
            listener.onClickDelete(app);
        });
        holder.imgEdit.setOnClickListener(view -> {
            listener.onClickEdit(app);
        });
    }

    String getAppName(String fileName) {
        return fileName.split("\\.")[0];
    }

    @Override
    public int getItemCount() {
        if (filteredDataList != null) {
            return filteredDataList.size();
        }
        return 0;
    }

    public static class GestureListViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout gestureLayout;
        private final TextView tvAppName;
        private final ImageView imgApp;
        private final ImageView imgEdit;
        private final ImageView imgDelete;

        public GestureListViewHolder(@NonNull View itemView) {
            super(itemView);
            gestureLayout = itemView.findViewById(R.id.gesture_item_layout);
            imgApp = itemView.findViewById(R.id.img_app);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
            imgEdit = itemView.findViewById(R.id.img_edit);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }

    public interface ActionClickGestureItem {
        void onClickLayout(Apps app);

        void onClickEdit(Apps app);

        void onClickDelete(Apps app);
    }
}
