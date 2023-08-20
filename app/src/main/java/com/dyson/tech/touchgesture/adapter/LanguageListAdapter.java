package com.dyson.tech.touchgesture.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;

import java.util.List;

public class LanguageListAdapter extends RecyclerView.Adapter<LanguageListAdapter.LanguageListViewHolder> {

    private List<String> languages;
    private int selectItem = -1;
    private String languageSelected;

    @SuppressLint("NotifyDataSetChanged")
    public void setLanguages(List<String> languages) {
        this.languages = languages;
        notifyDataSetChanged();
    }

    public String getLanguageSelected() {
        return languageSelected;
    }

    @NonNull
    @Override
    public LanguageListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_item,
                parent, false);
        return new LanguageListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageListViewHolder holder, int position) {
        String language = languages.get(position);

        holder.checkBox.setText(language);
        eventSelectItem(holder, language, position);

    }

    private void eventSelectItem(@NonNull LanguageListViewHolder holder,
                                 String language,
                                 int position) {

        holder.checkBox.setChecked(position == selectItem);

        holder.checkBox.setOnClickListener(view -> {
            if (selectItem != -1) {
                notifyItemChanged(selectItem);
            }
            selectItem = position;
            notifyItemChanged(selectItem);
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    languageSelected = language;
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (languages != null) {
            return languages.size();
        }
        return 0;
    }

    public static class LanguageListViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox checkBox;

        public LanguageListViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }

}
