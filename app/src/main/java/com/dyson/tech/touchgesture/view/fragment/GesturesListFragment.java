package com.dyson.tech.touchgesture.view.fragment;

import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.GesturesListAdapter;
import com.dyson.tech.touchgesture.data.GestureFilesHelper;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.utils.ChangeScreen;
import com.dyson.tech.touchgesture.view.AuthenticationCallBack;
import com.dyson.tech.touchgesture.view.activity.MainActivity;
import com.dyson.tech.touchgesture.view.dialog.ActionGestureDialog;
import com.dyson.tech.touchgesture.view.dialog.ConfirmDialog;
import com.dyson.tech.touchgesture.view.dialog.GestureDetailDialog;
import com.dyson.tech.touchgesture.view.dialog.LoadingDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GesturesListFragment extends Fragment
        implements GesturesListAdapter.ActionClickGestureItem,
        ActionGestureDialog.OnUpdatedGesture {

    private LinearLayout nothingLayout;
    private SearchView searchApp;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rcvGestureList;
    private FloatingActionButton btnAdd;

    private GestureFilesHelper gestureFilesHelper;
    private GesturesListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gestures_list, container, false);
        gestureFilesHelper = new GestureFilesHelper(getActivity());
        adapter = new GesturesListAdapter(this);
        MainActivity.setToolBar(false);
        initView(view);
        setRcvGestureList();
        setRefreshLayout();
        return view;
    }

    private void initView(View view) {
        ImageView imgBack = view.findViewById(R.id.img_back);
        imgBack.setOnClickListener(viewRoot -> requireActivity().onBackPressed());
        nothingLayout = view.findViewById(R.id.nothing_layout);
        btnAdd = view.findViewById(R.id.btn_add);
        searchApp = view.findViewById(R.id.search_view);
        refreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        rcvGestureList = view.findViewById(R.id.rcv_gesture_apps_list);

        searchApp.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });

        btnAdd.setOnClickListener(viewRoot -> {
            ChangeScreen.init().replace(getActivity(),new AppsOnDeviceFragment(),true);
        });
    }

    private void setRcvGestureList() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);

        rcvGestureList.setAdapter(adapter);
        gestureFilesHelper.getAllFile(new AuthenticationCallBack.GetGestureFilesCallBack() {
            @Override
            public void allFile(List<Apps> appsList) {
                adapter.setAppsList(appsList);
                nothingLayout.setVisibility(View.GONE);
                rcvGestureList.setVisibility(View.VISIBLE);
            }

            @Override
            public void nothingFile(String message) {
                nothingLayout.setVisibility(View.VISIBLE);
                rcvGestureList.setVisibility(View.GONE);
            }
        });

        new Handler().postDelayed(dialog::dismiss, 700);
    }

    private void setRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            setRcvGestureList();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    public void onClickLayout(Apps app) {
        GestureDetailDialog dialog = new GestureDetailDialog(app);
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    @Override
    public void onClickEdit(Apps app) {
        ActionGestureDialog dialog = new ActionGestureDialog(getString(R.string.edit_gesture),
                getString(R.string.please_draw_a_gesture_to_edit_for_open_your_app),
                app, this);
        dialog.show(getActivity().getSupportFragmentManager(), null);
    }

    @Override
    public void updatedGesture(Apps updatedApp) {
        setRcvGestureList();
    }

    @Override
    public void onClickDelete(Apps app) {
        ConfirmDialog.getInstance(getActivity()).setTitle(getString(R.string.delete_gesture))
                .setMessage(getString(R.string.confirm_to_delete_gesture))
                .setIcon(R.drawable.baseline_warning_amber_24)
                .setPositiveButton("Ok", () -> {
                    deleteGesture(app);
                })
                .setNegativeButton("Cancel", null)
                .create();
    }

    private void deleteGesture(Apps app) {
        if (gestureFilesHelper.isDeletedFile(app)) {
            setRcvGestureList();
            Toast.makeText(getContext(), getString(R.string.delete_gesture_success), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.delete_gesture_fail), Toast.LENGTH_LONG).show();
        }
    }
}