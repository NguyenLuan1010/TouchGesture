package com.dyson.tech.touchgesture.view.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.TabChangeAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewChange;

    private TabChangeAdapter adapter;
//    private AdView mAdView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        adapter = new TabChangeAdapter(getChildFragmentManager(), getLifecycle());

        initView(view);
        setTabLayout();
//        setAdView();
        return view;
    }

    private void initView(View view) {
        tabLayout = view.findViewById(R.id.tab_screen);
        viewChange = view.findViewById(R.id.view_change);
//        mAdView = view.findViewById(R.id.adView);
    }

    private void setTabLayout() {
        viewChange.setAdapter(adapter);
        viewChange.setCurrentItem(0);
        new TabLayoutMediator(tabLayout, viewChange, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(getString(R.string.utilities));
                    break;
                case 1:
                    tab.setText(getString(R.string.setting_));
                    break;
            }
        }).attach();
    }

    private void setAdView(){
        assert getActivity() != null;
        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
    }

}