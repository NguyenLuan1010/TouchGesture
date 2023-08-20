package com.dyson.tech.touchgesture.view.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.service.HomeButtonService;
import com.dyson.tech.touchgesture.service.ServiceBuilder;
import com.dyson.tech.touchgesture.service.ServiceBuilder.*;
import com.dyson.tech.touchgesture.utils.ChangeScreen;
import com.dyson.tech.touchgesture.utils.PermissionUtils;
import com.dyson.tech.touchgesture.view.dialog.IconsSelectDialog;
import com.dyson.tech.touchgesture.view.dialog.SelectLanguageDialog;
import com.permissionx.guolindev.callback.RequestCallback;

import java.util.List;

public class SettingFragment extends Fragment implements View.OnClickListener {

    private RelativeLayout activeTouchLayout,
            gestureSettingLayout,
            iconsLayout,
            languageLayout,
            buyUpgradeLayout, notesSettingLayout;
    private SwitchCompat serviceAction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        initView(view);
        setSwitchActions();
        return view;
    }

    private void initView(View view) {
        serviceAction = view.findViewById(R.id.switch_app_management);
        activeTouchLayout = view.findViewById(R.id.active_touch_layout);
        activeTouchLayout.setOnClickListener(this);
        gestureSettingLayout = view.findViewById(R.id.geture_setting_layout);
        gestureSettingLayout.setOnClickListener(this);
        iconsLayout = view.findViewById(R.id.icons_layout);
        iconsLayout.setOnClickListener(this);
        languageLayout = view.findViewById(R.id.language_layout);
        languageLayout.setOnClickListener(this);
        buyUpgradeLayout = view.findViewById(R.id.buy_an_upgrade_layout);
        buyUpgradeLayout.setOnClickListener(this);
        notesSettingLayout = view.findViewById(R.id.notes_setting_layout);
        notesSettingLayout.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.active_touch_layout:

                break;

            case R.id.geture_setting_layout:
                ChangeScreen.init().replace(getActivity(), new GesturesListFragment(), true);
                break;
            case R.id.notes_setting_layout:
                ChangeScreen.init().replace(getActivity(), new NotesSettingFragment(), true);
                break;
            case R.id.icons_layout:
                IconsSelectDialog dialog = new IconsSelectDialog();
                dialog.show(getActivity().getSupportFragmentManager(),null);
                break;
            case R.id.language_layout:
                SelectLanguageDialog.init().showDialog(getActivity());
                break;
            case R.id.buy_an_upgrade_layout:
                break;
        }
    }

    private void setSwitchActions() {
        serviceAction.setChecked(isMyServiceRunning(HomeButtonService.class));
        serviceAction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent intent = new Intent(getActivity(), HomeButtonService.class);

                if (isChecked) {
                    if (hadOverlayPermission()) {
                        PermissionUtils.requestBluetoothConnectPermission(SettingFragment.this,
                                (allGranted, grantedList, deniedList) -> {
                                    if (allGranted) {
                                        startService(getActivity(), intent);
                                    } else {
                                        serviceAction.setChecked(false);
                                    }
                                });
                    } else {
                        giveOverlayPermission();
                    }
                } else {
                    getActivity().stopService(intent);
                }
            }
        });
    }

    private void startService(Context context, Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("ObsoleteSdkInt")
    private boolean hadOverlayPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getActivity());
    }

    private void giveOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        launcherPermission.launch(intent);
    }

    ActivityResultLauncher<Intent> launcherPermission = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK && hadOverlayPermission()) {

                Intent intent = new Intent(getActivity(), HomeButtonService.class);
                serviceAction.setChecked(true);
                startService(getActivity(), intent);

            }
        }
    });

}