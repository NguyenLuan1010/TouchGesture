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
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;

import java.util.ArrayList;
import java.util.List;

public class NotesListAdapter extends RecyclerView.Adapter<NotesListAdapter.NotesListViewHolder> {

    private List<Notes> notesList;
    private List<Notes> filteredDataList;
    private final ActionNoteListListener listener;

    public NotesListAdapter(ActionNoteListListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotesList(List<Notes> notesList) {
        this.notesList = notesList;
        filteredDataList = notesList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filter(String query) {
        List<Notes> filteredList = new ArrayList<>();

        for (Notes data : notesList) {
            if (data.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(data);
            }
        }
        filteredDataList = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotesListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_item, parent, false);
        return new NotesListAdapter.NotesListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesListViewHolder holder, int position) {
        Notes note = filteredDataList.get(position);
        holder.tvTitle.setText(note.getTitle());

        String strTime = "Time: " + TimeHelper.init()
                .getTime(note.getTimeStart(), TimeHelper.DATE_TIME_FORMAT);
        if (note.getTimeEnd() != 0) {
            strTime = "Time: " + TimeHelper.init()
                    .getTime(note.getTimeStart(), TimeHelper.DATE_TIME_FORMAT) + " - " + TimeHelper.init()
                    .getTime(note.getTimeEnd(), TimeHelper.DATE_TIME_FORMAT);
        }
        holder.tvTime.setText(strTime);

        holder.notesLayout.setOnClickListener(view -> {
            listener.onClickLayout(note);
        });
        holder.imgDelete.setOnClickListener(view -> {
            listener.onClickDelete(note);
        });
    }

    @Override
    public int getItemCount() {
        if (filteredDataList != null) {
            return filteredDataList.size();
        }
        return 0;
    }

    public static class NotesListViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout notesLayout;
        private final TextView tvTitle;
        private final TextView tvTime;
        private final ImageView imgDelete;

        public NotesListViewHolder(@NonNull View itemView) {
            super(itemView);
            notesLayout = itemView.findViewById(R.id.notes_layout);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvTime = itemView.findViewById(R.id.tv_time);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }

    public interface ActionNoteListListener {
        void onClickLayout(Notes note);

        void onClickDelete(Notes note);
    }
}
