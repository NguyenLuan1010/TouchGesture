package com.dyson.tech.touchgesture.presenter;

import android.content.Context;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;
import com.dyson.tech.touchgesture.view.ViewMainCallBack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotesPresenter {
    private final Context context;

    public NotesPresenter(Context context) {
        this.context = context;
    }

    public void getTodayNotes(LifecycleOwner lifecycleOwner,
                              ViewMainCallBack.GetTodayNotesCallBack callBack) {
        NoteDBHelper.init(context).notesDAO().getAllNotes().observe(lifecycleOwner, new Observer<List<Notes>>() {
            @Override
            public void onChanged(List<Notes> notes) {
                Calendar calendar = Calendar.getInstance();
                String currentDate = TimeHelper.init().getTime(calendar.getTimeInMillis(), TimeHelper.DATE_FORMAT);

                if (notes == null) {
                    callBack.notTodayNotes();
                } else {
                    List<Notes> todayNotes = new ArrayList<>();
                    for (Notes n : notes) {
                        String startDate = TimeHelper.init().getTime(n.getTimeStart(), TimeHelper.DATE_FORMAT);
                        String endDate = TimeHelper.init().getTime(n.getTimeEnd(), TimeHelper.DATE_FORMAT);

                        if (currentDate.equals(startDate) ||
                                currentDate.equals(endDate)) {
                            todayNotes.add(n);
                        }
                    }
                    if (todayNotes.size() > 0) {
                        callBack.todayNotes(todayNotes);
                    } else {
                        callBack.notTodayNotes();
                    }
                }
            }
        });
    }
}
