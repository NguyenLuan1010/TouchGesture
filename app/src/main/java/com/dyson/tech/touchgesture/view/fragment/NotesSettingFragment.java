package com.dyson.tech.touchgesture.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.NotesListAdapter;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;
import com.dyson.tech.touchgesture.view.activity.MainActivity;
import com.dyson.tech.touchgesture.view.dialog.ActionNoteDialog;
import com.dyson.tech.touchgesture.view.dialog.ConfirmDialog;
import com.dyson.tech.touchgesture.view.dialog.LoadingDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class NotesSettingFragment extends Fragment implements NotesListAdapter.ActionNoteListListener {

    private LinearLayout nothingLayout;
    private SearchView searchView;
    private TextView tvAll, tvTimeFilter;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rcvNotesList;
    private FloatingActionButton btnAddNew;

    private NotesListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes_setting, container, false);
        adapter = new NotesListAdapter(this);
        initView(view);
        setRcvNotesList();
        searchData();
        setRefreshLayout();
        return view;
    }

    private void initView(View view) {
        MainActivity.setToolBar(false);
        searchView = view.findViewById(R.id.search_view);
        tvAll = view.findViewById(R.id.tv_all);
        tvAll.setOnClickListener(onClickListener);
        nothingLayout = view.findViewById(R.id.nothing_layout);
        tvTimeFilter = view.findViewById(R.id.tv_time_filter);
        tvTimeFilter.setOnClickListener(onClickListener);

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        rcvNotesList = view.findViewById(R.id.rcv_notes_list);
        btnAddNew = view.findViewById(R.id.btn_add_new);

        btnAddNew.setOnClickListener(viewRoot -> {
            ActionNoteDialog dialog = new ActionNoteDialog(ActionNoteDialog.ADD_ACTION, null);
            dialog.show(getActivity().getSupportFragmentManager(), null);
        });
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_time_filter:
                    clickTimeFilter();
                    break;
                case R.id.tv_all:
                    setRcvNotesList();
                    break;
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private void clickTimeFilter() {
        MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.
                Builder.
                dateRangePicker().
                setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds())).build();

        datePicker.show(getChildFragmentManager(), "DATE TIME RANGE");
        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long dateStart = selection.first;
            Long dateEnd = selection.second;
            String strStart = TimeHelper.init().getTime(dateStart, TimeHelper.DATE_FORMAT);
            String strEnd = TimeHelper.init().getTime(dateEnd, TimeHelper.DATE_FORMAT);
            tvTimeFilter.setText(strStart + " - " + strEnd);
            setNotesByTime(dateStart, dateEnd);
        });
    }

    private void setNotesByTime(long start, long end) {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);

       List<Notes> notesList =  NoteDBHelper.init(getActivity())
                .notesDAO()
                .getNoteByTime(start, end);

        if (notesList != null && notesList.size() > 0) {
            adapter.setNotesList(notesList);
            nothingLayout.setVisibility(View.GONE);
            rcvNotesList.setVisibility(View.VISIBLE);
        } else {
            nothingLayout.setVisibility(View.VISIBLE);
            rcvNotesList.setVisibility(View.GONE);
        }

        new Handler().postDelayed(dialog::dismiss, 700);
    }


    public void setRcvNotesList() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);

        rcvNotesList.setAdapter(adapter);
        NoteDBHelper.init(getActivity())
                .notesDAO()
                .getAllNotes()
                .observe(getViewLifecycleOwner(), notes -> {
                    if (notes != null && notes.size() > 0) {
                        adapter.setNotesList(notes);
                        nothingLayout.setVisibility(View.GONE);
                        rcvNotesList.setVisibility(View.VISIBLE);
                    } else {
                        nothingLayout.setVisibility(View.VISIBLE);
                        rcvNotesList.setVisibility(View.GONE);
                    }
                });

        new Handler().postDelayed(dialog::dismiss, 700);
    }

    private void searchData() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });
    }

    private void setRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            setRcvNotesList();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onClickLayout(Notes note) {
        ActionNoteDialog dialog = new ActionNoteDialog(ActionNoteDialog.EDIT_ACTION, note);
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    @Override
    public void onClickDelete(Notes note) {
        ConfirmDialog.getInstance(getActivity()).setTitle(getString(R.string.delete_gesture))
                .setMessage(getString(R.string.delete_note_confirm))
                .setIcon(R.drawable.baseline_warning_amber_24)
                .setPositiveButton("Ok", () -> {
                    NoteDBHelper.init(getActivity()).notesDAO().deleteNote(note.getId());
                })
                .setNegativeButton("Cancel", null)
                .create();
    }
}