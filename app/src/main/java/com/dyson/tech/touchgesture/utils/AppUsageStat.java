package com.dyson.tech.touchgesture.utils;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

public class AppUsageStat {

    public void printUsageStatistics(Context context,
                                     AppUsageStatsCallBack callBack) {
        @SuppressLint("InlinedApi")
        UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, -1);
        long startTime = calendar.getTimeInMillis();


        List<UsageStats> usageStatsList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);

        callBack.usageStatsADay(usageStatsList);

    }

    public interface AppUsageStatsCallBack {
        void usageStatsADay(List<UsageStats> usageStatsList);
    }
}
