package com.dyson.tech.touchgesture.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;

import java.util.List;

public class ThemesListAdapter extends RecyclerView.Adapter<ThemesListAdapter.ThemesListViewHolder> {

    private List<Integer> colorResource;
    private final ActionSelectColor listener;

    private int clickPosition = -1;

    public ThemesListAdapter(ActionSelectColor listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setColorResource(List<Integer> colorResource) {
        this.colorResource = colorResource;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ThemesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.theme_item, parent, false);
        return new ThemesListAdapter.ThemesListViewHolder(view);
    }

    @SuppressLint({"ResourceType", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ThemesListViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {

        int colorResource = this.colorResource.get(position);
        holder.imgColor.setBackgroundResource(colorResource);

        if (clickPosition == position) {
            holder.imgColor.setImageResource(R.drawable.baseline_check_circle_outline_24);
        } else {
            holder.imgColor.setImageResource(0);
        }

        holder.imgColor.setOnClickListener(view -> {

            int previousSelectedPosition = clickPosition;
            clickPosition = position;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(clickPosition);

            listener.onClickThemes(colorResource);
        });

    }

    @Override
    public int getItemCount() {
        if (colorResource != null) {
            return colorResource.size();
        }
        return 0;
    }

    public static class ThemesListViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgColor;

        public ThemesListViewHolder(@NonNull View itemView) {
            super(itemView);
            imgColor = itemView.findViewById(R.id.img_themes_color);
        }
    }

    public interface ActionSelectColor {
        void onClickThemes(int colorResource);
    }
}
