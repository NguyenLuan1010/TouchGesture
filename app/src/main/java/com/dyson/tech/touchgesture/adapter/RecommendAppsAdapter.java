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

public class RecommendAppsAdapter extends RecyclerView.Adapter<RecommendAppsAdapter.RecommendAppsViewHolder> {

    private List<Apps> appsList;
    private List<Apps> filteredDataList;
    private final ActionRecommendApp listener;

    public RecommendAppsAdapter(ActionRecommendApp listener) {
        this.listener = listener;
        appsList = new ArrayList<>();
        filteredDataList = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAppsList(List<Apps> appsList) {
        this.appsList = appsList;
        notifyDataSetChanged();
    }

    public void addApps(Apps app) {
        if (appsList == null && filteredDataList == null) {
            appsList = new ArrayList<>();
            filteredDataList = new ArrayList<>();
        }
        this.appsList.add(appsList.size(), app);
        this.filteredDataList.add(filteredDataList.size(),app);
        notifyItemInserted(filteredDataList.size());
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        List<Apps> filteredList = new ArrayList<>();

        for (Apps data : appsList) {
            if (data.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(data);
            }
        }
        filteredDataList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecommendAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recommend_app_item, parent, false);
        return new RecommendAppsAdapter.RecommendAppsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendAppsViewHolder holder, int position) {
        Apps app = filteredDataList.get(position);

        holder.imgApp.setImageDrawable(app.getIcon());
        holder.tvAppName.setText(app.getName());
        holder.appLayout.setOnClickListener(view -> {
            listener.onClickRecommendApp(app);
        });
    }

    @Override
    public int getItemCount() {
        if (filteredDataList != null) {
            return filteredDataList.size();
        }
        return 0;
    }

    public static class RecommendAppsViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout appLayout;
        private final ImageView imgApp;
        private final TextView tvAppName;

        public RecommendAppsViewHolder(@NonNull View itemView) {
            super(itemView);

            appLayout = itemView.findViewById(R.id.recommend_app_layout);
            imgApp = itemView.findViewById(R.id.img_app);
            tvAppName = itemView.findViewById(R.id.tv_app_name);
        }
    }

    public interface ActionRecommendApp {
        void onClickRecommendApp(Apps app);
    }
}
