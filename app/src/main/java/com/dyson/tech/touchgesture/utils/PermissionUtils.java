package com.dyson.tech.touchgesture.utils;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dyson.tech.touchgesture.R;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;

public class PermissionUtils {

    private static PermissionUtils permissionUtils;

    public static synchronized PermissionUtils init() {
        if (permissionUtils == null) {
            permissionUtils = new PermissionUtils();
        }
        return permissionUtils;
    }

    public static void requestPackageUsageStatsPermission(@NonNull Fragment fragment, RequestCallback callback) {
        Context context = fragment.requireContext();
        PermissionX.init(fragment)
                .permissions(Manifest.permission.PACKAGE_USAGE_STATS)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList,
                        fragment.getString(R.string.permission_explain_read_package_usage_stats),
                        getButtonPositive(context),
                        getButtonNegative(context)))
                .onForwardToSettings(getForwardToSettingsCallback(context))
                .request(callback);
    }

    public static void requestPackageWriteExternalPermission(@NonNull Fragment fragment, RequestCallback callback) {
        Context context = fragment.requireContext();
        PermissionX.init(fragment)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList,
                        fragment.getString(R.string.permission_write_external_storange),
                        getButtonPositive(context),
                        getButtonNegative(context)))
                .onForwardToSettings(getForwardToSettingsCallback(context))
                .request(callback);
    }

    public static void requestBluetoothConnectPermission(@NonNull Fragment fragment, RequestCallback callback) {
        Context context = fragment.requireContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionX.init(fragment)
                    .permissions(Manifest.permission.BLUETOOTH_CONNECT)
                    .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList,
                            fragment.getString(R.string.permission_explain_bluetooth_connect),
                            getButtonPositive(context),
                            getButtonNegative(context)))
                    .onForwardToSettings(getForwardToSettingsCallback(context))
                    .request(callback);
        }
    }

    private static String getButtonPositive(@NonNull Context context) {
        return context.getString(R.string.permission_button_positive);
    }

    private static String getButtonNegative(@NonNull Context context) {
        return context.getString(R.string.permission_button_negative);
    }

    @NonNull
    private static ForwardToSettingsCallback getForwardToSettingsCallback(Context context) {
        return (scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList,
                context.getString(R.string.permission_forward_to_settings),
                context.getString(R.string.permission_button_go_setting),
                getButtonNegative(context)
        );
    }
}
