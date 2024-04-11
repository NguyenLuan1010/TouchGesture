package com.dyson.tech.touchgesture.view.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.RecommendAppsAdapter;
import com.dyson.tech.touchgesture.adapter.TodayNotesAdapter;
import com.dyson.tech.touchgesture.data.GestureFilesHelper;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.presenter.ApplicationsPresenter;
import com.dyson.tech.touchgesture.presenter.NotesPresenter;
import com.dyson.tech.touchgesture.service.AdsShowingService;
import com.dyson.tech.touchgesture.utils.ChangeScreen;
import com.dyson.tech.touchgesture.view.ViewMainCallBack;
import com.dyson.tech.touchgesture.view.dialog.ActionGestureDialog;
import com.dyson.tech.touchgesture.view.dialog.ActionNoteDialog;
import com.dyson.tech.touchgesture.view.dialog.ConfirmDialog;
import com.dyson.tech.touchgesture.view.dialog.LoadingDialog;
import com.dyson.tech.touchgesture.view.dialog.UsageStatsPermissionDialog;

import java.util.List;

public class UtilitiesFragment extends Fragment implements
        RecommendAppsAdapter.ActionRecommendApp,
        TodayNotesAdapter.ActionTodayNoteListener {

    private LinearLayout nothingLayout;
    private RelativeLayout todayNotesLayout, recommendAppsLayout;
    private RecyclerView rcvRecommendApps;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rcvTodayNotes;
    private LoadingDialog dialog;

    private ApplicationsPresenter presenter;
    private RecommendAppsAdapter recommendAppsAdapter;
    private TodayNotesAdapter todayNotesAdapter;
//    private AdsShowingService mAdsShowingService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_utilities, container, false);

        presenter = new ApplicationsPresenter(getActivity());
//        mAdsShowingService = new AdsShowingService(getActivity());
        recommendAppsAdapter = new RecommendAppsAdapter(this);
       View v= LayoutInflater.from(getActivity())
                .inflate(R.layout.ready_note_item, null);
        todayNotesAdapter = new TodayNotesAdapter(this);
        dialog = new LoadingDialog();

        initView(view);
        setTodayNotesLayout();
        setRefreshLayout();
        getAppUsageStats();

        return view;
    }

    private void initView(View view) {
        nothingLayout = view.findViewById(R.id.nothing_layout);
        todayNotesLayout = view.findViewById(R.id.today_notes_layout);
        recommendAppsLayout = view.findViewById(R.id.recommend_apps_layout);
        refreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        rcvTodayNotes = view.findViewById(R.id.rcv_today_notes);
        rcvRecommendApps = view.findViewById(R.id.rcv_recommend_apps);

        rcvRecommendApps.setAdapter(recommendAppsAdapter);

        todayNotesLayout.setOnClickListener(v -> {
//            if (mAdsShowingService.canShowAds()) {
//                mAdsShowingService.showAds();
//            }
            ChangeScreen.init().replace(getActivity(), new NotesSettingFragment(), true);
        });

        recommendAppsLayout.setOnClickListener(v -> {
//            if (mAdsShowingService.canShowAds()) {
//                mAdsShowingService.showAds();
//            }
            ChangeScreen.init().replace(getActivity(), new GesturesListFragment(), true);
        });
    }

    private void setTodayNotesLayout() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);
        NotesPresenter presenter = new NotesPresenter(getActivity());
        rcvTodayNotes.setAdapter(todayNotesAdapter);

        presenter.getTodayNotes(getViewLifecycleOwner(),
                new ViewMainCallBack.GetTodayNotesCallBack() {
                    @Override
                    public void notTodayNotes() {
                        dialog.dismiss();
                        nothingLayout.setVisibility(View.VISIBLE);
                        rcvTodayNotes.setVisibility(View.GONE);
                    }

                    @Override
                    public void todayNotes(List<Notes> todayNotes) {
                        dialog.dismiss();
                        todayNotesAdapter.setNotesList(todayNotes);

                        nothingLayout.setVisibility(View.GONE);
                        rcvTodayNotes.setVisibility(View.VISIBLE);
                    }
                });
    }

    public void setRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            setTodayNotesLayout();
            refreshLayout.setRefreshing(false);
        });
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

    @Override
    public void onClickDetail(Notes note) {
        ActionNoteDialog dialog = new ActionNoteDialog(ActionNoteDialog.EDIT_ACTION,note);
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    private void getAppUsageStats() {
        if (presenter.isGrantedUsageStats(getActivity())) {
            setRcvRecommendApps();
        } else {
            showGetUsageStatsPermissionDialog();
        }
    }

    private void showGetUsageStatsPermissionDialog() {
        UsageStatsPermissionDialog dialog = new UsageStatsPermissionDialog(getActivity());
        dialog.setListener(new UsageStatsPermissionDialog.ActionClickGetPermissionDialog() {
            @Override
            public void onClickCancel() {
                setDeviceApps();
            }

            @Override
            public void onClickGetPermission() {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                mStartForResult.launch(intent);
            }
        });
        dialog.show();
    }

    private final ActivityResultLauncher<Intent> mStartForResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (getActivity() == null) {
                            return;
                        }
                        if (presenter.isGrantedUsageStats(getActivity())) {
                            setRcvRecommendApps();
                        }
                    });

    private void setRcvRecommendApps() {

        dialog.show(getActivity().getSupportFragmentManager(), null);
        presenter.getRecommendApps(new ViewMainCallBack.GetRecommendAppsCallBack() {
            @Override
            public void topRecommendApps(Apps apps) {
                recommendAppsAdapter.addApps(apps);
                dialog.dismiss();
            }

            @Override
            public void errorWhenLoad(String message) {
                dialog.dismiss();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setDeviceApps() {

        dialog.show(getActivity().getSupportFragmentManager(), null);
        presenter.getDeviceApp(new ViewMainCallBack.GetDeviceAppsCallBack() {
            @Override
            public void appsOnDevice(Apps appsOnDevice) {
                execMain(() -> {
                    recommendAppsAdapter.addApps(appsOnDevice);
                    dialog.dismiss();
                });
            }
        });
    }

    @Override
    public void onClickRecommendApp(Apps app) {
        if (!GestureFilesHelper.isExistGestureAppFile(app)) {
            ActionGestureDialog dialog = new ActionGestureDialog(getString(R.string.add_gesture),
                    getString(R.string.please_draw_a_gesture_to_add_for_open_your_app),
                    app, null);
            dialog.show(getActivity().getSupportFragmentManager(), null);
        } else {
            Toast.makeText(getActivity(), getActivity().getString(R.string.please_select_another_app),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void execMain(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

}