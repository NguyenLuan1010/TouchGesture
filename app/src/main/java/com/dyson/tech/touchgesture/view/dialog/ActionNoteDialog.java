package com.dyson.tech.touchgesture.view.dialog;

import static android.content.Context.POWER_SERVICE;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;

import java.util.Calendar;

public class ActionNoteDialog extends DialogFragment implements View.OnClickListener {

    public static final int EDIT_ACTION = 1;
    public static final int ADD_ACTION = 2;

    private EditText edtTitle, edtDescription;
    private TextView tvStartTime, tvEndTime;
    private int typeAction;
    private Notes noteUpdate;

    public ActionNoteDialog(int typeAction, Notes note) {
        this.typeAction = typeAction;
        noteUpdate = note;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_notes_dialog, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    private void initView(View view) {
        edtTitle = view.findViewById(R.id.edt_title);
        edtDescription = view.findViewById(R.id.edt_description);
        tvStartTime = view.findViewById(R.id.tv_start_time);
        tvStartTime.setOnClickListener(this);
        tvEndTime = view.findViewById(R.id.tv_end_time);
        tvEndTime.setOnClickListener(this);
        AppCompatButton btnSubmit = view.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(this);
        customDialog(getDialog());

        if (typeAction == EDIT_ACTION && noteUpdate != null)
            setData(noteUpdate);
    }

    public void setData(Notes note) {
        edtTitle.setText(note.getTitle());
        edtDescription.setText((note.getDescription() != null) ? note.getDescription() : " ");
        tvStartTime.setText(TimeHelper.init().getTime(note.getTimeStart(), TimeHelper.DATE_TIME_FORMAT));
        tvEndTime.setText(note.getTimeEnd() != 0
                ? TimeHelper.init().getTime(note.getTimeEnd(), TimeHelper.DATE_TIME_FORMAT)
                : " ");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_start_time:
                showDateTimePicker(true);
                break;
            case R.id.tv_end_time:
                showDateTimePicker(false);
                break;
            case R.id.btn_submit:
                onClickSubmit();

                PowerManager powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyApp::MyWakelockTag");
                wakeLock.acquire(10*60*1000L);
                break;
        }
    }

    private void showDateTimePicker(boolean isStartTime) {
        final Calendar c = Calendar.getInstance();
        int currentYear = c.get(Calendar.YEAR);
        int currentMonth = c.get(Calendar.MONTH);
        int currentDay = c.get(Calendar.DAY_OF_MONTH);
        int currentHour = c.get(Calendar.HOUR_OF_DAY);
        int currentMinute = c.get(Calendar.MINUTE);
        new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        new TimePickerDialog(getContext(),
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay,
                                                          int minute) {
                                        String dateTimeSelected = dayOfMonth +
                                                "-" + (monthOfYear + 1) +
                                                "-" + year + " "
                                                + hourOfDay + ":" + minute;
                                        if (isStartTime) {
                                            tvStartTime.setText(dateTimeSelected);
                                        } else {
                                            tvEndTime.setText(dateTimeSelected);
                                        }

                                    }
                                }, currentHour, currentMinute, false).show();
                    }
                }, currentYear, currentMonth, currentDay).show();
    }

    private void onClickSubmit() {
        String strStart = tvStartTime.getText().toString().trim();
        String strEnd = tvEndTime.getText().toString().trim();

        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        long startMillis = TimeHelper.init().getTimeMillis(strStart, TimeHelper.DATE_TIME_FORMAT);
        long endMillis = 0;
        if (!strEnd.isEmpty()) {
            endMillis = TimeHelper.init().getTimeMillis(strEnd, TimeHelper.DATE_TIME_FORMAT);
        }

        if (strStart.isEmpty() || title.isEmpty()) {
            Toast.makeText(getContext(), getString(R.string.title_and_start_time_must_be_entered_completely),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Calendar calendar = Calendar.getInstance();
        if (startMillis < calendar.getTimeInMillis() ||
                endMillis < calendar.getTimeInMillis()) {
            Toast.makeText(getContext(), getString(R.string.time_must_be_after_the_current_time),
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (startMillis >= endMillis) {
            Toast.makeText(getContext(), getString(R.string.end_time_must_be_after_start_time),
                    Toast.LENGTH_LONG).show();
            return;
        }
        Notes note = new Notes(title,
                description.isEmpty() ? " " : description,
                startMillis,
                endMillis,
                Notes.READY);

        progressData(note);
        dismiss();
    }

    private void progressData(Notes note){
        if (typeAction == ADD_ACTION) {
            NoteDBHelper.init(getContext()).notesDAO().insertNote(note);
            Toast.makeText(getContext(), getString(R.string.add_note_success),
                    Toast.LENGTH_LONG).show();
        } else if (typeAction == EDIT_ACTION) {
            NoteDBHelper.init(getContext()).notesDAO().updateNotes(noteUpdate.getId()
                    , note.getTitle()
                    , note.getTimeStart()
                    , note.getTimeEnd()
                    , note.getDescription());
            Toast.makeText(getContext(), getString(R.string.update_note_success),
                    Toast.LENGTH_LONG).show();
        }
    }


    private void customDialog(Dialog dialog) {
        dialog.getWindow().

                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().

                setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().

                getAttributes().windowAnimations = androidx.appcompat.R.style.Animation_AppCompat_Dialog;
        dialog.getWindow().

                setGravity(Gravity.BOTTOM);
    }

}
