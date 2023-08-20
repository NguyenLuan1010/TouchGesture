package com.dyson.tech.touchgesture.view;

import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.model.Notes;

import java.util.List;

public abstract class ViewMainCallBack {

    public interface GetRecommendAppsCallBack {
        void topRecommendApps(Apps apps);

        void errorWhenLoad(String message);
    }

    public interface GetDeviceAppsCallBack {
        void appsOnDevice(Apps appsOnDevice);
    }

    public interface GetTodayNotesCallBack {
        void notTodayNotes();

        void todayNotes(List<Notes> todayNotes);
    }
}


