package com.dyson.tech.touchgesture.adapter;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;

import java.util.List;

public class TodayNotesAdapter extends RecyclerView.Adapter<TodayNotesAdapter.TodayNotesViewHolder> {

    private List<Notes> notesList;
    private final ActionTodayNoteListener listener;

    public TodayNotesAdapter(ActionTodayNoteListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setNotesList(List<Notes> notesList) {
        this.notesList = notesList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public TodayNotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ready_note_item, parent, false);

        return new TodayNotesAdapter.TodayNotesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TodayNotesViewHolder holder, int position) {
        Notes note = notesList.get(position);
        long timeMillis = (note.getTimeStart() != 0) ? note.getTimeStart() : note.getTimeEnd();
        String time = TimeHelper.init().getTime(timeMillis, TimeHelper.TIME_FORMAT);
        holder.tvTime.setText(time);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescription.setText(note.getDescription());

        holder.imgDelete.setOnClickListener(view -> {
            listener.onClickDelete(note);
        });

        holder.noteLayout.setOnClickListener(view -> {
            listener.onClickDetail(note);
        });
    }

    @Override
    public int getItemCount() {
        if (notesList != null) {
            return notesList.size();
        }
        return 0;
    }

    public static class TodayNotesViewHolder extends RecyclerView.ViewHolder {

        private final RelativeLayout noteLayout;
        private final TextView tvTime;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final ImageView imgDelete;

        public TodayNotesViewHolder(@NonNull View itemView) {
            super(itemView);
            noteLayout = itemView.findViewById(R.id.notes_layout);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            imgDelete = itemView.findViewById(R.id.img_delete);
        }
    }

    public interface ActionTodayNoteListener {
        void onClickDelete(Notes note);

        void onClickDetail(Notes note);
    }
}
