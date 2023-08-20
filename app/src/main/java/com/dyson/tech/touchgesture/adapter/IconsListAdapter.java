package com.dyson.tech.touchgesture.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.dyson.tech.touchgesture.R;

import java.util.List;

public class IconsListAdapter extends RecyclerView.Adapter<IconsListAdapter.IconListViewHolder> {

    private List<Integer> iconsResource;
    private int clickPosition = -1;
    private final ActionSelectIcon listener;

    public IconsListAdapter(ActionSelectIcon listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setIconsResource(List<Integer> iconsResource) {
        this.iconsResource = iconsResource;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IconListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.icon_item, parent, false);
        return new IconsListAdapter.IconListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconListViewHolder holder,
                                 @SuppressLint("RecyclerView") int position) {
        int icon = iconsResource.get(position);
        holder.iconView.setAnimation(icon);

        holder.checkBox.setChecked(clickPosition == position);

        holder.iconLayout.setOnClickListener(view -> {
            int previousSelectedPosition = clickPosition;
            clickPosition = position;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(clickPosition);

            listener.onSelect(icon);
        });

        holder.checkBox.setOnClickListener(view -> {
            int previousSelectedPosition = clickPosition;
            clickPosition = position;
            notifyItemChanged(previousSelectedPosition);
            notifyItemChanged(clickPosition);

            listener.onSelect(icon);
        });
    }

    @Override
    public int getItemCount() {
        if (iconsResource != null) {
            return iconsResource.size();
        }
        return 0;
    }

    public static class IconListViewHolder extends RecyclerView.ViewHolder {
        private final RelativeLayout iconLayout;
        private final LottieAnimationView iconView;
        private final CheckBox checkBox;

        public IconListViewHolder(@NonNull View itemView) {
            super(itemView);

            iconLayout = itemView.findViewById(R.id.icons_layout);
            iconView = itemView.findViewById(R.id.icon_view);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }

    public interface ActionSelectIcon {
        void onSelect(int iconResource);
    }
}
