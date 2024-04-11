package com.dyson.tech.touchgesture.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.TodayNotesAdapter;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.receiver.AlarmReceiver;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HomeButtonService extends Service implements LifecycleObserver {

    private ServiceBuilder builder;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new ServiceBuilder(HomeButtonService.this);
        builder.buildHomeButton();
        builder.buildFirstMenu();
        builder.buildSecondTaskView();
        builder.buildDrawGestureView();
        //  builder.buildAddNote();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LifecycleOwner lifecycleOwner = ProcessLifecycleOwner.get();
        lifecycleOwner.getLifecycle().addObserver(this);
        builder.buildNotification();

        NoteDBHelper.init(HomeButtonService.this)
                .notesDAO()
                .getAllNotes().observe(lifecycleOwner, new Observer<List<Notes>>() {
                    @Override
                    public void onChanged(List<Notes> notes) {
                        if (notes != null) {
                            for (Notes note : notes) {
                                buildAlarmTask(note);
                            }
                        }
                    }
                });
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        builder.removeViewInWindow();
        SettingSharedPref.getInstance(this).removeOnBtnHomeChangeListener();
    }

    private void buildAlarmTask(Notes note) {
        Intent intent = new Intent(HomeButtonService.this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.NOTES_TRANSFER, note.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(HomeButtonService.this,
                note.getId(), intent,
                PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeWakeUp(note.getTimeStart()), pendingIntent);
    }

    private long timeWakeUp(long time) {
        int wakeUpBefore = -5;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(time));
        calendar.add(Calendar.MINUTE, wakeUpBefore);
        return calendar.getTimeInMillis();
    }
ddddd
}

