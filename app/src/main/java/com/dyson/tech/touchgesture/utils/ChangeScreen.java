package com.dyson.tech.touchgesture.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.dyson.tech.touchgesture.R;

public class ChangeScreen {
    private static ChangeScreen changeScreen;

    public static synchronized ChangeScreen init() {
        if (changeScreen == null) {
            changeScreen = new ChangeScreen();
        }
        return changeScreen;
    }

    public void replace(FragmentActivity activity,
                        Fragment fragment,
                        boolean isAddToBackStack) {

        FragmentManager frgManager = activity.getSupportFragmentManager();
        FragmentTransaction ft = frgManager.beginTransaction();

        ft.replace(R.id.main_layout, fragment);

        if (isAddToBackStack) {
            String backStateName = fragment.getClass().getName();
            ft.addToBackStack(backStateName);
        }

        ft.commit();
    }
}
