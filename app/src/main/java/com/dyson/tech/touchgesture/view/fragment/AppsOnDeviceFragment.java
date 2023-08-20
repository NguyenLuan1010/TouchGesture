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
import android.widget.Toast;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.RecommendAppsAdapter;
import com.dyson.tech.touchgesture.data.GestureFilesHelper;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.presenter.ApplicationsPresenter;
import com.dyson.tech.touchgesture.view.ViewMainCallBack;
import com.dyson.tech.touchgesture.view.activity.MainActivity;
import com.dyson.tech.touchgesture.view.dialog.ActionGestureDialog;
import com.dyson.tech.touchgesture.view.dialog.LoadingDialog;

public class AppsOnDeviceFragment extends Fragment implements RecommendAppsAdapter.ActionRecommendApp {

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView rcvAppsOnDevice;

    private SearchView searchView;

    private RecommendAppsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apps_on_device, container, false);
        MainActivity.setToolBar(false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        ImageView imgBack = view.findViewById(R.id.img_back);
        imgBack.setOnClickListener(viewRoot -> {
            requireActivity().onBackPressed();
        });
        refreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        rcvAppsOnDevice = view.findViewById(R.id.rcv_apps_on_device);
        searchView = view.findViewById(R.id.search_view);
        setRcvAppsOnDevice();
        searchData();
        setRefreshLayout();
    }


    private void setRcvAppsOnDevice() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getActivity().getSupportFragmentManager(), null);

        adapter = new RecommendAppsAdapter(this);
        rcvAppsOnDevice.setAdapter(adapter);

        ApplicationsPresenter presenter = new ApplicationsPresenter(getActivity());
        presenter.getDeviceApp(appsOnDevice -> {
            new Handler(Looper.getMainLooper()).post(() -> adapter.addApps(appsOnDevice));
            dialog.dismiss();
        });
    }

    private void searchData(){
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

    public void setRefreshLayout() {
        refreshLayout.setOnRefreshListener(() -> {
            setRcvAppsOnDevice();
            refreshLayout.setRefreshing(false);
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
}