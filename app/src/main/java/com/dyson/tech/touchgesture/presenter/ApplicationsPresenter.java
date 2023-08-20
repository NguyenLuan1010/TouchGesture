package com.dyson.tech.touchgesture.presenter;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.utils.AppUsageStat;
import com.dyson.tech.touchgesture.view.ViewMainCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ApplicationsPresenter {

    private static final int MAX_VALUE_RESULT = 15;
    private final Context context;

    public ApplicationsPresenter(Context context) {
        this.context = context;
    }

    @SuppressLint("InlinedApi")
    public boolean isGrantedUsageStats(FragmentActivity activity) {
        boolean granted;
        AppOpsManager appOps = (AppOpsManager)
                activity.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), activity.getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            granted = (activity.
                    checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS)
                    == PackageManager.PERMISSION_GRANTED);
        } else {
            granted = (mode == AppOpsManager.MODE_ALLOWED);
        }
        return granted;
    }

    public void getRecommendApps(ViewMainCallBack.GetRecommendAppsCallBack callBack) {
        AppUsageStat appUsageStat = new AppUsageStat();
        appUsageStat.printUsageStatistics(context, usageStatsList -> {
            execThread(() -> {
                List<UsageStats> topUsageStats = getTopUsageApp(usageStatsList);
                for (UsageStats u : topUsageStats) {
                    Apps a = findAppByPackage(u.getPackageName());
                    if (a != null) {
                        callBack.topRecommendApps(a);
                    } else {
                        callBack.errorWhenLoad(context.getString(R.string.something_went_wrong));
                    }
                }
            });
        });
    }

    private List<UsageStats> getTopUsageApp(List<UsageStats> usageStatsList) {
        sortUsageStats(usageStatsList);
        List<UsageStats> topUsageStats = new ArrayList<>();
        for (int i = 0; i < MAX_VALUE_RESULT; i++) {
            topUsageStats.add(usageStatsList.get(i));
        }

        for (int i = 0; i < topUsageStats.size(); i++) {
            for (int j = 0; j < topUsageStats.size(); j++) {
                if (topUsageStats.get(i).getPackageName()
                        .equals(topUsageStats.get(j).getPackageName())) {
                    topUsageStats.remove(topUsageStats.get(j));
                }
            }
        }

        return topUsageStats;
    }

    private Apps findAppByPackage(String packageApp) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageApp, PackageManager.GET_META_DATA);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            Drawable appIcon = packageManager.getApplicationIcon(appInfo);

            return new Apps(appIcon, appName, packageApp, null);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

    }

    private void sortUsageStats(List<UsageStats> usageStats) {

        for (int i = 0; i < usageStats.size() - 1; i++) {
            for (int j = 0; j < usageStats.size() - i - 1; j++) {
                if (usageStats.get(j).getTotalTimeInForeground()
                        < usageStats.get(j + 1).getTotalTimeInForeground()) {
                    UsageStats temp = usageStats.get(j);
                    usageStats.set(j, usageStats.get(j + 1));
                    usageStats.set(j + 1, temp);
                }
            }
        }
    }

    private void execThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public void getDeviceApp(ViewMainCallBack.GetDeviceAppsCallBack callBack) {
        PackageManager packageManager = context.getPackageManager();
        exec(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getAppsFromAPI_R(packageManager, callBack);
            } else {
                getAppOtherAPI(packageManager, callBack);
            }
        });
    }

    private void exec(Runnable runnable) {
        new Thread(runnable).start();
    }

    private void getAppsFromAPI_R(PackageManager packageManager,
                                  ViewMainCallBack.GetDeviceAppsCallBack callBack) {

        List<ApplicationInfo> applicationInfoList = packageManager.
                getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : applicationInfoList) {
            Intent intent = packageManager.getLaunchIntentForPackage(applicationInfo.packageName);
            if (intent != null && Intent.ACTION_MAIN.equals(intent.getAction())) {
                callBack.appsOnDevice(loadAppFromPackage(packageManager, applicationInfo));
            }
        }
    }

    private void getAppOtherAPI(PackageManager pm,
                                ViewMainCallBack.GetDeviceAppsCallBack callBack) {

        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (resolveInfo.activityInfo != null && resolveInfo.activityInfo.packageName != null) {
                Log.e("ddd", "getAppOtherAPI: " + resolveInfo.activityInfo.loadIcon(pm));

                callBack.appsOnDevice(loadAppFromPackage(pm, resolveInfo.activityInfo));
            }
        }
    }

    private Apps loadAppFromPackage(PackageManager pm,
                                    PackageItemInfo packageItemInfo) {

        String appName = packageItemInfo.loadLabel(pm).toString();

        Drawable icon = packageItemInfo.loadIcon(pm);
        Apps deviceApps = new Apps();
        deviceApps.setIcon(icon);
        deviceApps.setStrPackage(packageItemInfo.packageName);
        deviceApps.setName(appName);

        return deviceApps;
    }
}
