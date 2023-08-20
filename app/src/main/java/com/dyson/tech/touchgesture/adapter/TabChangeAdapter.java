package com.dyson.tech.touchgesture.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dyson.tech.touchgesture.view.fragment.UtilitiesFragment;
import com.dyson.tech.touchgesture.view.fragment.SettingFragment;

public class TabChangeAdapter extends FragmentStateAdapter {

    private static final int PAGES_NUM = 2;

    public TabChangeAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new SettingFragment();
            case 0:
            default:
                return new UtilitiesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGES_NUM;
    }
}
